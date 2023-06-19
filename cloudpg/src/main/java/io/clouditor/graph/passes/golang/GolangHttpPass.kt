package io.clouditor.graph.passes.golang

import de.fraunhofer.aisec.cpg.TranslationContext
import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.graph.Name
import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.graph.declarations.FunctionDeclaration
import de.fraunhofer.aisec.cpg.graph.declarations.TranslationUnitDeclaration
import de.fraunhofer.aisec.cpg.graph.declarations.VariableDeclaration
import de.fraunhofer.aisec.cpg.graph.statements.expressions.*
import de.fraunhofer.aisec.cpg.graph.types.PointerType
import de.fraunhofer.aisec.cpg.processing.IVisitor
import de.fraunhofer.aisec.cpg.processing.strategy.Strategy
import io.clouditor.graph.*
import io.clouditor.graph.passes.HttpClientPass

class GolangHttpPass(ctx: TranslationContext) : HttpClientPass(ctx) {
    private val clients = mutableMapOf<VariableDeclaration, HttpRequestHandler>()

    override fun cleanup() {}

    override fun accept(result: TranslationResult) {

        for (tu in result.translationUnits) {
            tu.accept(
                Strategy::AST_FORWARD,
                object : IVisitor<Node>() {
                    override fun visit(t: Node) {
                        when (t) {
                            // first, look for clients
                            is VariableDeclaration -> handleVariable(result, tu, t)
                            // then for the member calls
                            is MemberCallExpression -> handleMemberCall(result, tu, t)
                        }
                    }
                }
            )
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
                endpoint.name = Name(endpoint.path)
                funcDeclaration?.parameters?.forEach {
                    if (it.type is PointerType && it.type.name.localName == "http.Request*" ||
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
                (r.initializer as CallExpression).toString() == "http.NewServeMux"
        ) {
            val app = result.findApplicationByTU(tu)

            val requestHandler = HttpRequestHandler(app, mutableListOf(), "/")
            requestHandler.name = Name(requestHandler.path)

            clients[r] = requestHandler

            result += requestHandler
        }
    }
}
