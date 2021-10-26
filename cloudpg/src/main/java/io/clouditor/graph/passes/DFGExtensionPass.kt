package io.clouditor.graph.passes

import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.graph.statements.expressions.MemberExpression
import de.fraunhofer.aisec.cpg.helpers.SubgraphWalker
import de.fraunhofer.aisec.cpg.passes.Pass
import io.clouditor.graph.plusAssign

/**
 * The purpose of this Pass os tp enhance the DFG graph with additional edges that are needed for
 * DF-Label tracking. One such case is the labeling of a variable that is the base of a member
 * expression. Normally no data flows from the base to the member expression. FOr this use case,
 * however, the mere usage of a base causes labels to be relevant for the member expression.
 */
class DFGExtensionPass : Pass() {

    override fun accept(t: TranslationResult) {
        // loop through services
        val nodes = SubgraphWalker.flattenAST(t)
        val memberExpressions = nodes.filterIsInstance<MemberExpression>()

        memberExpressions.forEach { it.addPrevDFG(it.base) }
    }

    override fun cleanup() {}
}
