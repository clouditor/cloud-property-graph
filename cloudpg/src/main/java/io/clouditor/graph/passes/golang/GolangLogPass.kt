package io.clouditor.graph.passes.golang

import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.graph.statements.expressions.CallExpression
import de.fraunhofer.aisec.cpg.graph.statements.expressions.MemberCallExpression
import de.fraunhofer.aisec.cpg.processing.IVisitor
import de.fraunhofer.aisec.cpg.processing.strategy.Strategy
import io.clouditor.graph.passes.LogPass

class GolangLogPass : LogPass() {
    override fun accept(t: TranslationResult) {
        for (tu in t.translationUnits) {
            tu.accept(
                    Strategy::AST_FORWARD,
                    object : IVisitor<Node?>() {
                        fun visit(m: MemberCallExpression) {
                            val logMethods =
                                    arrayOf(
                                            "log.Info",
                                            "log.Debug",
                                            "log.Trace",
                                            "log.Warn",
                                            "log.Err",
                                    )
                            // we are looking for calls to Msg or Msgf, which have a base of one of the logging
                            // specifiers above, e.g. log.Info().Msg("Hello")
                            if ((m.name == "Msg" || m.name == "Msgf") &&
                                    (m.base as? CallExpression)?.fqn in logMethods
                            ) {
                                // the base name specifies the log severity, so we use this one as the
                                // "name" of the log operation
                                handleLog(t, m, m.base.name, tu)
                            }
                        }
                    }
            )
        }
    }
}
