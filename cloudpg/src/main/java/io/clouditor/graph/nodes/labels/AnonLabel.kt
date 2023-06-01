package io.clouditor.graph.nodes.labels

import de.fraunhofer.aisec.cpg.graph.Node
import org.neo4j.ogm.annotation.Relationship

class AnonLabel(labeledNode: Node) : Label(labeledNode) {
    /**
     * In the future on label will be used to label multiple nodes, with the purpose of havein one
     * unique label of the same type and properties.
     */
    @field:Relationship(value = "ANONYMIZES", direction = Relationship.Direction.OUTGOING)
    var anonymizes: Label? =
        null // We can make this a list if we allow anonlabels to anonymize several
    // labels that are not mergeable between each other

    override fun areMergeable(l: Label): Boolean {
        if (l::class != AnonLabel::class) {
            return false
        }
        (l as AnonLabel).anonymizes?.let {
            return anonymizes?.areMergeable(it) ?: false
        }
        return false
    }

    fun canAnonymize(l: Label): Boolean {
        return anonymizes?.areMergeable(l) ?: false
    }

    fun addAnonymize(l: Label) {
        if (anonymizes?.labeledNodes?.isEmpty() != false) {
            anonymizes = l
        } else {
            anonymizes!!.mergeWith(l)
        }
    }
}
