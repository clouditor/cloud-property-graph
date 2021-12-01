package io.clouditor.graph.nodes.labels

import de.fraunhofer.aisec.cpg.graph.Node

open class PseudoIdentifier(labeledNode: Node) : DataLabel(labeledNode) {
    override fun areMergeable(l: Label): Boolean {
        return l::class == PseudoIdentifier::class
    }
}
