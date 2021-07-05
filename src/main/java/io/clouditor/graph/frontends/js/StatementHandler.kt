package io.clouditor.graph.frontends.js

import de.fraunhofer.aisec.cpg.frontends.Handler
import de.fraunhofer.aisec.cpg.graph.NodeBuilder
import de.fraunhofer.aisec.cpg.graph.statements.ReturnStatement
import de.fraunhofer.aisec.cpg.graph.statements.Statement
import org.mozilla.javascript.Node
import org.mozilla.javascript.ast.AstRoot
import org.mozilla.javascript.ast.Block
import org.mozilla.javascript.ast.ExpressionStatement
import org.mozilla.javascript.ast.VariableDeclaration

class StatementHandler(lang: JavaScriptLanguageFrontend) :
    Handler<Statement, Node, JavaScriptLanguageFrontend>({ Statement() }, lang) {

    init {
        map.put(ExpressionStatement::class.java, ::handleExpressionStatement)
        map.put(Block::class.java, ::handleBlock)
        map.put(AstRoot::class.java, ::handleBlock)
        map.put(VariableDeclaration::class.java, ::handleVariableDeclaration)
    }

    private fun handleExpressionStatement(node: Node): Statement? {
        return if (node is ExpressionStatement) {
            return lang.expressionHandler.handle(node.expression)
        } else {
            null
        }
    }

    private fun handleBlock(node: Node): Statement? {
        if (node !is Block && node !is AstRoot) {
            return null
        }

        val body = NodeBuilder.newCompoundStatement(lang.getCodeFromRawNode(node))

        var child: Node? = node.firstChild
        while (child != null) {
            val statement = lang.statementHandler.handle(child)

            body.addStatement(statement)

            child = child.next
        }

        val statements = body.statements

        // get the last statement
        var lastStatement: Statement? = null
        if (!statements.isEmpty()) {
            lastStatement = statements.get(statements.size - 1)
        }

        // add an implicit return statement, if there is none
        if (lastStatement !is ReturnStatement) {
            val returnStatement = NodeBuilder.newReturnStatement("return;")
            returnStatement.isImplicit = true
            body.addStatement(returnStatement)
        }

        return body
    }

    private fun handleVariableDeclaration(node: Node): Statement? {
        if (node !is VariableDeclaration) {
            return null
        }

        val decl = NodeBuilder.newDeclarationStatement(lang.getCodeFromRawNode(node))

        decl.singleDeclaration = lang.declarationHandler.handle(node)

        return decl
    }
}
