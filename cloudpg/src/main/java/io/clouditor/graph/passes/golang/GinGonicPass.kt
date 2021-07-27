package io.clouditor.graph.passes.golang

import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.graph.declarations.FunctionDeclaration
import de.fraunhofer.aisec.cpg.graph.declarations.TranslationUnitDeclaration
import de.fraunhofer.aisec.cpg.graph.declarations.VariableDeclaration
import de.fraunhofer.aisec.cpg.graph.statements.expressions.CallExpression
import de.fraunhofer.aisec.cpg.graph.statements.expressions.DeclaredReferenceExpression
import de.fraunhofer.aisec.cpg.graph.statements.expressions.Literal
import de.fraunhofer.aisec.cpg.graph.statements.expressions.MemberCallExpression
import de.fraunhofer.aisec.cpg.passes.Pass
import de.fraunhofer.aisec.cpg.processing.IVisitor
import de.fraunhofer.aisec.cpg.processing.strategy.Strategy
import io.clouditor.graph.*

class GinGonicPass : Pass() {
    private val clients = mutableMapOf<VariableDeclaration, HttpRequestHandler>()

    override fun cleanup() {}

    override fun accept(result: TranslationResult?) {
        if (result != null) {
            // first, look for clients
            for (tu in result.translationUnits) {
                tu.accept(
                    Strategy::AST_FORWARD,
                    object : IVisitor<Node?>() {
                        fun visit(r: VariableDeclaration) {
                            handleVariable(result, tu, r)
                        }
                    }
                )
            }
        }
    }

    private fun handleMemberCall(
        result: TranslationResult,
        tu: TranslationUnitDeclaration,
        m: MemberCallExpression
    ) {
        if (m.base is DeclaredReferenceExpression &&
                clients.containsKey((m.base as DeclaredReferenceExpression).refersTo)
        ) {
            val client = clients[(m.base as DeclaredReferenceExpression).refersTo]

            if (m.name == "GET") {
                val endpoint =
                    HttpEndpoint(
                        NoAuthentication(),
                        null,
                        null,
                        "GET",
                        (m.arguments[1] as? DeclaredReferenceExpression)?.refersTo as?
                            FunctionDeclaration,
                        getPath(m)
                    )
                endpoint.name = endpoint.path

                log.debug("Adding GET to {} - resolved to {}", client?.name, endpoint.handler?.name)

                client?.httpEndpoints?.plusAssign(endpoint)
            } else if (m.name == "Group") {
                // add a new (sub) client
                val app = result.findApplicationByTU(tu)

                val requestHandler =
                    HttpRequestHandler(
                        app,
                        mutableListOf(),
                        client?.path?.appendPath(getPath(m)) ?: "/"
                    )
                requestHandler.name = requestHandler.path

                val subClient = m.nextDFG.filterIsInstance<VariableDeclaration>().firstOrNull()
                subClient?.let {
                    clients[it] = requestHandler

                    log.debug("Adding new group client {}", requestHandler.name)

                    result += requestHandler
                }
            }
        }
    }

    private fun getPath(call: MemberCallExpression): String {
        val literal = call.arguments.firstOrNull() as? Literal<*>

        return if (literal?.value.toString() == "") {
            "/"
        } else {
            literal?.value.toString()
        }
    }

    private fun handleVariable(
        result: TranslationResult,
        tu: TranslationUnitDeclaration,
        r: VariableDeclaration
    ) {
        // check initializers for http.NewServeMux()
        // actually check for return types - but that does not work (yet) with the standard library

        if (r.initializer is CallExpression &&
                (r.initializer as CallExpression).fqn == "gin.Default"
        ) {
            val app = result.findApplicationByTU(tu)

            val requestHandler = HttpRequestHandler(app, mutableListOf(), "/")
            requestHandler.name = requestHandler.path

            clients[r] = requestHandler

            log.debug("Adding new client {}", r.name)

            result += requestHandler

            // look for calls to that client
            r.accept(
                Strategy::EOG_FORWARD,
                object : IVisitor<Node?>() {
                    fun visit(m: MemberCallExpression) {
                        handleMemberCall(result, tu, m)
                    }
                }
            )
        }
    }
}

fun String.appendPath(path: String): String {
    return if (this.endsWith("/") && path.startsWith("/")) {
        this + path.substring(1)
    } else if (!path.startsWith("/")) {
        "$this/$path"
    } else {
        this + path
    }
}
