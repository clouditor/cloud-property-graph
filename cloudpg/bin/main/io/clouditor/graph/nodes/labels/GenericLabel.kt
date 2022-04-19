package io.clouditor.graph.nodes.labels

import de.fraunhofer.aisec.cpg.graph.Node

/** Generic property Label used to store labels defined by arbitrary annotations */
class GenericLabel(labeledNode: Node) : DataLabel(labeledNode) {

    val properties: MutableMap<String, String> = mutableMapOf()

    override fun areMergeable(l: Label): Boolean {
        return l::class == GenericLabel::class &&
            properties.entries.equals((l as GenericLabel).properties)
    }
}
