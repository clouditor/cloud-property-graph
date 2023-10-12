package io.clouditor.graph.passes.python

import de.fraunhofer.aisec.cpg.TranslationContext
import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.graph.declarations.TranslationUnitDeclaration
import de.fraunhofer.aisec.cpg.graph.declarations.VariableDeclaration
import de.fraunhofer.aisec.cpg.graph.statements.expressions.*
import de.fraunhofer.aisec.cpg.passes.CallResolver
import de.fraunhofer.aisec.cpg.passes.TranslationResultPass
import de.fraunhofer.aisec.cpg.passes.VariableUsageResolver
import de.fraunhofer.aisec.cpg.passes.order.DependsOn
import de.fraunhofer.aisec.cpg.processing.IVisitor
import de.fraunhofer.aisec.cpg.processing.strategy.Strategy
import io.clouditor.graph.*
import io.clouditor.graph.nodes.Signature

@Suppress("UNUSED_PARAMETER")
@DependsOn(CallResolver::class)
@DependsOn(VariableUsageResolver::class)
class CryptographyPass(ctx: TranslationContext) : TranslationResultPass(ctx) {

    override fun cleanup() {
        // nothing to do
    }

    override fun accept(result: TranslationResult) {
        // if (this.lang is PythonLanguageFrontend) {
        val translationUnits =
            result.components.stream().flatMap { it.translationUnits.stream() }.toList()
        for (tu in translationUnits) {
            tu.accept(
                Strategy::AST_FORWARD,
                object : IVisitor<Node>() {
                    fun visit(t: MemberCallExpression) {
                        // look for key.sign()
                        if (t.name.localName == "sign") {
                            val privateKey = t.base as Reference
                            // FIXME: As with the other issues, the DeclaredReferenceExpression is
                            //  missing its target (refersTo)
                            val generator =
                                (privateKey.prevDFG.firstOrNull() as? VariableDeclaration)
                                    ?.initializer as?
                                    CallExpression
                            if (generator?.name?.localName == "generate_private_key") {
                                handleSignature(tu, result, t)
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
        val textToBeSignedExpression = mce.arguments.first() as Reference
        val plainText = textToBeSignedExpression.refersTo as VariableDeclaration
        val signature = Signature(plainText, mce.nextDFG.first() as VariableDeclaration)
        t += signature
    }
}
