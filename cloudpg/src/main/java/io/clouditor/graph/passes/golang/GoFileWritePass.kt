package io.clouditor.graph.passes.python

import de.fraunhofer.aisec.cpg.ExperimentalPython
import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.graph.statements.expressions.MemberCallExpression
import de.fraunhofer.aisec.cpg.processing.IVisitor
import de.fraunhofer.aisec.cpg.processing.strategy.Strategy
import io.clouditor.graph.findApplicationByTU
import io.clouditor.graph.passes.FileWritePass

@ExperimentalPython
class GoFileWritePass: FileWritePass() {
    override fun accept(t: TranslationResult) {
        for (tu in t.translationUnits) {
            tu.accept(
                Strategy::AST_FORWARD,
                object : IVisitor<Node?>() {
                    // check all MemberCallExpressions
                    fun visit(r: MemberCallExpression) {
                        // look for writeFile() call of os library
                        if (r.name == "WriteFile" && r.base.name == "os") {
                            createFileWrite(t, r, t.findApplicationByTU(tu))
                        }
                    }
                }
            )
        }
    }

    override fun cleanup() {
        // Nothing to do
    }

}