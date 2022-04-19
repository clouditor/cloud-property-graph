package io.clouditor.graph.passes.golang

import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.graph.declarations.FunctionDeclaration
import de.fraunhofer.aisec.cpg.graph.declarations.TranslationUnitDeclaration
import de.fraunhofer.aisec.cpg.graph.declarations.VariableDeclaration
import de.fraunhofer.aisec.cpg.graph.statements.expressions.*
import de.fraunhofer.aisec.cpg.graph.types.PointerType
import de.fraunhofer.aisec.cpg.passes.Pass
import de.fraunhofer.aisec.cpg.processing.IVisitor
import de.fraunhofer.aisec.cpg.processing.strategy.Strategy
import io.clouditor.graph.HttpRequest
import io.clouditor.graph.*

class GolangHttpPass : Pass() {
    private val clients = mutableMapOf<VariableDeclaration, HttpRequestHandler>()

    override fun cleanup() {}

    override fun accept(result: TranslationResult) {
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

            // then for the member calls
            for (tu in result.translationUnits) {
                tu.accept(
                    Strategy::AST_FORWARD,
                    object : IVisitor<Node?>() {
                        fun visit(m: MemberCallExpression) {
                            handleMemberCall(result, tu, m)
                        }
                    }
                )
            }

            // look for http call expressions in the client code
            for (tu in result.translationUnits) {
                tu.accept(
                    Strategy::AST_FORWARD,
                    object : IVisitor<Node?>() {
                        fun visit(m: CallExpression) {
                            handleCallExpression(result, tu, m)
                        }
                    }
                )
            }
        }
    }

    private fun handleMemberCall(
        result: TranslationResult,
        tu: TranslationUnitDeclaration?,
        m: MemberCallExpression
    ) {
        if (m.base is DeclaredReferenceExpression &&
                clients.containsKey((m.base as DeclaredReferenceExpression).refersTo)
        ) {
            val client = clients[(m.base as DeclaredReferenceExpression).refersTo]

            val funcDeclaration =
                (m.arguments[1] as? DeclaredReferenceExpression)?.refersTo as? FunctionDeclaration
            val literal = m.arguments.first() as? Literal<*>
            literal.let {
                val endpoint =
                    HttpEndpoint(
                        NoAuthentication(),
                        funcDeclaration,
                        "GET",
                        literal?.value as String?,
                        null,
                        null
                    )
                endpoint.name = endpoint.path
                funcDeclaration?.parameters?.forEach {
                    if (it.type is PointerType && it.type.name == "http.Request*" ||
                            it.type is HttpRequest
                    ) {
                        // add a dfg from the endpoint to the paramvariabledeclaration the data is
                        // stored in
                        endpoint.addNextDFG(it)
                    }
                }
                client?.httpEndpoints?.plusAssign(endpoint)
            }
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
                (r.initializer as CallExpression).fqn == "http.NewServeMux"
        ) {
            val app = result.findApplicationByTU(tu)

            val requestHandler = HttpRequestHandler(app, mutableListOf(), "/")
            requestHandler.name = requestHandler.path

            clients[r] = requestHandler

            result += requestHandler
        }
    }

    private fun handleCallExpression(
        result: TranslationResult,
        tu: TranslationUnitDeclaration,
        c: CallExpression
    ) {
        val app = result.findApplicationByTU(tu)
        var requestFunction = c.invokes as FunctionDeclaration
        // should also have c.base.name == "http" but this is not parsed correctly atm
        if (c.name == "PostForm") {
            var request = createHttpRequest(
                    result,
                    requestFunction.parameters.first() as String,
                    c,
                    "POST",
                    requestFunction.parameters[1],
                    app
            )
        }
    }
}
