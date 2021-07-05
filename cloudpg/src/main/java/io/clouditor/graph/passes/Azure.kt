package io.clouditor.graph.passes

import com.azure.core.credential.TokenCredential
import com.azure.core.management.AzureEnvironment
import com.azure.core.management.Region
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
import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.graph.declarations.ParamVariableDeclaration
import de.fraunhofer.aisec.cpg.graph.declarations.TranslationUnitDeclaration
import de.fraunhofer.aisec.cpg.graph.declarations.ValueDeclaration
import de.fraunhofer.aisec.cpg.graph.declarations.VariableDeclaration
import de.fraunhofer.aisec.cpg.graph.statements.expressions.*
import de.fraunhofer.aisec.cpg.passes.Pass
import de.fraunhofer.aisec.cpg.processing.IVisitor
import de.fraunhofer.aisec.cpg.processing.strategy.Strategy
import io.clouditor.graph.*
import io.clouditor.graph.nodes.followDFGReverse
import io.clouditor.graph.nodes.followEOG
import io.clouditor.graph.nodes.location

class AzureClientSDKPass : Pass() {
    override fun accept(t: TranslationResult) {
        for (tu in t.translationUnits) {
            tu.accept(
                Strategy::AST_FORWARD,
                object : IVisitor<Node?>() {
                    /*fun visit(c: MemberCallExpression) {
                        handleCall(t, tu, c)
                    }*/
                    fun visit(c: NewExpression) {
                        handleNewClient(t, tu, c)
                    }
                }
            )
        }
    }

