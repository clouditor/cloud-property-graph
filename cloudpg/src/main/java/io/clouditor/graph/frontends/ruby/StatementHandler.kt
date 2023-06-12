package io.clouditor.graph.frontends.ruby

import de.fraunhofer.aisec.cpg.frontends.Handler
import de.fraunhofer.aisec.cpg.graph.newCompoundStatement
import de.fraunhofer.aisec.cpg.graph.newReturnStatement
import de.fraunhofer.aisec.cpg.graph.statements.CompoundStatement
import de.fraunhofer.aisec.cpg.graph.statements.ReturnStatement
import de.fraunhofer.aisec.cpg.graph.statements.Statement
import de.fraunhofer.aisec.cpg.graph.statements.expressions.ProblemExpression
import org.jruby.ast.BlockNode
import org.jruby.ast.Node

class StatementHandler(lang: RubyLanguageFrontend) :
    Handler<Statement, Node, RubyLanguageFrontend>({ ProblemExpression() }, lang) {

    init {
        map.put(BlockNode::class.java, ::handleBlockNode)
    }

    private fun handleBlockNode(blockNode: Node): CompoundStatement? {
        if (blockNode !is BlockNode) {
            return null
        }

        blockNode.containsVariableAssignment()
        val compoundStatement = newCompoundStatement(language.code)

        for (node in blockNode) {
            val statement = frontend.expressionHandler.handle(node)
            statement?.let { compoundStatement.addStatement(it) }
        }

        val statements = compoundStatement.statements

        // get the last statement
        var lastStatement: Statement? = null
        if (statements.isNotEmpty()) {
            lastStatement = statements[statements.size - 1]
        }

        // add an implicit return statement, if there is none
        if (lastStatement !is ReturnStatement) {
            val returnStatement = newReturnStatement(language.code)
            returnStatement.isImplicit = true
            compoundStatement.addStatement(returnStatement)
        }

        return compoundStatement
    }
}
