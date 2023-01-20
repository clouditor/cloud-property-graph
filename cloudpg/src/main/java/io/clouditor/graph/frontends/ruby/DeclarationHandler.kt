package io.clouditor.graph.frontends.ruby

import de.fraunhofer.aisec.cpg.frontends.Handler
import de.fraunhofer.aisec.cpg.graph.declarations.Declaration
import de.fraunhofer.aisec.cpg.graph.declarations.ProblemDeclaration
import de.fraunhofer.aisec.cpg.graph.newParamVariableDeclaration
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

        val param =
            newParamVariableDeclaration(
                node.name.idString(),
                UnknownType.getUnknownType(),
                false,
                frontend.getCodeFromRawNode(node)
            )

        return param
    }
}
