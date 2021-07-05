package io.clouditor.graph.frontends.js

import de.fraunhofer.aisec.cpg.frontends.Handler
import de.fraunhofer.aisec.cpg.graph.NodeBuilder
import de.fraunhofer.aisec.cpg.graph.declarations.Declaration
import de.fraunhofer.aisec.cpg.graph.types.UnknownType
import org.mozilla.javascript.Node
import org.mozilla.javascript.ast.FunctionNode
import org.mozilla.javascript.ast.Name
import org.mozilla.javascript.ast.VariableDeclaration

class DeclarationHandler(lang: JavaScriptLanguageFrontend) :
    Handler<Declaration, Node, JavaScriptLanguageFrontend>({ Declaration() }, lang) {

    init {
        map.put(FunctionNode::class.java, ::handleFunctionNode)
        map.put(VariableDeclaration::class.java, ::handleVariableDeclaration)
    }

    private fun handleFunctionNode(node: Node): Declaration? {
        if (node !is FunctionNode) {
            return null
        }

        val func =
            NodeBuilder.newFunctionDeclaration(
                node.functionName?.identifier ?: "",
                lang.getCodeFromRawNode(node)
            )

        func.body = lang.statementHandler.handle(node.body)

        return func
    }

    private fun handleVariableDeclaration(
        node: Node
    ): de.fraunhofer.aisec.cpg.graph.declarations.VariableDeclaration? {
        if (node !is VariableDeclaration) {
            return null
        }

        val init = node.variables.first()
        val name = (init.target as Name).identifier

        val decl =
            NodeBuilder.newVariableDeclaration(
                name,
                UnknownType.getUnknownType(),
                lang.getCodeFromRawNode(node),
                false
            )

        decl.initializer = lang.expressionHandler.handle(init.initializer)

        return decl
    }
}
