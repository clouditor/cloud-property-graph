package io.clouditor.graph.passes.python

import de.fraunhofer.aisec.cpg.TranslationContext
import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.graph.statements.expressions.MemberCallExpression
import de.fraunhofer.aisec.cpg.processing.IVisitor
import de.fraunhofer.aisec.cpg.processing.strategy.Strategy
import io.clouditor.graph.*
import io.clouditor.graph.passes.LogPass

class PythonLogPass(ctx: TranslationContext) : LogPass(ctx) {
    override fun accept(result: TranslationResult) {
        // if (this.lang is PythonLanguageFrontend) {
        for (tu in result.translationUnits) {
            tu.accept(
                Strategy::AST_FORWARD,
                object : IVisitor<Node>() {
                    override fun visit(t: Node) {
                        when (t) {
                            is MemberCallExpression -> {
                                if (t.name.localName == "info" &&
                                        t.base?.name?.localName == "logging"
                                ) {
                                    handleLog(result, t, t.name.localName, tu)
                                }
                            }
                        }
                    }
                }
            )
        }
    }
}
