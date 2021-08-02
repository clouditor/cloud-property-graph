package io.clouditor.graph.passes

import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.graph.declarations.ParamVariableDeclaration
import de.fraunhofer.aisec.cpg.helpers.SubgraphWalker
import de.fraunhofer.aisec.cpg.passes.Pass
import io.clouditor.graph.*
import io.clouditor.graph.nodes.labels.PrivacyLabel

class LabelExtractionPass : Pass() {
    override fun accept(t: TranslationResult) {
        // loop through services
        val parameters =
            SubgraphWalker.flattenAST(t).filterIsInstance(ParamVariableDeclaration::class.java)

        for (param in parameters) {
            // look for containers
            handleParamVariableDeclaration(t, param)
        }
    }

    private fun handleParamVariableDeclaration(
        t: TranslationResult,
        param: ParamVariableDeclaration
    ) {

        val label: PrivacyLabel = PrivacyLabel(param)

        label.protectionlevel = 1

        t += label
    }

    override fun cleanup() {}
}
