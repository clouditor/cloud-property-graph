package io.clouditor.graph.nodes.labels

import de.fraunhofer.aisec.cpg.graph.Node

class Identifier(labeledNode: Node) : PseudoIdentifier(labeledNode) {
    override fun areMergeable(l: Label): Boolean {
        return l::class == Identifier::class
    }
}
