package io.clouditor.graph.passes.js

import de.fraunhofer.aisec.cpg.TranslationContext
import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.graph.Name
import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.graph.declarations.FunctionDeclaration
import de.fraunhofer.aisec.cpg.graph.declarations.TranslationUnitDeclaration
import de.fraunhofer.aisec.cpg.graph.declarations.VariableDeclaration
import de.fraunhofer.aisec.cpg.graph.statements.expressions.*
import de.fraunhofer.aisec.cpg.passes.TranslationResultPass
import de.fraunhofer.aisec.cpg.processing.IVisitor
import de.fraunhofer.aisec.cpg.processing.strategy.Strategy
import io.clouditor.graph.*

@Suppress("UNUSED_PARAMETER")
class JSHttpPass(ctx: TranslationContext) : TranslationResultPass(ctx) {

    override fun cleanup() {}

    override fun accept(result: TranslationResult) {
        val translationUnits =
            result.components.stream().flatMap { it.translationUnits.stream() }.toList()
        for (tu in translationUnits) {
            tu.accept(
                Strategy::AST_FORWARD,
                object : IVisitor<Node>() {
                    override fun visit(t: Node) {
                        when (t) {
                            is VariableDeclaration -> handleVariableDeclaration(result, tu, t)
                        }
                    }
                }
            )
        }
    }

    private fun handleVariableDeclaration(
        result: TranslationResult,
        tu: TranslationUnitDeclaration,
        v: VariableDeclaration
    ) {
        if (v.name.localName == "dispatcher" ||
                (v.initializer as? CallExpression)?.name?.localName == "express"
        ) {
            val app = result.findApplicationByTU(tu)

            val requestHandler = HttpRequestHandler(app, mutableListOf(), "/")
            requestHandler.name = Name(requestHandler.path)

            tu.accept(
                Strategy::AST_FORWARD, // EOG_FORWARD would be better but seems to be broken on top
                // level statements
                object : IVisitor<Node>() {
                    override fun visit(t: Node) {
                        when (t) {
                            is MemberCallExpression -> {
                                val endpoint = handleEndpoint(result, tu, t, v)

                                endpoint?.let {
                                    requestHandler.httpEndpoints.plusAssign(it)
                                    result += endpoint
                                    app?.functionalities?.plusAssign(endpoint)
                                }
                            }
                        }
                    }
                }
            )

            result += requestHandler
            app?.functionalities?.plusAssign(requestHandler)
        }
    }

    private fun handleEndpoint(
        result: TranslationResult,
        tu: TranslationUnitDeclaration?,
        mce: MemberCallExpression,
        v: VariableDeclaration
    ): HttpEndpoint? {
        return if ((mce.name.localName == "onPost" ||
                mce.name.localName == "onGet" ||
                mce.name.localName == "post" ||
                mce.name.localName == "get") &&
                (mce.base as? DeclaredReferenceExpression)?.refersTo == v
        ) {
            val path: String =
                unRegex((mce.arguments.first() as? Literal<*>)?.value as? String ?: "/")
            val func = (mce.arguments[mce.arguments.size - 1] as? LambdaExpression)?.function

            val endpoint = HttpEndpoint(NoAuthentication(), func, getMethod(mce), path, null, null)
            endpoint.name = Name(path)

            // get the endpoint's handler and look for assignments of the request's JSON body
            func?.accept(
                Strategy::AST_FORWARD,
                object : IVisitor<Node>() {
                    override fun visit(t: Node) {
                        when (t) {
                            is MemberCallExpression -> {
                                handleRequestUnpacking(func, t, endpoint)
                            }
                        }
                    }
                }
            )

            endpoint
        } else {
            null
        }
    }

    private fun handleRequestUnpacking(
        fd: FunctionDeclaration,
        me: MemberExpression,
        e: HttpEndpoint
    ) {
        if (me.name.localName == "body" &&
                fd.parameters.first() == (me.base as? DeclaredReferenceExpression)?.refersTo
        ) {
            // set the DFG target of this call to the DFG target of our http endpoints
            me.nextDFG.forEach { e.addNextDFG(it) }

            // TODO(oxisto): Once we update the ontology, we should also set this as the
            // "request_body" property of the http endpoint
        }
    }

    private fun unRegex(`in`: String): String {
        // very hacky way to get rid of the regex
        var s = `in`

        s = s.replace("^", "")
        s = s.replace("\\/", "/")
        s = s.replace("[0-9]*", "{number}")

        return s
    }

    private fun getMethod(mce: MemberCallExpression): String {
        return if (mce.name.localName == "onPost" || mce.name.localName == "post") {
            "POST"
        } else {
            "GET"
        }
    }
}
