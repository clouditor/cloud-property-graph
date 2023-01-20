package io.clouditor.graph.passes.golang

import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.graph.declarations.TranslationUnitDeclaration
import de.fraunhofer.aisec.cpg.graph.declarations.VariableDeclaration
import de.fraunhofer.aisec.cpg.graph.statements.expressions.*
import de.fraunhofer.aisec.cpg.passes.Pass
import de.fraunhofer.aisec.cpg.processing.IVisitor
import de.fraunhofer.aisec.cpg.processing.strategy.Strategy
import io.clouditor.graph.*
import io.clouditor.graph.nodes.Signature

class GoCryptoPass : Pass() {

    override fun cleanup() {}

    override fun accept(result: TranslationResult) {
        for (tu in result.translationUnits) {
            tu.accept(
                Strategy::AST_FORWARD,
                object : IVisitor<Node?>() {
                    fun visit(c: CallExpression) {
                        handleSign(result, tu, c)
                    }
                }
            )
        }
    }

    private fun handleSign(
        t: TranslationResult,
        tu: TranslationUnitDeclaration,
        c: CallExpression
    ) {
        if (c.name.toString() == "ed25519.Sign") {
            // the text that is signed is the second argument
            val textToBeSigned = c.arguments[1] as DeclaredReferenceExpression
            val plainText = textToBeSigned.refersTo as VariableDeclaration
            val signature = Signature(plainText, c.nextDFG.first() as VariableDeclaration)
            t += signature
        }
    }
}
