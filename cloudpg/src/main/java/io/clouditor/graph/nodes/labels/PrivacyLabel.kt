package io.clouditor.graph.nodes.labels

import de.fraunhofer.aisec.cpg.graph.Node
import org.neo4j.ogm.annotation.Relationship

/**
 * Stores an integer to denote a hierarchy of privacy labels, with higher numbers meaning more
 * private labels.
 */
class PrivacyLabel(labeledNode: Node) : DataLabel(labeledNode) {

    @Relationship(value = "PROTECTION_LEVEL", direction = Relationship.Direction.OUTGOING) var protectionlevel: Int = 0

    override fun areMergeable(l: Label): Boolean {
        return l::class == PrivacyLabel::class &&
            protectionlevel == (l as PrivacyLabel).protectionlevel
    }
}
