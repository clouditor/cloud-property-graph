package io.clouditor.graph.nodes.labels

import de.fraunhofer.aisec.cpg.graph.Node

/** Generic property Label used to store labels defined by arbitrary annotations */
class GenericLabel(labeledNode: Node) : Label(labeledNode) {

    val properties: MutableMap<String, String> = mutableMapOf()
}
