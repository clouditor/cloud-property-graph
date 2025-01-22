package io.clouditor.graph.nodes.labels

import de.fraunhofer.aisec.cpg.graph.Node
import org.neo4j.ogm.annotation.Relationship

/** Generic label attached to one ore multiple nodes. */
open class Label constructor(labeledNode: Node) : Node() {

    /**
     * In the future on label will be used to label multiple nodes, with the purpose of havein one
     * unique label of the same type and properties.
     */
    @field:Relationship(value = "LABELEDNODE", direction = Relationship.Direction.OUTGOING)
    var labeledNodes: MutableList<Node> = mutableListOf(labeledNode)

    open fun areMergeable(l: Label): Boolean {
        return true
    }

    open fun mergeWith(l: Label) {
        labeledNodes.addAll(l.labeledNodes)
    }
}
