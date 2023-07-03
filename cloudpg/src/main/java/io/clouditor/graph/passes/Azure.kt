package io.clouditor.graph.passes

import com.azure.core.credential.TokenCredential
import com.azure.core.management.AzureEnvironment
import com.azure.core.management.Region
import com.azure.core.management.exception.ManagementException
import com.azure.core.management.profile.AzureProfile
import com.azure.identity.AzureCliCredentialBuilder
import com.azure.identity.ChainedTokenCredentialBuilder
import com.azure.resourcemanager.AzureResourceManager
import com.azure.resourcemanager.compute.models.Disk
import com.azure.resourcemanager.compute.models.EncryptionType
import com.azure.resourcemanager.compute.models.VirtualMachine
import com.azure.resourcemanager.containerservice.models.KubernetesCluster
import com.azure.resourcemanager.loganalytics.LogAnalyticsManager
import com.azure.resourcemanager.loganalytics.models.Workspace
import com.azure.resourcemanager.storage.models.PublicAccess
import com.azure.resourcemanager.storage.models.StorageAccount
import de.fraunhofer.aisec.cpg.TranslationContext
import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.graph.Name
import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.graph.declarations.ParamVariableDeclaration
import de.fraunhofer.aisec.cpg.graph.declarations.TranslationUnitDeclaration
import de.fraunhofer.aisec.cpg.graph.declarations.ValueDeclaration
import de.fraunhofer.aisec.cpg.graph.declarations.VariableDeclaration
import de.fraunhofer.aisec.cpg.graph.statements.expressions.*
import de.fraunhofer.aisec.cpg.passes.TranslationResultPass
import de.fraunhofer.aisec.cpg.processing.IVisitor
import de.fraunhofer.aisec.cpg.processing.strategy.Strategy
import io.clouditor.graph.*
import io.clouditor.graph.nodes.followDFGReverse
import io.clouditor.graph.nodes.followEOG
import io.clouditor.graph.nodes.location
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import kotlin.time.toJavaDuration

@Suppress("UNUSED_PARAMETER")
class AzureClientSDKPass(ctx: TranslationContext) : TranslationResultPass(ctx) {
    override fun accept(result: TranslationResult) {
        val translationUnits =
            result.components.stream().flatMap { it.translationUnits.stream() }.toList()
        for (tu in translationUnits) {
            val app = result.findApplicationByTU(tu)

            tu.accept(
                Strategy::AST_FORWARD,
                object : IVisitor<Node>() {
                    override fun visit(t: Node) {
                        when (t) {
                            is NewExpression -> handleNewClient(result, tu, t, app)
                        }
                    }
                }
            )
        }
    }

