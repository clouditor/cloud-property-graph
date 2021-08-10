package io.clouditor.graph.passes

import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.graph.declarations.ParamVariableDeclaration
import de.fraunhofer.aisec.cpg.graph.statements.expressions.Expression
import de.fraunhofer.aisec.cpg.graph.statements.expressions.Literal
import de.fraunhofer.aisec.cpg.helpers.SubgraphWalker
import de.fraunhofer.aisec.cpg.passes.Pass
import io.clouditor.graph.*
import io.clouditor.graph.nodes.labels.PrivacyLabel
import java.util.function.Consumer
import java.util.function.Predicate

class LabelExtractionPass : Pass() {

    val predicatesToHandle: MutableMap<Predicate<Node>, Consumer<Node>> = mutableMapOf()

    override fun accept(t: TranslationResult) {
        // loop through services
        val nodes = SubgraphWalker.flattenAST(t)

        // Register default extractor that gets Label from Annotation
        predicatesToHandle.put(
            { node -> node.annotations.count() > 0 },
            { node -> handleAnnotations(t, node) }
        )

        nodes.forEach { node: Node ->
            predicatesToHandle.forEach { predicate, handler ->
                when (predicate.test(node)) {
                    true -> handler.accept(node)
                }
            }
        }
    }

    private fun handleAnnotations(t: TranslationResult, annotationParent: Node) {
        annotationParent.annotations
            .filter { annotation -> annotation.name == "PrivacyLabel" }
            .forEach {
                val values: List<Expression> =
                    it.members.filter { member -> member.name == "level" }.map { member ->
                        member.value
                    }
                if (!values.isEmpty()) {
                    val literal: Literal<Int>? = values.get(0) as? Literal<Int>
                    literal?.let {
                        val label = PrivacyLabel(annotationParent)
                        label.protectionlevel = it.value
                        t += label
                    }
                }
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
