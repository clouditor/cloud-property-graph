package io.clouditor.graph.passes.python

import de.fraunhofer.aisec.cpg.TranslationContext
import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.graph.declarations.TranslationUnitDeclaration
import de.fraunhofer.aisec.cpg.graph.evaluate
import de.fraunhofer.aisec.cpg.graph.statements.expressions.*
import de.fraunhofer.aisec.cpg.passes.SymbolResolver
import de.fraunhofer.aisec.cpg.passes.configuration.DependsOn
import de.fraunhofer.aisec.cpg.processing.IVisitor
import de.fraunhofer.aisec.cpg.processing.strategy.Strategy
import io.clouditor.graph.*
import io.clouditor.graph.passes.HttpClientPass
import io.clouditor.graph.testing.LocalTestingPass

@DependsOn(SymbolResolver::class)
@DependsOn(LocalTestingPass::class)
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
                    fun visit(t: CallExpression) {
                        if (t.name.toString() == "requests.get") {
                            handleClientRequest(tu, result, t, "GET")
                        } else if (t.name.toString() == "requests.post") {
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
        r: CallExpression,
        method: String
    ) {
        val app = t.findApplicationByTU(tu)

        val url = r.arguments.first().evaluate()

        createHttpRequest(t, url as String, r, method, r.arguments.getOrNull(1), app)
    }
}
