package io.clouditor.graph.passes.js

import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.graph.declarations.FunctionDeclaration
import de.fraunhofer.aisec.cpg.graph.declarations.ParamVariableDeclaration
import de.fraunhofer.aisec.cpg.graph.declarations.TranslationUnitDeclaration
import de.fraunhofer.aisec.cpg.graph.declarations.VariableDeclaration
import de.fraunhofer.aisec.cpg.graph.statements.expressions.*
import de.fraunhofer.aisec.cpg.passes.Pass
import de.fraunhofer.aisec.cpg.processing.IVisitor
import de.fraunhofer.aisec.cpg.processing.strategy.Strategy
import io.clouditor.graph.*

class JSHttpPass : Pass() {

    override fun cleanup() {}

    override fun accept(result: TranslationResult?) {
        if (result != null) {
            for (tu in result.translationUnits) {
                tu.accept(
                    Strategy::AST_FORWARD,
                    object : IVisitor<Node?>() {
                        fun visit(v: VariableDeclaration) {
                            handleVariableDeclaration(result, tu, v)
                        }
                    }
                )
            }
        }
    }

    private fun handleVariableDeclaration(
        result: TranslationResult,
        tu: TranslationUnitDeclaration,
        v: VariableDeclaration
    ) {
        if (v.name == "dispatcher" || (v.initializer as? CallExpression)?.name == "express") {
            val app = result.findApplicationByTU(tu)

            val requestHandler = HttpRequestHandler(app, mutableListOf(), "/")
            requestHandler.name = requestHandler.path

            tu.accept(
                Strategy::AST_FORWARD, // EOG_FORWARD would be better but seems to be broken on top
                // level statements
                object : IVisitor<Node?>() {
                    fun visit(mce: MemberCallExpression) {
                        val endpoint = handleEndpoint(result, tu, mce, v)

                        endpoint?.let {
                            requestHandler.httpEndpoints.plusAssign(it)
                            result += endpoint
                            app?.functionalities?.plusAssign(endpoint)
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
        return if ((mce.name == "onPost" ||
                mce.name == "onGet" ||
                mce.name == "post" ||
                mce.name == "get") && (mce.base as? DeclaredReferenceExpression)?.refersTo == v
        ) {
            val path: String =
                unRegex((mce.arguments.first() as? Literal<*>)?.value as? String ?: "/")
            val func = (mce.arguments[mce.arguments.size - 1] as? LambdaExpression)?.function

            val endpoint = HttpEndpoint(NoAuthentication(), func, getMethod(mce), path, null, null)
            endpoint.name = path

            // get the endpoint's handler and look for assignments of the request's JSON body
            func?.accept(
                Strategy::AST_FORWARD,
                object : IVisitor<Node?>() {
                    fun visit(me: MemberExpression) {
                        handleRequestUnpacking(func, me, endpoint)
                    }
                }
            )

            endpoint
        } else {
            null
        }
    }

    private fun handleRequestUnpacking(fd: FunctionDeclaration, me: MemberExpression, e: HttpEndpoint) {
        // TODO: this is specific to the naming we use in the PCE example; we should check if "req"
        // is actually an argument of the POST expression
        if (me.name == "body" && fd.parameters.first() == me.base) {
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
        return if (mce.name == "onPost" || mce.name == "post") {
            "POST"
        } else {
            "GET"
        }
    }
}