    private fun handleNewClient(
        t: TranslationResult,
        tu: TranslationUnitDeclaration,
        c: NewExpression
    ) {
        try {
            if (c.type.name == "com.azure.storage.blob.BlobContainerClientBuilder") {
                // we need to follow the EOG until we have proper support for querying outgoing
                // edges in the graph because
                // we need to find call expressions which have the new expression as a base

                // var client =

                var endpoint: String
                var containerName: String
                var url: String = ""
                var client: ValueDeclaration? = null
                var appendClient: ValueDeclaration

                var eog: Node = c

                var path =
                    eog.followEOG {
                        it.end is CallExpression &&
                            it.end.name == "endpoint" &&
                            (it.end as CallExpression).base == c
                    }
                path?.let {
                    val call = it.last().end as CallExpression
                    eog = call
                    endpoint = (call.arguments.first() as Literal<*>).value.toString()
                    System.out.println(endpoint)

                    url = endpoint
                }

                path =
                    eog.followEOG {
                        it.end is CallExpression &&
                            it.end.name ==
                                "containerName" /*&& (it.end as CallExpression).base == c*/
                    }
                path?.let {
                    val call = it.last().end as CallExpression
                    eog = call
                    containerName = (call.arguments.first() as Literal<*>).value.toString()
                    System.out.println(containerName)

                    url += containerName
                }

                path =
                    eog.followEOG {
                        it.end is CallExpression &&
                            it.end.name == "buildClient" /*&& (it.end as CallExpression).base == c*/
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
                            it.end is CallExpression &&
                                it.end.name == "getAppendBlobClient" &&
                                (it.end as CallExpression).base is CallExpression &&
                                (it.end as CallExpression).base.name == "getBlobClient" &&
                                (((it.end as CallExpression).base as CallExpression).base as
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
                        call?.let { it1 -> handleCall(t, tu, it1, storage) }
                    }

                    // look for all calls to the client
                    for (base in append?.nextDFG ?: listOf()) {
                        val call = findCallWithByUsingEOG(base)
                        call?.let { it1 -> handleCall(t, tu, it1, storage) }
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
        storage: ObjectStorage
    ) {
        val app = t.findApplicationByTU(tu)

        if (c.name == "create") {
            System.out.println("We got an interesting call: create")

            val request = ObjectStorageRequest(listOf(storage), c, "create")
            request.addNextDFG(storage)
            request.name = request.type

            t += request

            app?.functionalitys?.plusAssign(request)
        } else if (c.name == "appendBlock") {
            println("We got an interesting call: appendBlock")

            // create an object storage request
            val request = ObjectStorageRequest(listOf(storage), c, "append")
            request.addNextDFG(storage)
            request.name = request.type

            t += request

            app?.functionalitys?.plusAssign(request)

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
            val param = s.followDFGReverse { it.second.name == "password" }

            if (param?.isEmpty() == false) {
                println("Dude, you are probably leaking a password.")
            }
        }
    }
}

class AzurePass : CloudResourceDiscoveryPass() {
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
        val storages = azure.storageAccounts().listByResourceGroup(App.azureResourceGroup)
        for (storage in storages) {
            t.additionalNodes.addAll(handleStorageAccounts(t, storage, azure))
        }

        // next, look for our log workbench
        val workspaces = logAnalytics.workspaces().listByResourceGroup(App.azureResourceGroup)
        for (workspace in workspaces) {
            val log = handleWorkspace(t, workspace)

            t += log

            val exports =
                logAnalytics.dataExports().listByWorkspace(App.azureResourceGroup, workspace.name())

            exports.forEach { _ ->
                val storage =
                    t.additionalNodes.filterIsInstance<ObjectStorage>().firstOrNull {
                        // TODO:  unique names
                        it.name == "am-containerlog"
                    }

                // model data export as ObjectStorageRequest
                val request = ObjectStorageRequest(listOf(storage), log, "append")
                storage?.let { request.addNextDFG(it) }

                // add DFG from the source to the sink
                request.to.forEach { request.source.nextDFG.add(it) }
                request.name = request.type

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
            val name = vm.tags().getOrDefault("image", null)

            val image = t.getImageByName(name)

            // image?.implements?.forEach { it.deployedOn.add(compute) }
            t.computes += compute
        }

        val disks = azure.disks().listByResourceGroup(App.azureResourceGroup)
        for (disk in disks) {
            t.additionalNodes.add(handleDisk(t, disk))
        }
    }

    private fun handleWorkspace(t: TranslationResult, workspace: Workspace): ResourceLogging {
        val logging = ResourceLogging(t.locationForRegion(workspace.region()), mapOf())
        logging.name = workspace.name()

        return logging
    }

    private fun handleCluster(
        t: TranslationResult,
        cluster: KubernetesCluster
    ): ContainerOrchestration {
        var log: ResourceLogging? = null

        // check if resource logging is activated
        val profile = cluster.addonProfiles().getValue("omsagent")
        profile?.let {
            if (it.enabled()) {
                val workspaceId = it.config()["logAnalyticsWorkspaceResourceID"]

                // TODO: FQN, for now just use the shortName
                val shortName = workspaceId?.split("/")?.last()

                log =
                    t.additionalNodes.filterIsInstance(ResourceLogging::class.java).firstOrNull {
                        it.name == shortName
                    }
            }
        }

        // generic compute for now
        val compute =
            ContainerOrchestration(
                log,
                mutableListOf(),
                "https://${cluster.innerModel().fqdn()}",
                t.locationForRegion(cluster.region()),
                mapOf()
            )
        compute.resourceLogging?.let {
            // add a DFG edge to it
            compute.nextDFG.add(it)
        }

        return compute
    }

    private fun handleStorageAccounts(
        t: TranslationResult,
        account: StorageAccount,
        azure: AzureResourceManager
    ): List<Storage> {
        val storageList = mutableListOf<Storage>()

        // loop through the containers (for our use case this is ok, for larger ones, probably not)
        val paged =
            azure.storageBlobContainers().listAsync(account.resourceGroupName(), account.name())
        for (blob in paged.collectList().block()) {
            val te =
                TransportEncryption(
                    "TLS",
                    account.innerModel().minimumTlsVersion().toString(),
                    account.innerModel().enableHttpsTrafficOnly(),
                    true
                )

            // TODO: also include other endpoints

            // for now only GET requests, since we assume this is a public bucket, in reality it
            // accepts more methods
            // TODO: make methods an array?

            val auth =
                if (blob.publicAccess() == PublicAccess.NONE) {
                    SingleSignOn() // this is closest to how auth works in Azure. TokenBaseed would
                    // be better
                } else {
                    NoAuthentication() // public access
                }

            val endpoint =
                HttpEndpoint(
                    auth,
                    te,
                    null,
                    account.innerModel().primaryEndpoints().blob() + blob.name(),
                    "GET",
                    null
                )

            // at rest seems to be default anyway now
            val storage =
                ObjectStorage(
                    endpoint,
                    AtRestEncryption("AES-256", null),
                    t.locationForRegion(account.region()),
                    mapOf()
                )
            storage.name = blob.name()

            t += endpoint
            storageList += storage
        }

        return storageList
    }

    private fun handleDisk(t: TranslationResult, disk: Disk): BlockStorage {
        val e = disk.innerModel().encryption()
        var atRest: AtRestEncryption? = null

        if (e.type() == EncryptionType.ENCRYPTION_AT_REST_WITH_PLATFORM_KEY) {
            atRest = ManagedKeyEncryption("AES-256", null)
        } else if (e.type() == EncryptionType.ENCRYPTION_AT_REST_WITH_CUSTOMER_KEY) {
            atRest =
                CustomerKeyEncryption(
                    e.diskEncryptionSetId(),
                    "AES-256",
                    null
                ) // not the actual key, but close enough
        }

        val block = BlockStorage(atRest, t.locationForRegion(disk.region()), mapOf())
        block.name = disk.name()

        return block
    }

    private fun handleVirtualMachine(
        t: TranslationResult,
        vm: VirtualMachine
    ): io.clouditor.graph.VirtualMachine {
        val compute = VirtualMachine(null, null, null, t.locationForRegion(vm.region()), mapOf())
        compute.name = vm.name()
        compute.labels = mapOf<String, String>()

        return compute
    }
}

fun TranslationResult.getImageByName(name: String?): Image? {
    return this.images.firstOrNull { it.name == name }
}

fun TranslationResult.getObjectStorageByUrl(url: String?): ObjectStorage? {
    return this.additionalNodes.firstOrNull {
        it is ObjectStorage && it.httpEndpoint.url == url
    } as?
        ObjectStorage
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
