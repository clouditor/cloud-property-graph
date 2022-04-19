package io.clouditor.graph.passes

import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.graph.statements.expressions.ConstructExpression
import de.fraunhofer.aisec.cpg.helpers.SubgraphWalker
import de.fraunhofer.aisec.cpg.passes.Pass

class BidirectionalEdgesCachePass : Pass() {

    enum class EdgeLabel {
        INSTANTIATES
    }

    val predicatesToHandle: MutableMap<(Node) -> Boolean, Pair<EdgeLabel, (Node) -> List<Node>>> =
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
                { node: Node ->
                    (node as ConstructExpression).instantiates?.let { listOf(it) }
                        ?: emptyList<Node>()
                }
            )
        )

        nodes.forEach { node: Node ->
            predicatesToHandle.forEach { predicate, pair ->
                val targets: List<Node> =
                    if (predicate(node)) {
                        pair.second(node)
                    } else {
                        emptyList<Node>()
                    }
                if (!targets.isEmpty()) {
                    if (!outgoingEdgesCache.containsKey(node))
                        outgoingEdgesCache[node] = mutableMapOf<EdgeLabel, MutableList<Node>>()

                    if (!outgoingEdgesCache[node]!!.containsKey(pair.first))
                        outgoingEdgesCache[node]?.put(pair.first, mutableListOf<Node>())
                    outgoingEdgesCache[node]?.get(pair.first)?.addAll(targets)

                    targets.forEach {
                        if (!incomingEdgesCache.containsKey(it))
                            incomingEdgesCache[it] = mutableMapOf<EdgeLabel, MutableList<Node>>()
                        if (!incomingEdgesCache[it]!!.containsKey(pair.first))
                            incomingEdgesCache[it]?.put(pair.first, mutableListOf<Node>())
                        incomingEdgesCache[it]?.get(pair.first)?.add(node)
                    }
                }
            }
        }
    }

    fun getEdgeTargetOf(source: Node, edgeLabel: EdgeLabel): List<Node>? {
        return incomingEdgesCache.get(source)?.get(edgeLabel)
    }

    fun getEdgeSourceOf(target: Node, edgeLabel: EdgeLabel): List<Node>? {
        return outgoingEdgesCache.get(target)?.get(edgeLabel)
    }

    override fun cleanup() {}
}
