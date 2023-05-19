package io.clouditor.graph.passes

import de.fraunhofer.aisec.cpg.TranslationResult
import io.clouditor.graph.*
import io.clouditor.graph.nodes.isInSelector
import io.kubernetes.client.openapi.ApiClient
import io.kubernetes.client.openapi.Configuration
import io.kubernetes.client.openapi.apis.CoreV1Api
import io.kubernetes.client.openapi.apis.NetworkingV1Api
import io.kubernetes.client.openapi.models.V1Ingress
import io.kubernetes.client.openapi.models.V1Pod
import io.kubernetes.client.openapi.models.V1Service
import io.kubernetes.client.util.ClientBuilder
import io.kubernetes.client.util.KubeConfig
import java.io.FileReader
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.emptyList
import kotlin.collections.filter
import kotlin.collections.filterIsInstance
import kotlin.collections.first
import kotlin.collections.firstOrNull
import kotlin.collections.forEach
import kotlin.collections.listOf
import kotlin.collections.map
import kotlin.collections.mapOf
import kotlin.collections.mutableListOf
import kotlin.collections.plusAssign
import kotlin.collections.toCollection
import kotlin.collections.toMap

class KubernetesPass : CloudResourceDiscoveryPass() {

    override fun cleanup() {}

    override fun accept(t: TranslationResult) {
        // file path to your KubeConfig
        val kubeConfigPath = System.getProperty("user.home") + "/.kube/config"

        // loading the out-of-cluster config, a kubeconfig from file-system
        val client: ApiClient =
            ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(FileReader(kubeConfigPath))).build()

        // set the global default api-client to the in-cluster one from above
        Configuration.setDefaultApiClient(client)

        // the CoreV1Api loads default api-client from global configuration.
        val api = CoreV1Api()
        // val extensionsApi = ExtensionsV1beta1Api()
        val extensionsApi = NetworkingV1Api()

        val namespace = App.kubernetesNamespace

        // look for existing container orchestration nodes
        val cluster =
            t.additionalNodes.filterIsInstance<ContainerOrchestration>().firstOrNull {
                it.managementUrl == api.apiClient.basePath.replace(":443", "") // strip port
            }

        // invokes the CoreV1Api client
        val pods =
            api.listNamespacedPod(
                namespace,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                false
            )
        for (pod in pods.items) {
            val container = handlePod(t, pod, cluster)

            container?.let {
                t.computes.add(it)
                cluster?.containers?.add(it)
            }
        }

        val services =
            api.listNamespacedService(
                namespace,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                false
            )
        for (item in services.items) {
            val service = handleService(t, item, cluster)

            service?.let { t += it }
        }

        val endpoints =
            api.listNamespacedEndpoints(
                namespace,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                false
            )
        for (item in endpoints.items) {
            for (subset in item.subsets ?: emptyList()) {
                // look for container with the name
                val container =
                    t.computes.filterIsInstance(Container::class.java).firstOrNull {
                        // not sure why/when there are more addresses
                        it.name == subset.addresses?.get(0)?.targetRef?.name
                    }

                // look for the service
                val service =
                    t.additionalNodes.filterIsInstance(NetworkService::class.java).firstOrNull() {
                        // not sure if the name is really unique, probably need the namespace later
                        it.name == item.metadata?.name
                    }

                // some quick and dirty database heuristics
                heuristics(service, container, t)

                // was runsOn
                service?.compute = container
            }
        }

