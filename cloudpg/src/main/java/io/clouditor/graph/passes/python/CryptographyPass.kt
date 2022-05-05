package io.clouditor.graph.passes.python

import de.fraunhofer.aisec.cpg.ExperimentalPython
import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.graph.declarations.TranslationUnitDeclaration
import de.fraunhofer.aisec.cpg.graph.statements.expressions.*
import de.fraunhofer.aisec.cpg.passes.Pass
import de.fraunhofer.aisec.cpg.processing.IVisitor
import de.fraunhofer.aisec.cpg.processing.strategy.Strategy
import io.clouditor.graph.*

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
                        if (r.name == "sign" &&
                                ((r.base as DeclaredReferenceExpression).prevDFG as
                                        MemberExpression)
                                    .name == "generate_private_key"
                        ) {
                            handleSignature(tu, t, r)
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
        r: MemberCallExpression
    ) {
        val app = t.findApplicationByTU(tu)

        // TODO add integrity protection to ontology and then as a node to the datum?
    }
}
