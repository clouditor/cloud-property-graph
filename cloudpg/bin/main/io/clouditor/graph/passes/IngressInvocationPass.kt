package io.clouditor.graph.passes

import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.passes.Pass
import io.clouditor.graph.*
import io.clouditor.graph.passes.golang.appendPath

class IngressInvocationPass : Pass() {
    override fun accept(t: TranslationResult) {
        // loop through services
        val services = t.additionalNodes.filterIsInstance(NetworkService::class.java)

        for (service in services) {
            // look for containers
            handle(
                t,
                service,
                "${service.name}:${service.ports.firstOrNull()}",
                TransportEncryption(null, false, false, null)
            )
        }

        // loop through all ingresses
        val ingresses = t.additionalNodes.filterIsInstance(LoadBalancer::class.java)

        for (ingress in ingresses) {
            // look for containers
            for (backend in ingress.networkServices) {
                handle(t, backend, ingress.url, ingress.httpEndpoints.first().transportEncryption)
            }
        }
    }

    private fun handle(
        t: TranslationResult,
        backend: NetworkService,
        inUrl: String,
        transportEncryption: TransportEncryption
    ) {
        val controllers =
            t.additionalNodes
                .filter {
                    it is HttpRequestHandler &&
                        it.application?.runsOn?.contains(backend.compute) == true
                }
                .map { it as HttpRequestHandler }
        for (controller in controllers) {
            for (endpoint in controller.httpEndpoints) {
                var url = inUrl.appendPath(controller.path)
                url = url.appendPath(endpoint.path)

                // for now, NoAuthentication
                val authentication = NoAuthentication()

                // lets create the proxied endpoint
                val proxy =
                    ProxiedEndpoint(
                        listOf(endpoint),
                        authentication,
                        endpoint.handler,
                        endpoint.method,
                        endpoint.path,
                        // use the TE of the ingress's TE
                        transportEncryption,
                        url
                    )
                proxy.addNextDFG(endpoint)
                t += proxy
            }
        }
    }

    override fun cleanup() {}
}
