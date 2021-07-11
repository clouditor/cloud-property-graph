package io.clouditor.graph.passes.js

import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.graph.declarations.TranslationUnitDeclaration
import de.fraunhofer.aisec.cpg.graph.statements.expressions.CallExpression
import de.fraunhofer.aisec.cpg.processing.IVisitor
import de.fraunhofer.aisec.cpg.processing.strategy.Strategy
import io.clouditor.graph.passes.HttpClientPass

class FetchPass : HttpClientPass() {
    override fun accept(t: TranslationResult) {
        for (tu in t.translationUnits) {
            tu.accept(
                Strategy::AST_FORWARD,
                object : IVisitor<Node?>() {
                    fun visit(v: CallExpression) {
                        handleCallExpression(t, tu, v)
                    }
                }
            )
        }
    }

    private fun handleCallExpression(
        t: TranslationResult,
        tu: TranslationUnitDeclaration?,
        v: CallExpression
    ) {
        if (v.name == "fetch") {
            println(v)
        }
    }

    override fun cleanup() {
        // nothing to do
    }
}
