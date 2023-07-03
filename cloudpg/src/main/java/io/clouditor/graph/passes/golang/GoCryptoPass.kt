package io.clouditor.graph.passes.golang

import de.fraunhofer.aisec.cpg.TranslationContext
import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.graph.declarations.TranslationUnitDeclaration
import de.fraunhofer.aisec.cpg.graph.declarations.VariableDeclaration
import de.fraunhofer.aisec.cpg.graph.statements.expressions.*
import de.fraunhofer.aisec.cpg.passes.TranslationResultPass
import de.fraunhofer.aisec.cpg.processing.IVisitor
import de.fraunhofer.aisec.cpg.processing.strategy.Strategy
import io.clouditor.graph.*
import io.clouditor.graph.nodes.Signature

@Suppress("UNUSED_PARAMETER")
class GoCryptoPass(ctx: TranslationContext) : TranslationResultPass(ctx) {

    override fun cleanup() {}

    override fun accept(result: TranslationResult) {
        val translationUnits =
            result.components.stream().flatMap { it.translationUnits.stream() }.toList()
        for (tu in translationUnits) {
            tu.accept(
                Strategy::AST_FORWARD,
                object : IVisitor<Node>() {
                    override fun visit(t: Node) {
                        when (t) {
                            is CallExpression -> handleSign(result, tu, t)
                        }
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
            // FIXME: Safety measures added later; they were not necessary with the previous CPG
            //  version. This can mean that the expected value differs from before (not null/empty).
            val plainText = textToBeSigned.refersTo as? VariableDeclaration
            val signature = Signature(plainText, c.nextDFG.firstOrNull() as? VariableDeclaration)
            t += signature
        }
    }
}
