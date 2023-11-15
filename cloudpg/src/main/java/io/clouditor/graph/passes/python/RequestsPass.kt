package io.clouditor.graph.passes.python

import de.fraunhofer.aisec.cpg.TranslationContext
import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.graph.declarations.TranslationUnitDeclaration
import de.fraunhofer.aisec.cpg.graph.statements.expressions.*
import de.fraunhofer.aisec.cpg.passes.SymbolResolver
import de.fraunhofer.aisec.cpg.passes.order.DependsOn
import de.fraunhofer.aisec.cpg.processing.IVisitor
import de.fraunhofer.aisec.cpg.processing.strategy.Strategy
import io.clouditor.graph.*
import io.clouditor.graph.passes.HttpClientPass

@DependsOn(SymbolResolver::class)
class RequestsPass(ctx: TranslationContext) : HttpClientPass(ctx) {

    override fun cleanup() {
        // nothing to do
    }

    override fun accept(result: TranslationResult) {
        // if (this.lang is PythonLanguageFrontend) {
        val translationUnits =
            result.components.stream().flatMap { it.translationUnits.stream() }.toList()
        for (tu in translationUnits) {
            tu.accept(
                Strategy::AST_FORWARD,
                object : IVisitor<Node>() {
                    fun visit(t: MemberCallExpression) {
                        // look for requests.get()
                        if (t.name.localName == "get" && t.base?.name?.localName == "requests") {
                            handleClientRequest(tu, result, t, "GET")
                        } else if (t.name.localName == "post" &&
                                t.base?.name?.localName == "requests"
                        ) {
                            handleClientRequest(tu, result, t, "POST")
                        }
                    }
                }
            )
        }
        // }
    }

    private fun handleClientRequest(
        tu: TranslationUnitDeclaration,
        t: TranslationResult,
        r: MemberCallExpression,
        method: String
    ) {
        val app = t.findApplicationByTU(tu)

        val url = PythonValueResolver(app).resolve(r.arguments.first())

        createHttpRequest(t, url as String, r, method, r.arguments.getOrNull(1), app)
    }
}
