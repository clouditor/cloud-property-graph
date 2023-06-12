package io.clouditor.graph.frontends.ruby

import de.fraunhofer.aisec.cpg.frontends.Handler
import de.fraunhofer.aisec.cpg.graph.declarations.Declaration
import de.fraunhofer.aisec.cpg.graph.declarations.ProblemDeclaration
import de.fraunhofer.aisec.cpg.graph.newMethodDeclaration
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

        return newMethodDeclaration(node.name.idString(), language.code, false)
    }
}
