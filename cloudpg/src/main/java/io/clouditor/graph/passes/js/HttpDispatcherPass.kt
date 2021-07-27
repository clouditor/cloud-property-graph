package io.clouditor.graph.passes.js

import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.graph.declarations.FunctionDeclaration
import de.fraunhofer.aisec.cpg.graph.declarations.TranslationUnitDeclaration
import de.fraunhofer.aisec.cpg.graph.declarations.VariableDeclaration
import de.fraunhofer.aisec.cpg.graph.statements.expressions.DeclaredReferenceExpression
import de.fraunhofer.aisec.cpg.graph.statements.expressions.Literal
import de.fraunhofer.aisec.cpg.graph.statements.expressions.MemberCallExpression
import de.fraunhofer.aisec.cpg.passes.Pass
import de.fraunhofer.aisec.cpg.processing.IVisitor
import de.fraunhofer.aisec.cpg.processing.strategy.Strategy
import io.clouditor.graph.*

class HttpDispatcherPass : Pass() {

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
        if (v.name == "dispatcher") {
            val app = result.findApplicationByTU(tu)

            val requestHandler = HttpRequestHandler(app, mutableListOf(), "/")
            requestHandler.name = requestHandler.path

            v.accept(
                Strategy::EOG_FORWARD,
                object : IVisitor<Node?>() {
                    fun visit(mce: MemberCallExpression) {
                        val endpoint = handleEndpoint(result, tu, mce)

                        endpoint?.let {
                            requestHandler.httpEndpoints.plusAssign(it)
                            result += endpoint
                            app?.functionalitys?.plusAssign(endpoint)
                        }
                    }
                }
            )

            result += requestHandler
            app?.functionalitys?.plusAssign(requestHandler)
        }
    }

    private fun handleEndpoint(
        result: TranslationResult,
        tu: TranslationUnitDeclaration?,
        mce: MemberCallExpression
    ): HttpEndpoint? {
        return if (mce.name == "onPost" || mce.name == "onGet") {
            val path: String =
                unRegex((mce.arguments.first() as? Literal<*>)?.value as? String ?: "/")
            val func =
                (mce.arguments[mce.arguments.size - 1] as? DeclaredReferenceExpression)
                    ?.refersTo as?
                    FunctionDeclaration

            val endpoint = HttpEndpoint(NoAuthentication(), null, null, getMethod(mce), func, path)
            endpoint.name = path

            endpoint
        } else {
            null
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
        return if (mce.name == "onPost") {
            "POST"
        } else {
            "GET"
        }
    }
}
