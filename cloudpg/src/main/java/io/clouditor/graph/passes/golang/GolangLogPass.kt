package io.clouditor.graph.passes.golang

import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.graph.declarations.TranslationUnitDeclaration
import de.fraunhofer.aisec.cpg.graph.statements.expressions.MemberCallExpression
import de.fraunhofer.aisec.cpg.passes.Pass
import de.fraunhofer.aisec.cpg.processing.IVisitor
import de.fraunhofer.aisec.cpg.processing.strategy.Strategy
import io.clouditor.graph.*
import io.clouditor.graph.passes.LogPass

class GolangLogPass : LogPass() {
    override fun accept(t: TranslationResult) {
        for (tu in t.translationUnits) {
            tu.accept(
                Strategy::AST_FORWARD,
                object : IVisitor<Node?>() {
                    fun visit(m: MemberCallExpression) {
                        // TODO missing newEvent
                        val logMethods =
                                arrayOf(
                                        "log.Info()",
                                        "log.Debug()",
                                        "log.Trace()",
                                        "log.Warn()",
                                        "log.Error()",
                                        "log.log()"
                                )
                        // very simple for now, the logging library needs to be called "log"
                        // we assume that this is going to std out, resulting in log collection in k8s; the
                        // kubernetes resource is only connected if it found
                        if (m.base.code in logMethods) {
                            handleLog(t, m, tu)
                        }
                    }
                }
            )
        }
    }

}
