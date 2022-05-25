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

    override fun accept(result: TranslationResult?) {
        if (result != null) {
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
    }

    private fun handleSign(
        t: TranslationResult,
        tu: TranslationUnitDeclaration,
        c: CallExpression
    ) {
        if (c.fqn == "ed25519.Sign") {
            // the text that is signed is the second argument
            val text_to_be_signedDRE = c.arguments[1] as DeclaredReferenceExpression
            val plain_text = text_to_be_signedDRE.refersTo as VariableDeclaration
            val signature = Signature(plain_text)
            t += signature
        }
    }
}
