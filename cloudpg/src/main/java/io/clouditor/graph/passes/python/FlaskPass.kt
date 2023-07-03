package io.clouditor.graph.passes.python

import de.fraunhofer.aisec.cpg.TranslationContext
import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.graph.Annotation
import de.fraunhofer.aisec.cpg.graph.Name
import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.graph.declarations.FunctionDeclaration
import de.fraunhofer.aisec.cpg.graph.declarations.TranslationUnitDeclaration
import de.fraunhofer.aisec.cpg.graph.declarations.VariableDeclaration
import de.fraunhofer.aisec.cpg.graph.statements.ReturnStatement
import de.fraunhofer.aisec.cpg.graph.statements.expressions.*
import de.fraunhofer.aisec.cpg.passes.TranslationResultPass
import de.fraunhofer.aisec.cpg.processing.IVisitor
import de.fraunhofer.aisec.cpg.processing.strategy.Strategy
import io.clouditor.graph.*

@Suppress("UNUSED_PARAMETER")
class FlaskPass(ctx: TranslationContext) : TranslationResultPass(ctx) {
    // for now, assume, that we have one Flask application per analysis
    // this might not be the case everytime

    private val httpMap: Map<String, String> =
        mapOf(
            // harmonize the http status codes across frameworks; we use the names as used in Spring
            // here
            // TODO add more status code mappings
            "200" to "HttpStatus.OK",
            "201" to "HttpStatus.CREATED",
            "202" to "HttpStatus.ACCEPTED",
            "404" to "HttpStatus.NOT_FOUND",
            "409" to "HttpStatus.CONFLICT",
        )

    override fun cleanup() {}

    override fun accept(result: TranslationResult) {
        // if (this.lang is PythonLanguageFrontend) {
        val translationUnits =
            result.components.stream().flatMap { it.translationUnits.stream() }.toList()
        for (tu in translationUnits) {
            tu.accept(
                Strategy::AST_FORWARD,
                object : IVisitor<Node>() {
                    override fun visit(t: Node) {
                        when (t) {
                            is VariableDeclaration ->
                                handleVariableDeclarations(result, tu, t, t.annotations)
                        }
                    }
                }
            )
        }
    }

    private fun handleVariableDeclarations(
        result: TranslationResult,
        tu: TranslationUnitDeclaration,
        v: VariableDeclaration,
        annotations: MutableList<Annotation>
    ) {
        val app = result.findApplicationByTU(tu)

        if ((v.initializer as? CallExpression)?.name?.localName == "Flask") {
            // handle it as a request handler
            val handler = HttpRequestHandler(app, mutableListOf(), "/")
            handler.name = v.name

            app?.functionalities?.plusAssign(handler)

            // look for functions
            tu.accept(
                Strategy::AST_FORWARD,
                object : IVisitor<Node>() {
                    override fun visit(t: Node) {
                        when (t) {
                            is FunctionDeclaration -> {
                                handleMapping(t)?.let {
                                    handler.httpEndpoints.plusAssign(it)

                                    app?.functionalities?.plusAssign(it)

                                    result += it
                                }
                            }
                        }
                    }
                }
            )

            result += handler
        }
    }

    private fun handleMapping(func: FunctionDeclaration): HttpEndpoint? {
        val mapping = func.annotations.firstOrNull { it.name.localName == "route" }
        if (mapping != null) {
            // TE is not really interesting for us here, but we need to fill it. maybe make it
            // optional later
            val te = /*TransportEncryption(null, null, false, false)*/ null

            // the url is unknown at this point
            val endpoint =
                HttpEndpoint(
                    NoAuthentication(),
                    func,
                    getMethod(mapping),
                    getPath(mapping),
                    te,
                    null
                )
            endpoint.name = Name(endpoint.path)

            // get the endpoint's handler and look for assignments of the request's JSON body
            func.accept(
                Strategy::AST_FORWARD,
                object : IVisitor<Node>() {
                    override fun visit(t: Node) {
                        when (t) {
                            is MemberExpression -> handleRequestUnpacking(t, endpoint)
                            is ReturnStatement -> handleReturnStatement(t)
                        }
                    }
                }
            )

            return endpoint
        }

        return null
    }

    private fun handleRequestUnpacking(me: MemberExpression, e: HttpEndpoint) {
        if (me.name.localName == "json" && me.base.name.localName == "request") {
            // set the DFG target of this call to the DFG target of our http endpoints
            me.nextDFG.forEach { e.addNextDFG(it) }

            // TODO(oxisto): Once we update the ontology, we should also set this as the
            // "request_body" property of the http endpoint

        }
    }

    private fun getMethod(mapping: Annotation): String {
        var method = "GET"
        (mapping.members.firstOrNull { it.name.localName == "methods" }?.value as?
                InitializerListExpression)
            ?.initializers?.firstOrNull()
            .let { it -> (it as? Literal<*>)?.let { method = it.value.toString() } }

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

    private fun handleReturnStatement(rs: ReturnStatement) {
        val returnValue = rs.returnValue as InitializerListExpression
        // set the correct http status code by looking through the initializers
        // FIXME: Safety measures added later; they were not necessary with the previous CPG
        //  version. This can mean that the expected value differs from before (not null/empty).
        returnValue.initializers.firstOrNull { it.name.toString() in httpMap }.let {
            returnValue.name = Name(httpMap[it?.name.toString()].toString())
        }
    }
}
