package io.clouditor.graph.passes

import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.graph.statements.expressions.*
import de.fraunhofer.aisec.cpg.passes.Pass
import io.clouditor.graph.*

abstract class HttpClientPass : Pass() {

    protected fun createHttpRequest(
        t: TranslationResult,
        url: String,
        call: CallExpression,
        method: String,
        app: Application?
    ): HttpRequest {
        val endpoints = getEndpointsForUrl(t, url, method)

        val request = HttpRequest(call, endpoints)
        request.name = method
        request.location = call.location

        endpoints.forEach { request.addNextDFG(it) }

        // call.invokes = listOf(request)
        call.addPrevDFG(request)

        val i = endpoints.firstOrNull()
        val f = i?.handler

        // for convenience, add DFG edges from return nodes to the point in the code where the
        // remote call happens
        f?.prevDFG?.forEach {
            // for each return node, connect it to the get-call
            it.addNextDFG(call)
            println("Connecting $it to $call")
        }

        app?.functionalities?.plusAssign(request)

        return request
    }

    private fun getEndpointsForUrl(
        t: TranslationResult,
        url: String,
        method: String
    ): List<HttpEndpoint> {
        log.info("Looking for endpoints for {} request to {}", method, url)

        return t.additionalNodes.filterIsInstance(HttpEndpoint::class.java).filter {
            endpointMatches(it, url) &&
                (it.method == method || it.method == null) // TODO: make methods an array
        }
    }

    private fun endpointMatches(it: HttpEndpoint, url: String): Boolean {
        // skip empty urls
        if (it.url == null) {
            return false
        }

        var endpointUrl = it.url
        var matchUrl = url

        // get rid of variable names
        endpointUrl = endpointUrl?.replace("[{<].*[}>]".toRegex(), "{}")
        matchUrl = matchUrl.replace("[{<].*[}>]".toRegex(), "{}")

        // add http, if not specified
        if (!endpointUrl.startsWith("http")) {
            endpointUrl = "http://${endpointUrl}"
        }

        if (!matchUrl.startsWith("http")) {
            matchUrl = "http://${matchUrl}"
        }

        // adding transport encryption if url is https
        if (endpointUrl.startsWith("https")){
            if (it.transportEncryption == null) {
                it.transportEncryption = TransportEncryption("TLS", true, false, "")
            }
        }

        val match = matchUrl.startsWith(endpointUrl)

        log.debug("{},{},{} == {}: {}", endpointUrl, it.path, it.method, matchUrl, match)

        return match
    }
}
