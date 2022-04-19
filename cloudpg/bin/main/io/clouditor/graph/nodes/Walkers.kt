package io.clouditor.graph.nodes

import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.graph.edge.PropertyEdge

fun Node.followEOG(predicate: (PropertyEdge<*>) -> Boolean): List<PropertyEdge<*>>? {
    val path = mutableListOf<PropertyEdge<*>>()

    for (edge in this.nextEOGEdges) {
        path.add(edge)

        if (predicate(edge)) {
            return path
        }

        val subPath = edge.end.followEOG(predicate)
        if (subPath != null) {
            path.addAll(subPath)

            return path
        }
    }

    return null
}

fun Node.followDFGReverse(predicate: (Pair<Node, Node>) -> Boolean): List<Pair<Node, Node>>? {
    val path = mutableListOf<Pair<Node, Node>>()

    for (edge in this.prevDFG) {
        val pair = Pair(this, edge)
        path.add(pair)

        if (predicate(pair)) {
            return path
        }

        val subPath = edge.followDFGReverse(predicate)
        if (subPath != null) {
            path.addAll(subPath)

            return path
        }
    }

    return null
}
