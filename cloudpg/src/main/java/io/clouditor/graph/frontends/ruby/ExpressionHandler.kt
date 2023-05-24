package io.clouditor.graph.frontends.ruby

import de.fraunhofer.aisec.cpg.frontends.Handler
import de.fraunhofer.aisec.cpg.graph.*
import de.fraunhofer.aisec.cpg.graph.statements.Statement
import de.fraunhofer.aisec.cpg.graph.statements.expressions.Expression
import de.fraunhofer.aisec.cpg.graph.statements.expressions.ProblemExpression
import de.fraunhofer.aisec.cpg.graph.types.UnknownType
import org.jruby.ast.*
import org.jruby.ast.Node

class ExpressionHandler(lang: RubyLanguageFrontend) :
    Handler<Statement, Node, RubyLanguageFrontend>({ ProblemExpression() }, lang) {

    init {
        map.put(CallNode::class.java, ::handleCallNode)
        map.put(FCallNode::class.java, ::handleFCallNode)
        map.put(IterNode::class.java, ::handleIterNode)
        map.put(StrNode::class.java, ::handleStrNode)
        map.put(DVarNode::class.java, ::handleDVarNode)
        map.put(AttrAssignNode::class.java, ::handleAttrAssignNode)
        map.put(AssignableNode::class.java, ::handleAssignableNode)
    }

    private fun handleFCallNode(node: Node?): Statement? {
        if (node !is FCallNode) {
            return null
        }

        return null
    }

    private fun handleAttrAssignNode(node: Node?): Statement? {
        if (node !is AttrAssignNode) {
            return null
        }

        val binOp = newBinaryOperator("=", frontend.getCodeFromRawNode(node))

        val base =
            this.handle(node.receiverNode) as? Expression
                ?: return ProblemExpression("could not parse base")
        val expr =
            newMemberExpression(
                node.name.idString(),
                base,
                UnknownType.getUnknownType(),
                frontend.getCodeFromRawNode(base)
            )

        binOp.lhs = expr
        (this.handle(node.argsNode) as? Expression)?.let { binOp.rhs = it }

        return expr
    }

    private fun handleDVarNode(node: Node?): Statement? {
        if (node !is DVarNode) {
            return null
        }

        val ref =
            newDeclaredReferenceExpression(
                node.name.idString(),
                UnknownType.getUnknownType(),
                frontend.getCodeFromRawNode(node)
            )

        return ref
    }

    private fun handleAssignableNode(node: Node?): Statement? {
        if (node !is DAsgnNode && node !is LocalAsgnNode) {
            return null
        }

        val name =
            if (node is DAsgnNode) {
                node.name
            } else {
                (node as LocalAsgnNode).name
            }

        // either a binary operator or a variable declaration
        val lhs =
            newDeclaredReferenceExpression(
                name.idString(),
                UnknownType.getUnknownType(),
                frontend.getCodeFromRawNode(node)
            )
        val rhs = this.handle((node as AssignableNode).valueNode) as? Expression

        // can we resolve it?
        var decl = this.frontend.scopeManager.resolveReference(lhs)

        if (decl == null) {
            val stmt = newDeclarationStatement(frontend.getCodeFromRawNode(lhs))
            decl =
                newVariableDeclaration(
                    lhs.name,
                    UnknownType.getUnknownType(),
                    frontend.getCodeFromRawNode(lhs),
                    false
                )
            decl.initializer = rhs

            stmt.singleDeclaration = decl

            return stmt
        }

        val binOp = newBinaryOperator("=", frontend.getCodeFromRawNode(node))
        binOp.lhs = lhs
        rhs?.let { binOp.rhs = it }

        return binOp
    }

    private fun handleCallNode(node: Node): Expression? {
        if (node !is CallNode) {
            return null
        }

        val base =
            handle(node.receiverNode) as? Expression
                ?: return ProblemExpression("could not parse base")
        val callee = newMemberExpression(node.name.asJavaString(), base)

        val mce = newMemberCallExpression(callee, false, frontend.getCodeFromRawNode(node))

        for (arg in node.argsNode?.childNodes() ?: emptyList()) {
            mce.addArgument((handle(arg) as? Expression)!!)
        }

        // add the iterNode as last argument
        node.iterNode?.let { mce.addArgument((handle(it) as? Expression)!!) }

        return mce
    }

    private fun handleIterNode(node: Node): Expression? {
        if (node !is IterNode) {
            return null
        }

        // a complete hack, to handle iter nodes, which is sort of a lambda expression
        // so we create an anonymous function declaration out of the bodyNode and varNode
        // and a declared reference expressions to that anonymous function
        val func = newFunctionDeclaration("", frontend.getCodeFromRawNode(node))

        frontend.scopeManager.enterScope(func)

        for (arg in node.argsNode.args) {
            val param = frontend.declarationHandler.handle(arg)
            frontend.scopeManager.addDeclaration(param)
        }

        func.body = frontend.statementHandler.handle(node.bodyNode)

        frontend.scopeManager.leaveScope(func)

        val def = newDeclarationStatement(frontend.getCodeFromRawNode(node))
        def.singleDeclaration = func

        val cse = newCompoundStatementExpression(frontend.getCodeFromRawNode(node).toString())
        cse.statement = def

        return cse
    }

    private fun handleStrNode(node: Node): Expression? {
        if (node !is StrNode) {
            return null
        }

        val literal =
            newLiteral(
                String(node.value.bytes()),
                parseType("string"),
                frontend.getCodeFromRawNode(node)
            )

        return literal
    }
}
