package io.clouditor.graph.passes.java

import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.graph.Annotation
import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.graph.declarations.MethodDeclaration
import de.fraunhofer.aisec.cpg.graph.declarations.RecordDeclaration
import de.fraunhofer.aisec.cpg.graph.declarations.TranslationUnitDeclaration
import de.fraunhofer.aisec.cpg.graph.statements.expressions.Literal
import de.fraunhofer.aisec.cpg.passes.Pass
import de.fraunhofer.aisec.cpg.processing.IVisitor
import de.fraunhofer.aisec.cpg.processing.strategy.Strategy
import io.clouditor.graph.*

class JaxRsPass : Pass() {
    // for now, assume, that we have one JAX-RS Application per analysis
    // this might not be the case everytime

    override fun cleanup() {}

    override fun accept(result: TranslationResult?) {
        if (result != null) {
            for (tu in result.translationUnits) {
                tu.accept(
                    Strategy::AST_FORWARD,
                    object : IVisitor<Node?>() {
                        fun visit(r: RecordDeclaration) {
                            handleAnnotations(result, tu, r, r.annotations)
                        }
                    }
                )
            }
        }
    }

    private fun handleAnnotations(
        result: TranslationResult,
        tu: TranslationUnitDeclaration,
        r: RecordDeclaration,
        annotations: List<Annotation>
    ) {
        val app = result.findApplicationByTU(tu)

        if (annotations.any { it.name == "Path" }) {
            // handle it as a request handler
            val handler = HttpRequestHandler(app, mutableListOf(), "")
            handler.name = r.name

            app?.functionalitys?.plusAssign(handler)

            // the path of the controller is in the Path annotation, if it is set
            handler.path = getPath(r.annotations.firstOrNull { it.name == "Path" })

            // look for methods
            for (method in r.methods) {
                handleMapping(method)?.let {
                    handler.httpEndpoints.plusAssign(it)

                    app?.functionalitys?.plusAssign(handler)
                }
            }

            result += handler
        }
    }

    private fun handleMapping(methodDeclaration: MethodDeclaration): HttpEndpoint? {
        val mapping =
            methodDeclaration.annotations.firstOrNull { it.name == "GET" || it.name == "POST" }
        if (mapping != null) {
            // TE is not really interesting for us here, but we need to fill it. maybe make it
            // optional later
            val te = /*TransportEncryption(null, null, false, false)*/ null

            val pathMapping = methodDeclaration.annotations.firstOrNull { it.name == "Path" }

            // the url is unknown at this point
            val endpoint =
                HttpEndpoint(
                    NoAuthentication(),
                    te,
                    null,
                    getMethod(mapping),
                    methodDeclaration,
                    getPath(pathMapping),
                )
            endpoint.name = endpoint.path

            return endpoint
        }

        return null
    }

    private fun getMethod(mapping: Annotation): String {
        var method = "GET"

        if (mapping.name == "POST") {
            method = "POST"
        }

        return method
    }

    private fun getPath(mapping: Annotation?): String {
        var path = "/"

        mapping?.getValueForName("value")?.let {
            if (it is Literal<*>) {
                path = it.value as String
            }
        }

        return path
    }
}