        val list =
            extensionsApi.listNamespacedIngress(
                namespace,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                false
            )
        for (item in list.items) {
            val ingress = handleIngress(t, item, cluster)

            ingress.forEach { t.additionalNodes.add(it) }
        }
    }

    private fun heuristics(service: NetworkService?, container: Container?, t: TranslationResult) {
        if (service?.name == "postgres") {
            val db =
                RelationalDatabaseService(
                    mutableListOf<Storage>(),
                    container,
                    service.ips,
                    listOf(),
                    service.ports,
                    null,
                    service.geoLocation,
                    mapOf()
                )
            db.name = service.name
            t += db
        }

        if (service?.name == "mongo") {
            val db =
                DocumentDatabaseService(
                    mutableListOf<Storage>(),
                    container,
                    service.ips,
                    listOf(),
                    service.ports,
                    null,
                    service.geoLocation,
                    mapOf()
                )
            db.name = service.name
            t += db
        }
    }

    private fun handlePod(
        t: TranslationResult,
        pod: V1Pod,
        cluster: ContainerOrchestration?
    ): Container? {
        val name = pod.spec?.containers?.first()?.image?.split(":")?.first()

        // just the first image for now
        val image: Image =
            t.getImageByName(name)
                ?: Image(null, null, mapOf()).let {
                    name?.let { n -> it.name = n }
                    t += it
                    it
                }
        val container =
            pod.metadata?.let { meta ->
                val c =
                    Container(
                        image,
                        null,
                        cluster?.geoLocation ?: GeoLocation("Europe"),
                        meta.labels?.toMap(HashMap())
                    )
                c.name = meta.name ?: ""

                // add env to labels with env_ prefix
                pod.spec?.containers?.first()?.env?.forEach {
                    c.labels["env_" + it.name] = it.value
                }

                // TODO(all): Fix that
                //                // if the cluster has resource logging, also add a DFG edge to it
                //                cluster?.resourceLogging?.let { c.nextDFG.add(it) }

                c
            }

        // runsOn is a shortcut for an application to a compute resource
        image.application?.runsOn?.add(container)

        // add dataflow from image to container
        container?.let { image.addNextDFG(it) }

        return container
    }

    private fun handleService(
        t: TranslationResult,
        service: V1Service,
        cluster: ContainerOrchestration?
    ): NetworkService? {
        return service.spec?.let { spec ->
            val ips = mutableListOf<String>()
            spec.clusterIP?.let { ips.add(it) }
            spec.externalIPs?.let { ips.addAll(it) }
            spec.loadBalancerIP?.let { ips.add(it) }

            val node =
                NetworkService(
                    null,
                    ips as ArrayList<String>?,
                    listOf(),
                    spec.ports?.map { it.port.toShort() }?.toCollection(ArrayList()) ?: ArrayList(),
                    null,
                    cluster?.geoLocation ?: GeoLocation("Europe"),
                    mapOf()
                )
            service.metadata?.name?.let {
                node.name = it

                // also add the name as IP / host
                node.ips.add(it)
            }

            node
        }
    }

    private fun handleIngress(
        t: TranslationResult,
        ingress: V1Ingress,
        cluster: ContainerOrchestration?
    ): List<LoadBalancer> {
        val list = mutableListOf<LoadBalancer>()

        for (rule in ingress.spec?.rules ?: listOf()) {
            for (path in rule.http?.paths ?: listOf()) {
                val url = rule.host + path.path

                // look for the service (TODO: add namespace to filter)
                val service =
                    t.additionalNodes.filterIsInstance(NetworkService::class.java).firstOrNull {
                        it.name == path.backend?.service?.name
                    }

                val hasTLS = ingress.spec?.tls?.isEmpty() ?: false

                val te =
                    TransportEncryption(
                        "TLS",
                        hasTLS,
                        hasTLS,
                        null,
                    ) // if it is enabled, it is enforced in Kubernetes

                // TODO: add multiple endpoints
                // lets set method as null for now, and lets mean that it accepts all methods
                val node =
                    LoadBalancer(
                        null,
                        listOf(HttpEndpoint(NoAuthentication(), null, null, path.path, te, url)),
                        listOf(service),
                        url,
                        null,
                        ArrayList(),
                        ArrayList(),
                        ArrayList(),
                        null,
                        cluster?.geoLocation ?: GeoLocation("Europe"),
                        mapOf()
                    )

                list += node
            }
        }

        return list
    }

    private fun selectContainers(
        t: TranslationResult,
        selector: Map<String, String>
    ): List<Container> {
        return t.computes.filter { it is Container && it.isInSelector(selector) }.map {
            it as Container
        }
    }
}
