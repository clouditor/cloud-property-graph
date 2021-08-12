package io.clouditor.graph.passes.python

import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.graph.statements.expressions.MemberCallExpression
import de.fraunhofer.aisec.cpg.processing.IVisitor
import de.fraunhofer.aisec.cpg.processing.strategy.Strategy
import io.clouditor.graph.Application
import io.clouditor.graph.findApplicationByTU
import io.clouditor.graph.passes.DatabaseOperationPass

class Psycopg2Pass : DatabaseOperationPass() {
    override fun accept(t: TranslationResult) {
        for (tu in t.translationUnits) {
            val app = t.findApplicationByTU(tu)

            t.accept(
                Strategy::AST_FORWARD, // actually we want to have EOG_FORWARD, but that doesn't
                // work
                object : IVisitor<Node?>() {
                    fun visit(call: MemberCallExpression) {
                        if (call.name == "connect") {
                            handleConnect(t, call, app)
                        }
                    }
                }
            )
        }
    }

    private fun handleConnect(
        t: TranslationResult,
        call: MemberCallExpression,
        app: Application?
    ) {}

    override fun cleanup() {
        // nothing to do
    }
}
