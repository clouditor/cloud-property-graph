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
                                    val private_key = t.base as DeclaredReferenceExpression
                                    val generator = private_key.prevDFG.first() as MemberCallExpression
                                    if (generator.name.localName == "generate_private_key") {
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
        val text_to_be_signed_expression = mce.arguments.first() as DeclaredReferenceExpression
        val plain_text = text_to_be_signed_expression.refersTo as VariableDeclaration
        val signature = Signature(plain_text, mce.nextDFG.first() as VariableDeclaration)
        t += signature
    }
}
