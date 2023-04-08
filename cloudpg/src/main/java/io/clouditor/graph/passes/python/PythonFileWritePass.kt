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
class PythonFileWritePass: FileWritePass() {
    override fun accept(t: TranslationResult) {
        for (tu in t.translationUnits) {
            tu.accept(
                Strategy::AST_FORWARD,
                object : IVisitor<Node?>() {
                    fun visit(r: MemberCallExpression) {
                        // look for write() call
                        if (r.name == "write") {
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