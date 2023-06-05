package io.clouditor.graph.frontends.ruby

import de.fraunhofer.aisec.cpg.frontends.Handler
import de.fraunhofer.aisec.cpg.graph.NodeBuilder
import de.fraunhofer.aisec.cpg.graph.declarations.Declaration
import de.fraunhofer.aisec.cpg.graph.declarations.ProblemDeclaration
import de.fraunhofer.aisec.cpg.graph.types.UnknownType
import org.jruby.ast.*

class DeclarationHandler(lang: RubyLanguageFrontend) :
    Handler<Declaration, Node, RubyLanguageFrontend>({ ProblemDeclaration() }, lang) {

    init {
        map.put(ArgumentNode::class.java, ::handleArgumentNode)
    }

    private fun handleArgumentNode(node: Node?): Declaration? {
        if (node !is ArgumentNode) {
            return null
        }

        // FIXME: where do we get our Declaration from?
        val param =
            NodeBuilder.newMethodParameterIn(
                node.name.idString(),
                UnknownType.getUnknownType(),
                false,
                lang.getCodeFromRawNode(node)
            )
        // Something like this? How do I actually access the builder methods?
        // val param = DeclarationBuilder.newMethodDeclaration(node.name.idString())

        return param
    }
}
