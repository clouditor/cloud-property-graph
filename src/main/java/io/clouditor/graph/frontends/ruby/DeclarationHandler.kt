package io.clouditor.graph.frontends.ruby

import de.fraunhofer.aisec.cpg.frontends.Handler
import de.fraunhofer.aisec.cpg.graph.NodeBuilder
import de.fraunhofer.aisec.cpg.graph.declarations.Declaration
import de.fraunhofer.aisec.cpg.graph.types.UnknownType
import org.jruby.ast.*

class DeclarationHandler(lang: RubyLanguageFrontend) :
    Handler<Declaration, Node, RubyLanguageFrontend>({ Declaration() }, lang) {

    init {
        map.put(ArgumentNode::class.java, ::handleArgumentNode)
    }

    private fun handleArgumentNode(node: Node?): Declaration? {
        if (node !is ArgumentNode) {
            return null
        }

        val param =
            NodeBuilder.newMethodParameterIn(
                node.name.idString(),
                UnknownType.getUnknownType(),
                false,
                lang.getCodeFromRawNode(node)
            )

        return param
    }
}
