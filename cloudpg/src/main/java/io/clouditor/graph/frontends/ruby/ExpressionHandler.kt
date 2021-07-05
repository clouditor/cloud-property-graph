package io.clouditor.graph.frontends.ruby

import de.fraunhofer.aisec.cpg.frontends.Handler
import de.fraunhofer.aisec.cpg.graph.NodeBuilder
import de.fraunhofer.aisec.cpg.graph.statements.Statement
import de.fraunhofer.aisec.cpg.graph.statements.expressions.Expression
import de.fraunhofer.aisec.cpg.graph.types.TypeParser
import de.fraunhofer.aisec.cpg.graph.types.UnknownType
import org.jruby.ast.*

class ExpressionHandler(lang: RubyLanguageFrontend) :
    Handler<Statement, Node, RubyLanguageFrontend>({ Expression() }, lang) {

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

        var binOp = NodeBuilder.newBinaryOperator("=", lang.getCodeFromRawNode(node))

        var base = this.handle(node.receiverNode) as? Expression
        var expr =
            NodeBuilder.newMemberExpression(
                base,
                UnknownType.getUnknownType(),
                node.name.idString(),
                "=",
                lang.getCodeFromRawNode(base)
            )

        binOp.lhs = expr
        binOp.rhs = this.handle(node.argsNode) as? Expression

        return expr
    }

    private fun handleDVarNode(node: Node?): Statement? {
        if (node !is DVarNode) {
            return null
        }

        val ref =
            NodeBuilder.newDeclaredReferenceExpression(
                node.name.idString(),
                UnknownType.getUnknownType(),
                lang.getCodeFromRawNode(node)
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
            NodeBuilder.newDeclaredReferenceExpression(
                name.idString(),
                UnknownType.getUnknownType(),
                lang.getCodeFromRawNode(node)
            )
        val rhs = this.handle((node as AssignableNode).valueNode) as? Expression

        // can we resolve it?
        var decl = this.lang.scopeManager.resolve(lhs)

        if (decl == null) {
            val stmt = NodeBuilder.newDeclarationStatement(lang.getCodeFromRawNode(lhs))
            decl =
                NodeBuilder.newVariableDeclaration(
                    lhs.name,
                    UnknownType.getUnknownType(),
                    lang.getCodeFromRawNode(lhs),
                    false
                )
            decl.initializer = rhs

            stmt.singleDeclaration = decl

            return stmt
        }

        val binOp = NodeBuilder.newBinaryOperator("=", lang.getCodeFromRawNode(node))
        binOp.lhs = lhs
        binOp.rhs = rhs

        return binOp
    }

    private fun handleCallNode(node: Node): Expression? {
        if (node !is CallNode) {
            return null
        }

        val base = handle(node.receiverNode) as? Expression
        val member = null

        val mce =
            NodeBuilder.newMemberCallExpression(
                node.name.asJavaString(),
                node.name.asJavaString(),
                base,
                member,
                ".",
                lang.getCodeFromRawNode(node)
            )

        for (arg in node.argsNode?.childNodes() ?: emptyList()) {
            mce.addArgument(handle(arg) as? Expression)
        }

        // add the iterNode as last argument
        node.iterNode?.let { mce.addArgument(handle(it) as? Expression) }

        return mce
    }

    private fun handleIterNode(node: Node): Expression? {
        if (node !is IterNode) {
            return null
        }

        // a complete hack, to handle iter nodes, which is sort of a lambda expression
        // so we create an anonymous function declaration out of the bodyNode and varNode
        // and a declared reference expressions to that anonymous function
        val func = NodeBuilder.newFunctionDeclaration("", lang.getCodeFromRawNode(node))

        lang.scopeManager.enterScope(func)

        for (arg in node.argsNode.args) {
            val param = lang.declarationHandler.handle(arg)
            lang.scopeManager.addDeclaration(param)
        }

        func.body = lang.statementHandler.handle(node.bodyNode)

        lang.scopeManager.leaveScope(func)

        var def = NodeBuilder.newDeclarationStatement(lang.getCodeFromRawNode(node))
        def.singleDeclaration = func

        var cse =
            NodeBuilder.newCompoundStatementExpression(lang.getCodeFromRawNode(node).toString())
        cse.statement = def

        return cse
    }

    private fun handleStrNode(node: Node): Expression? {
        if (node !is StrNode) {
            return null
        }

        val literal =
            NodeBuilder.newLiteral(
                String(node.value.bytes()),
                TypeParser.createFrom("string", false),
                lang.getCodeFromRawNode(node)
            )

        return literal
    }
}
