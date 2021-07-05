package io.clouditor.graph.passes.python

import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.graph.declarations.TranslationUnitDeclaration
import de.fraunhofer.aisec.cpg.graph.statements.expressions.MemberCallExpression
import de.fraunhofer.aisec.cpg.passes.Pass
import de.fraunhofer.aisec.cpg.processing.IVisitor
import de.fraunhofer.aisec.cpg.processing.strategy.Strategy
import io.clouditor.graph.*

class LogPass : Pass() {
    override fun accept(t: TranslationResult) {
        // if (this.lang is PythonLanguageFrontend) {
        for (tu in t.translationUnits) {
            tu.accept(
                Strategy::AST_FORWARD,
                object : IVisitor<Node?>() {
                    fun visit(m: MemberCallExpression) {
                        handleMemberCall(t, m, tu)
                    }
                }
            )
        }
    }

    private fun handleMemberCall(
        t: TranslationResult,
        m: MemberCallExpression,
        tu: TranslationUnitDeclaration
    ) {
        // very simple for now
        if (m.name == "info" && m.base.name == "logging") {
            // get the application this is running in
            val application = t.findApplicationByTU(tu)

            // check, if application runs somewhere with resource logging
            val log =
                application
                    ?.runsOn
                    ?.firstOrNull()
                    ?.nextDFG
                    ?.filterIsInstance<ResourceLogging>()
                    ?.map { it }
                    ?: emptyList()

            val out = LogOutput(log as List<Logging>, m.arguments.firstOrNull(), m)

            // add DFG from expression to sink
            out.to.forEach { out.value.nextDFG.add((it)) }

            t += out
        }
    }

    override fun cleanup() {}
}
