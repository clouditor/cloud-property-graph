package io.clouditor.graph.passes.java

import de.fraunhofer.aisec.cpg.TranslationContext
import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.graph.Annotation
import de.fraunhofer.aisec.cpg.graph.Name
import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.graph.declarations.MethodDeclaration
import de.fraunhofer.aisec.cpg.graph.declarations.RecordDeclaration
import de.fraunhofer.aisec.cpg.graph.declarations.TranslationUnitDeclaration
import de.fraunhofer.aisec.cpg.graph.parseName
import de.fraunhofer.aisec.cpg.graph.statements.ReturnStatement
import de.fraunhofer.aisec.cpg.graph.statements.expressions.*
import de.fraunhofer.aisec.cpg.passes.TranslationResultPass
import de.fraunhofer.aisec.cpg.processing.IVisitor
import de.fraunhofer.aisec.cpg.processing.strategy.Strategy
import io.clouditor.graph.*

/**
 * This pass parses an application for spring boot annotations and creates services and end-points
 * from it.
 */
class SpringBootPass(ctx: TranslationContext) : TranslationResultPass(ctx) {
    override fun cleanup() {}

    override fun accept(result: TranslationResult) {
        for (tu in result.translationUnits) {
            tu.accept(
                Strategy::AST_FORWARD,
                object : IVisitor<Node>() {
                    override fun visit(t: Node) {
                        when (t) {
                            is RecordDeclaration -> handleAnnotations(result, tu, t, t.annotations)
                            is MemberExpression -> handleExpression(t)
                        }
                    }
                }
            )
        }
    }

    fun handleExpression(e: MemberExpression) {
        if (e.base.name.localName == "HttpStatus") {
            // use the code, e.g. "HttpStatus.CONFLICT", as we are using this Spring syntax across
            // languages
            e.name = e.parseName(e.code.toString())
        }
    }

    fun handleAnnotations(
        result: TranslationResult,
        tu: TranslationUnitDeclaration,
        recordDeclaration: RecordDeclaration,
        annotations: List<Annotation>
    ) {
        val app = result.findApplicationByTU(tu)

        if (annotations.any { it.name.localName == "RestController" }) {
            // handle it as a request handler
            val handler = HttpRequestHandler(app, mutableListOf(), "")
            handler.name = recordDeclaration.name

            app?.functionalities?.plusAssign(handler)

            // the path of the controller is in the requestmapping annotation, if it is set
            val mapping =
                recordDeclaration.annotations.firstOrNull { it.name.localName == "RequestMapping" }
            mapping?.let { handler.path = getPath(mapping) }

            // look for methods
            for (method in recordDeclaration.methods) {
                handleMapping(method)?.let {
                    handler.httpEndpoints.plusAssign(it)

                    app?.functionalities?.plusAssign(it)

                    result += it
                }
            }

            result.additionalNodes.add(handler)
        }
    }

    private fun handleMapping(methodDeclaration: MethodDeclaration): HttpEndpoint? {
        val mapping =
            methodDeclaration.annotations.firstOrNull {
                it.name.localName == "RequestMapping" ||
                    it.name.localName == "PostMapping" ||
                    it.name.localName == "GetMapping"
            }
        if (mapping != null) {
            // TE is not really interesting for us here, but we need to fill it. maybe make it
            // optional later
            val te = /*TransportEncryption(null, null, false, false)*/ null

            // TODO: check for Spring authentication filters

            // the url is unknown at this point
            val endpoint =
                HttpEndpoint(
                    NoAuthentication(),
                    methodDeclaration,
                    getMethod(mapping),
                    getPath(mapping),
                    te,
                    null
                )
            endpoint.name = Name(endpoint.path)

            // if it's a mapping and has a simple return statement, it is an HttpStatus.OK
            val ret = methodDeclaration.prevDFG.firstOrNull()
            if (ret is ReturnStatement) {
                ret.name = methodDeclaration.parseName("HttpStatus.OK")
            }

            return endpoint
        }

        return null
    }

    private fun getPath(mapping: Annotation): String {
        var path = "/"

        mapping.getValueForName("path")?.let {
            if (it is Literal<*>) {
                path = it.value as String
            }
        }

        mapping.getValueForName("value")?.let {
            if (it is Literal<*>) {
                path = it.value as String
            }
        }
        return path
    }

    private fun getMethod(mapping: Annotation): String {
        var method = "GET"

        if (mapping.name.localName == "PostMapping") {
            method = "POST"
        }

        mapping.getValueForName("method")?.let {
            if (it is Literal<*>) {
                method = it.value as String
            }
        }

        return method
    }
}