    private fun handleNewClient(
        t: TranslationResult,
        tu: TranslationUnitDeclaration,
        c: NewExpression,
        app: Application?
    ) {
        try {
            if (c.type.name.toString() == "com.azure.storage.blob.BlobContainerClientBuilder") {
                // we need to follow the EOG until we have proper support for querying outgoing
                // edges in the graph because
                // we need to find call expressions which have the new expression as a base

                // var client =

                var endpoint: String
                var containerName: String
                var url = ""
                var client: ValueDeclaration? = null

                var eog: Node = c

                var path =
                    eog.followEOG {
                        it.end is CallExpression &&
                            it.end.name.localName == "endpoint" &&
                            (it.end as MemberCallExpression).base == c
                    }
                path?.let {
                    val call = it.last().end as CallExpression
                    eog = call
                    endpoint = (call.arguments.first() as Literal<*>).value.toString()
                    println(endpoint)

                    url = endpoint
                }

                path =
                    eog.followEOG {
                        it.end is CallExpression &&
                            it.end.name.localName ==
                                "containerName" /*&& (it.end as CallExpression).base == c*/
                    }
                path?.let {
                    val call = it.last().end as CallExpression
                    eog = call
                    containerName = (call.arguments.first() as Literal<*>).value.toString()
                    println(containerName)

                    url += containerName
                }

                path =
                    eog.followEOG {
                        it.end is CallExpression &&
                            it.end.name.localName ==
                                "buildClient" /*&& (it.end as CallExpression).base == c*/
                    }
                path?.let {
                    val call = it.last().end as CallExpression
                    eog = call
                    val next = call.nextDFG.iterator().next()
                    client =
                        if (next is ValueDeclaration) {
                            next
                        } else {
                            (next as DeclaredReferenceExpression).refersTo as ValueDeclaration?
                        }
                }

                if (client == null) {
                    return
                }

                println("Found connection to a storage account container:$url")

                // look for the storage account / container
                val storage = t.getObjectStorageByUrl(url)
                storage?.let {
                    println("Found object storage in our graph: $storage")

                    // look for more specific clients. in our case we only look at the
                    // AppendBlobClient
                    path =
                        eog.followEOG {
                            it.end is MemberCallExpression &&
                                it.end.name.localName == "getAppendBlobClient" &&
                                (it.end as MemberCallExpression).base is CallExpression &&
                                (it.end as MemberCallExpression).base?.name?.localName ==
                                    "getBlobClient" &&
                                (((it.end as CallExpression).callee as MemberCallExpression).base as
                                        DeclaredReferenceExpression)
                                    .refersTo == client
                        }

                    val next = (path?.last()?.end)?.nextDFG?.iterator()?.next()
                    val append =
                        if (next is ValueDeclaration) {
                            next
                        } else {
                            (next as DeclaredReferenceExpression).refersTo as ValueDeclaration?
                        }

                    append?.let {
                        val call = findCallWithByUsingEOG(append)
                        call?.let { it1 -> handleCall(t, tu, it1, storage, app) }
                    }

                    // look for all calls to the client
                    for (base in append?.nextDFG ?: listOf()) {
                        val call = findCallWithByUsingEOG(base)
                        call?.let { it1 -> handleCall(t, tu, it1, storage, app) }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun findCallWithByUsingEOG(base: Node): MemberCallExpression? {
        val path =
            base.followEOG {
                it.end is MemberCallExpression &&
                    ((it.end as MemberCallExpression).base == base ||
                        ((it.end as MemberCallExpression).base is DeclaredReferenceExpression &&
                            ((it.end as MemberCallExpression).base as DeclaredReferenceExpression)
                                .refersTo == base))
            }

        return path?.last()?.end as? MemberCallExpression
    }

    override fun cleanup() {}

    private fun handleCall(
        t: TranslationResult,
        tu: TranslationUnitDeclaration,
        c: CallExpression,
        storage: ObjectStorage,
        app: Application?
    ) {
        if (c.name.localName == "create") {
            println("We got an interesting call: create")

            val request = ObjectStorageRequest(c, listOf(storage), "create")
            request.addNextDFG(storage)
            request.name = Name(request.type, null)

            t += request

            app?.functionalities?.plusAssign(request)
        } else if (c.name.localName == "appendBlock") {
            println("We got an interesting call: appendBlock")

            // create an object storage request
            val request = ObjectStorageRequest(c, listOf(storage), "append")
            request.addNextDFG(storage)
            request.name = Name(request.type, null)

            t += request

            app?.functionalities?.plusAssign(request)

            // the following is more or less specific to our example application and should be
            // documented as a graph query in the paper

            // first parameter is always an input stream
            val inputStreamRef = c.arguments[0] as DeclaredReferenceExpression
            val inputStream = inputStreamRef.refersTo as VariableDeclaration
            val newExpression = inputStream.initializer as NewExpression
            val construct = newExpression.initializer as ConstructExpression

            // this is very hacky, but we assume that it is always a new
            // ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8))
            val sRef =
                (construct.arguments[0] as MemberCallExpression).base as DeclaredReferenceExpression
            val s = sRef.refersTo as ParamVariableDeclaration

            // follow
            val param = s.followDFGReverse { it.second.name.localName == "password" }

            if (param?.isEmpty() == false) {
                println("Dude, you are probably leaking a password.")
            }
        }
    }
}

class AzurePass(ctx: TranslationContext) : CloudResourceDiscoveryPass(ctx) {
    override fun cleanup() {}

    override fun accept(t: TranslationResult) {
        val profile = AzureProfile(AzureEnvironment.AZURE)
        val credential: TokenCredential =
            ChainedTokenCredentialBuilder()
                .addAll(
                    listOf(
                        // EnvironmentCredentialBuilder().build(),
                        AzureCliCredentialBuilder().build()
                    ),
                )
                .build()
        val azure = AzureResourceManager.authenticate(credential, profile).withDefaultSubscription()

        val logAnalytics =
            LogAnalyticsManager.authenticate(
                credential,
                AzureProfile(azure.tenantId(), azure.subscriptionId(), AzureEnvironment.AZURE)
            )

        // first, storage accounts
        try {
            val storages = azure.storageAccounts().listByResourceGroup(App.azureResourceGroup)
            for (storage in storages) {
                t.additionalNodes.add(handleStorageAccounts(t, storage, azure))
            }

            // next, look for our log workbench
            val workspaces = logAnalytics.workspaces().listByResourceGroup(App.azureResourceGroup)
            for (workspace in workspaces) {
                val log = handleWorkspace(t, workspace)

                t += log

                val exports =
                    logAnalytics
                        .dataExports()
                        .listByWorkspace(App.azureResourceGroup, workspace.name())

                exports.forEach { _ ->
                    val storage =
                        t.additionalNodes.filterIsInstance<ObjectStorage>().firstOrNull {
                            // TODO:  unique names
                            it.name.localName == "am-containerlog"
                        }

                    // model data export as ObjectStorageRequest
                    val request = ObjectStorageRequest(log, listOf(storage), "append")
                    storage?.let { request.addNextDFG(it) }

                    // add DFG from the source to the sink
                    request.to.forEach { request.source.nextDFG.add(it) }
                    request.name = Name(request.type, null)

                    t += request
                }
            }

            val clusters = azure.kubernetesClusters().listByResourceGroup(App.azureResourceGroup)
            for (cluster in clusters) {
                val compute = handleCluster(t, cluster)

                t += compute
            }

            val vms = azure.virtualMachines().listByResourceGroup(App.azureResourceGroup)
            for (vm in vms) {
                val compute = handleVirtualMachine(t, vm)

                // look for the image tag to connect services

                // val name = vm.tags().getOrDefault("image", null)
                // val image = t.getImageByName(name)
                // image?.implements?.forEach { it.deployedOn.add(compute) }
                t.computes += compute
            }

            val disks = azure.disks().listByResourceGroup(App.azureResourceGroup)
            for (disk in disks) {
                t.additionalNodes.add(handleDisk(t, disk))
            }
        } catch (ex: ManagementException) {
            log.error("Could not fetch Azure resources: {}", ex.message)
        }
    }

    private fun handleWorkspace(t: TranslationResult, workspace: Workspace): Logging {
        val loggingServices = mutableListOf<LoggingService>()
        val logService =
            LoggingService(
                null,
                null,
                null,
                null,
                null,
                null,
                t.locationForRegion(workspace.region()),
                mapOf()
            )
        loggingServices += logService

        val logging =
            ActivityLogging(
                true,
                workspace.retentionInDays().toDuration(DurationUnit.DAYS).toJavaDuration(),
                loggingServices
            )
        logging.name = Name(workspace.name(), null)

        return logging
    }

    private fun handleCluster(
        t: TranslationResult,
        cluster: KubernetesCluster
    ): ContainerOrchestration {
        var log: ResourceLogging? = null

        // check if resource logging is activated
        val profile = cluster.addonProfiles().getValue("omsagent")
        profile?.let { it ->
            if (it.enabled()) {
                val workspaceId = it.config()["logAnalyticsWorkspaceResourceID"]

                // TODO: FQN, for now just use the shortName
                val shortName = workspaceId?.split("/")?.last()

                log =
                    t.additionalNodes.filterIsInstance(ResourceLogging::class.java).firstOrNull {
                        it.name.localName == shortName
                    }
            }
        }

        // generic compute for now
        val compute =
            ContainerOrchestration(
                mutableListOf(),
                "https://${cluster.innerModel().fqdn()}",
                log,
                t.locationForRegion(cluster.region()),
                mapOf()
            )
        // TODO(all): Update compute logging DFG edge
        //        compute.Logging?.let {
        //            // add a DFG edge to it
        //            compute.nextDFG.add(it)
        //        }

        return compute
    }

    private fun handleStorageAccounts(
        t: TranslationResult,
        account: StorageAccount,
        azure: AzureResourceManager
    ): StorageService {
        val storageList = mutableListOf<Storage>()

        // Get transport encryption from the storage account information.
        val te =
            TransportEncryption(
                "TLS",
                true,
                account.innerModel().enableHttpsTrafficOnly(),
                account.innerModel().minimumTlsVersion().toString(),
            )

        // Get authenticity from the storage account information, it would be better to get that
        // information from the containers.
        val auth =
            if (account.isBlobPublicAccessAllowed) {
                NoAuthentication() // public access
            } else {
                SingleSignOn(true) // this is closest to how auth works in Azure. TokenBased would
                // be better
            }

        // TODO(all): We are not able to fill out the StorageService as some of the parameters are
        // specific to the Storage containers, e.g., authenticity, url,
        // For now we fill it out as good as we can with the current ontology
        val storageAccount =
            ObjectStorageService(
                HttpEndpoint(
                    auth,
                    null,
                    "GET",
                    null,
                    te,
                    account.innerModel().primaryEndpoints().blob() /*+ blob.name()*/
                ),
                null,
                null,
                null,
                null,
                ArrayList(listOf<Short>()),
                TransportEncryption(
                    "TLS",
                    true,
                    account.isHttpsTrafficOnly,
                    account.minimumTlsVersion().toString()
                ), // Transport encryption cannot be disabled
                GeoLocation(account.region().toString()),
                mapOf<String, String>()
            )
        // loop through the containers (for our use case this is ok, for larger ones, probably not)
        val paged =
            azure.storageBlobContainers().listAsync(account.resourceGroupName(), account.name())
        for (blob in paged.collectList().block() ?: listOf()) {

            // TODO: also include other endpoints

            // for now only GET requests, since we assume this is a public bucket, in reality it
            // accepts more methods
            // TODO: make methods an array?

            if (blob.publicAccess() == PublicAccess.NONE) {
                SingleSignOn(true) // this is closest to how auth works in Azure. TokenBased would
                // be better
            } else {
                NoAuthentication()
            }

            // at rest seems to be default anyway now
            val storage =
                ObjectStorage(
                    false, // TODO(all): Fix it!
                    Immutability(false), // TODO(all): Fix it!
                    mutableListOf(AtRestEncryption("AES-256", true)),
                    t.locationForRegion(account.region()),
                    mapOf()
                )
            storage.name = Name(blob.name(), null)

            storageList += storage
        }

        storageAccount.storage = storageList

        return storageAccount
    }

    private fun handleDisk(t: TranslationResult, disk: Disk): BlockStorage {
        val e = disk.innerModel().encryption()
        var atRest: AtRestEncryption? = null

        if (e.type() == EncryptionType.ENCRYPTION_AT_REST_WITH_PLATFORM_KEY) {
            atRest = ManagedKeyEncryption("AES-256", true)
        } else if (e.type() == EncryptionType.ENCRYPTION_AT_REST_WITH_CUSTOMER_KEY) {
            atRest =
                CustomerKeyEncryption(
                    e.diskEncryptionSetId(),
                    "AES-256",
                    true,
                ) // not the actual key, but close enough
        }

        val block =
            BlockStorage(null, mutableListOf(atRest), t.locationForRegion(disk.region()), mapOf())
        block.name = Name(disk.name(), null)

        return block
    }

    private fun handleVirtualMachine(
        t: TranslationResult,
        vm: VirtualMachine
    ): io.clouditor.graph.VirtualMachine {
        val compute =
            VirtualMachine(
                null,
                null,
                null,
                null,
                null,
                null,
                t.locationForRegion(vm.region()),
                mapOf()
            )
        compute.name = Name(vm.name(), null)
        compute.labels = mapOf<String, String>()

        return compute
    }
}

fun TranslationResult.getImageByName(name: String?): Image? {
    return this.images.firstOrNull { it.name.localName == name }
}

@Suppress("UNUSED_PARAMETER")
fun TranslationResult.getObjectStorageByUrl(url: String?): ObjectStorage? {
    //    return this.additionalNodes.firstOrNull {
    //        // TODO(all): How to check that?
    //        it is ObjectStorage && it.httpEndpoint.url == url
    //    } as?
    //        ObjectStorage
    return null
}

fun TranslationResult.locationForRegion(region: Region): GeoLocation {
    var locationName = "Global"

    // TODO: this is not complete, but will suffice for now
    if (region.name().endsWith("us") || region.name().endsWith("us2")) {
        locationName = "US"
    } else if (region.name().endsWith("europe") || region.name().contains("germany")) {
        locationName = "Europe"
    }

    return location(locationName)
}
