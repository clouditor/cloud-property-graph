package io.clouditor.graph.passes.python

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

class CryptographyPass(ctx: TranslationContext) : TranslationResultPass(ctx) {

    override fun cleanup() {
        // nothing to do
    }

    override fun accept(result: TranslationResult) {
        // if (this.lang is PythonLanguageFrontend) {
        for (tu in result.translationUnits) {
            tu.accept(
                Strategy::AST_FORWARD,
                object : IVisitor<Node>() {
                    override fun visit(t: Node) {
                        when (t) {
                            is MemberCallExpression -> {
                                // look for key.sign()
                                if (t.name.localName == "sign") {
                                    val privateKey = t.base as DeclaredReferenceExpression
                                    // FIXME: Safety measures added later; they were not necessary with the previous CPG version.
                                    // FIXME: This can mean that the expected value differs from before (not null/empty).
                                    val generator =
                                        privateKey.prevDFG.firstOrNull() as? MemberCallExpression
                                    if (generator?.name?.localName == "generate_private_key") {
                                        handleSignature(tu, result, t)
                                    }
                                }
                            }
                        }
                    }
                }
            )
        }
        // }
    }

    private fun handleSignature(
        tu: TranslationUnitDeclaration,
        t: TranslationResult,
        mce: MemberCallExpression
    ) {
        // TODO check if it is always the first one
        val textToBeSignedExpression = mce.arguments.first() as DeclaredReferenceExpression
        val plainText = textToBeSignedExpression.refersTo as VariableDeclaration
        val signature = Signature(plainText, mce.nextDFG.first() as VariableDeclaration)
        t += signature
    }
}
