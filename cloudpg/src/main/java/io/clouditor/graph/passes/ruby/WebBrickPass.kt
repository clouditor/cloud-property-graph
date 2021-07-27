package io.clouditor.graph.passes.ruby

import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.graph.declarations.FunctionDeclaration
import de.fraunhofer.aisec.cpg.graph.declarations.TranslationUnitDeclaration
import de.fraunhofer.aisec.cpg.graph.declarations.VariableDeclaration
import de.fraunhofer.aisec.cpg.graph.statements.CompoundStatement
import de.fraunhofer.aisec.cpg.graph.statements.DeclarationStatement
import de.fraunhofer.aisec.cpg.graph.statements.expressions.*
import de.fraunhofer.aisec.cpg.passes.Pass
import de.fraunhofer.aisec.cpg.processing.IVisitor
import de.fraunhofer.aisec.cpg.processing.strategy.Strategy
import io.clouditor.graph.*
import io.clouditor.graph.passes.golang.appendPath

class WebBrickPass : Pass() {

    override fun cleanup() {}

    override fun accept(result: TranslationResult?) {
        if (result != null) {
            for (tu in result.translationUnits) {
                val app = result.findApplicationByTU(tu)

                // one handler per file
                val handler = HttpRequestHandler(app, mutableListOf(), "/")

                tu.accept(
                    Strategy::AST_FORWARD,
                    object : IVisitor<Node?>() {
                        fun visit(mce: MemberCallExpression) {
                            handleMemberCall(result, tu, mce, handler)
                        }
                    }
                )

                result += handler

                app?.functionalitys?.plusAssign(handler)
            }
        }
    }

    private fun handleMemberCall(
        result: TranslationResult,
        tu: TranslationUnitDeclaration,
        mce: MemberCallExpression,
        handler: HttpRequestHandler
    ) {
        val app = result.findApplicationByTU(tu)

        if (mce.name == "mount_proc") {
            var path: String = (mce.arguments.first() as? Literal<*>)?.value as? String ?: "/"

            val func =
                ((mce.arguments[mce.arguments.size - 1] as? CompoundStatementExpression)
                        ?.statement as?
                        DeclarationStatement)
                    ?.singleDeclaration as?
                    FunctionDeclaration

            val req = func?.parameters?.get(0)

            // check, if path is further split
            (func?.body as? CompoundStatement)?.statements?.forEach { statement ->
                // just look for the pattern for now
                if (statement is DeclarationStatement &&
                        statement.singleDeclaration is VariableDeclaration
                ) {
                    val init = (statement.singleDeclaration as VariableDeclaration).initializer

                    if (init is MemberCallExpression && init.name == "split") {
                        if (init.base is MemberCallExpression &&
                                (init.base as MemberCallExpression).base is
                                    DeclaredReferenceExpression
                        ) {
                            if (((init.base as MemberCallExpression).base as
                                        DeclaredReferenceExpression)
                                    .refersTo == req
                            ) {
                                path = path.appendPath("{fragment}")
                            }
                        }
                    }
                }
            }

            val endpoint = HttpEndpoint(NoAuthentication(), null, null, "GET", func, path)
            endpoint.name = path

            handler.httpEndpoints.plusAssign(endpoint)
            app?.functionalitys?.plusAssign(endpoint)

            result += endpoint
        }
    }
}
