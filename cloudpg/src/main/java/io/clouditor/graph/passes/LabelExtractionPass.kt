package io.clouditor.graph.passes

import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.graph.declarations.ParamVariableDeclaration
import de.fraunhofer.aisec.cpg.graph.statements.expressions.Expression
import de.fraunhofer.aisec.cpg.graph.statements.expressions.Literal
import de.fraunhofer.aisec.cpg.helpers.SubgraphWalker
import de.fraunhofer.aisec.cpg.passes.Pass
import io.clouditor.graph.nodes.labels.Label
import io.clouditor.graph.nodes.labels.PrivacyLabel
import io.clouditor.graph.plusAssign
import java.util.function.Consumer
import java.util.function.Predicate
import java.util.stream.Collectors

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
                        val label: PrivacyLabel =
                            addLabelToDFGBorderEdges<PrivacyLabel>(annotationParent)
                        label.protectionlevel = it.value
                        t += label
                    }
                }
            }
    }

    inline fun <reified T : Label> addLabelToAnnotatedNode(n: Node): T {
        var label: T = T::class.constructors.first().call(n)
        label.labeledNodes.add(n)
        return label
    }

    inline fun <reified T : Label> addLabelToDFGBorderEdges(n: Node): T {
        var label: T = T::class.constructors.first().call(n)

        var dfgExitNodes: MutableList<Node> = getDFGPathEdges(n)!!.exits

        label.labeledNodes.addAll(dfgExitNodes)

        return label
    }

    private fun handleParamVariableDeclaration(
        t: TranslationResult,
        param: ParamVariableDeclaration
    ) {

        val label: PrivacyLabel = PrivacyLabel(param)

        label.protectionlevel = 1

        t += label
    }

    /**
     * Function returns two lists in a list. The first list contains all dfg nodes with no
     * predecesor in the subgraph with root 'n'. The second list contains dfg edges that have no
     * successor in the subgraph with root 'n'. The first List marks the entry and the second marks
     * the exit nodes of the dfg in this subgraph.
     *
     * @param n
     * - root of the subgraph.
     * @return Two lists, list 1 contains all dfg entries and list 2 contains all exits.
     */
    fun getDFGPathEdges(n: Node?): SubgraphWalker.Border? {
        var border = SubgraphWalker.Border()
        var flattedASTTree = SubgraphWalker.flattenAST(n)
        var dfgNodes =
            flattedASTTree
                .stream()
                .filter { node: Node -> !node.prevDFG.isEmpty() || !node.nextDFG.isEmpty() }
                .collect(Collectors.toList())
        // Nodes that are incoming edges, no other node
        border.entries.addAll(
            dfgNodes
                .stream()
                .filter { node: Node ->
                    node.prevDFG.stream().anyMatch { prev: Node -> !dfgNodes.contains(prev) }
                }
                .collect(Collectors.toList())
        )
        border.exits.addAll(
            dfgNodes
                .stream()
                .filter { node: Node ->
                    node.nextDFG.stream().anyMatch { next: Node -> !dfgNodes.contains(next) }
                }
                .collect(Collectors.toList())
        )
        return border
    }

    override fun cleanup() {}
}
