package io.clouditor.graph.passes.python

import de.fraunhofer.aisec.cpg.ExperimentalPython
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

@ExperimentalPython
class CryptographyPass : Pass() {

    override fun cleanup() {
        // nothing to do
    }

    override fun accept(t: TranslationResult) {
        // if (this.lang is PythonLanguageFrontend) {
        for (tu in t.translationUnits) {
            tu.accept(
                Strategy::AST_FORWARD,
                object : IVisitor<Node?>() {
                    fun visit(r: MemberCallExpression) {
                        // look for key.sign()
                        if (r.name == "sign") {
                            val private_key = r.base as DeclaredReferenceExpression
                            val generator = private_key.prevDFG.first() as MemberCallExpression
                            if (generator.name == "generate_private_key") {
                                handleSignature(tu, t, r)
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
