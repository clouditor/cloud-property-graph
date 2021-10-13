package io.clouditor.graph.passes

import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.graph.statements.expressions.ConstructExpression
import de.fraunhofer.aisec.cpg.helpers.SubgraphWalker
import de.fraunhofer.aisec.cpg.passes.Pass
import java.util.function.Predicate

class BidirectionalEdgesCachePass : Pass() {

    enum class EdgeLabel {
        INSTANTIATES
    }

    val predicatesToHandle: MutableMap<Predicate<Node>, Pair<EdgeLabel, (Node) -> List<Node>>> =
        mutableMapOf()

    val outgoingEdgesCache: MutableMap<Node, MutableMap<EdgeLabel, MutableList<Node>>> =
        mutableMapOf()
    val incomingEdgesCache: MutableMap<Node, MutableMap<EdgeLabel, MutableList<Node>>> =
        mutableMapOf()

    override fun accept(t: TranslationResult) {
        // loop through services
        val nodes = SubgraphWalker.flattenAST(t)

        // Register default extractor that gets Label from Annotation
        predicatesToHandle.put(
            { node -> node is ConstructExpression },
            Pair(
                EdgeLabel.INSTANTIATES,
                { node: Node -> listOf((node as ConstructExpression).instantiates as Node) }
            )
        )

        nodes.forEach { node: Node ->
            predicatesToHandle.forEach { predicate, pair ->
                val targets: List<Node> =
                    if (predicate.test(node)) {
                        pair.second(node)
                    } else {
                        emptyList<Node>()
                    }
                if (!targets.isEmpty()) {
                    val outgoingMap: MutableMap<EdgeLabel, MutableList<Node>> =
                        outgoingEdgesCache[node]
                            ?: outgoingEdgesCache.put(
                                node,
                                mutableMapOf<EdgeLabel, MutableList<Node>>()
                            )!!

                    val outgoingList: MutableList<Node> =
                        outgoingMap.get(pair.first)
                            ?: outgoingMap.put(pair.first, mutableListOf<Node>())!!

                    outgoingList.addAll(targets)

                    targets.forEach {
                        val incomingMap: MutableMap<EdgeLabel, MutableList<Node>> =
                            incomingEdgesCache[it]
                                ?: incomingEdgesCache.put(
                                    it,
                                    mutableMapOf<EdgeLabel, MutableList<Node>>()
                                )!!
                        val incomingList: MutableList<Node> =
                            incomingMap.get(pair.first)
                                ?: outgoingMap.put(pair.first, mutableListOf<Node>())!!
                        incomingList.add(node)
                    }
                }
            }
        }
    }

    fun getEdgeTargetOf(source: Node, edgeLabel: EdgeLabel): List<Node>? {
        return outgoingEdgesCache.get(Node)?.get(edgeLabel)
    }

    fun getEdgeSourceOf(target: Node, edgeLabel: EdgeLabel): List<Node>? {
        return outgoingEdgesCache.get(Node)?.get(edgeLabel)
    }

    override fun cleanup() {}
}
