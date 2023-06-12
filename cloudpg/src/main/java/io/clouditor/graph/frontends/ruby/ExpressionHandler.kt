package io.clouditor.graph.frontends.ruby

import de.fraunhofer.aisec.cpg.frontends.Handler
import de.fraunhofer.aisec.cpg.graph.*
import de.fraunhofer.aisec.cpg.graph.statements.Statement
import de.fraunhofer.aisec.cpg.graph.statements.expressions.Expression
import de.fraunhofer.aisec.cpg.graph.statements.expressions.ProblemExpression
import de.fraunhofer.aisec.cpg.graph.types.TypeParser
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
        // FIXME: what is this? Unimplemented or intentional?
        return null
    }

    private fun handleAttrAssignNode(node: Node?): Statement? {
        if (node !is AttrAssignNode) {
            return null
        }

        val binOp = newBinaryOperator("=", language.code)

        val base = this.handle(node.receiverNode) as Expression
        val expr =
            newMemberExpression(
                node.name.idString(),
                base,
                UnknownType.getUnknownType(),
                "=",
                language.code
            )

        binOp.lhs = expr
        binOp.rhs = this.handle(node.argsNode) as Expression

        return expr
    }

    private fun handleDVarNode(node: Node?): Statement? {
        if (node !is DVarNode) {
            return null
        }

        return newDeclaredReferenceExpression(
            node.name.idString(),
            UnknownType.getUnknownType(),
            language.code
        )
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
                language.code
            )
        val rhs = this.handle((node as AssignableNode).valueNode) as? Expression

        // can we resolve it?
        // FIXME
        var decl = this.lang.scopeManager.resolveReference(lhs)

        if (decl == null) {
            val stmt = newDeclarationStatement(language.code)
            decl =
                newVariableDeclaration(lhs.name, UnknownType.getUnknownType(), language.code, false)
            decl.initializer = rhs

            stmt.singleDeclaration = decl

            return stmt
        }

        val binOp = newBinaryOperator("=", language.code)
        binOp.lhs = lhs
        binOp.rhs = rhs

        return binOp
    }

    private fun handleCallNode(node: Node): Expression? {
        if (node !is CallNode) {
            return null
        }

        val base = handle(node.receiverNode) as? Expression

        val mce = newMemberCallExpression(base, false, language.code)

        for (arg in node.argsNode?.childNodes() ?: emptyList()) {
            mce.addArgument(handle(arg) as Expression)
        }

        // add the iterNode as last argument
        node.iterNode?.let { mce.addArgument(handle(it) as Expression) }

        return mce
    }

    private fun handleIterNode(node: Node): Expression? {
        if (node !is IterNode) {
            return null
        }

        // a complete hack, to handle iter nodes, which is sort of a lambda expression
        // so we create an anonymous function declaration out of the bodyNode and varNode
        // and a declared reference expressions to that anonymous function
        val func = newFunctionDeclaration("", language.code)

        // FIXME
        lang.scopeManager.enterScope(func)

        // FIXME (maybe use handle methods implemented in "this"?)
        for (arg in node.argsNode.args) {
            val param = lang.declarationHandler.handle(arg)
            lang.scopeManager.addDeclaration(param)
        }

        func.body = lang.statementHandler.handle(node.bodyNode)

        lang.scopeManager.leaveScope(func)

        val def = newDeclarationStatement(language.code)
        def.singleDeclaration = func

        val cse = newCompoundStatementExpression(language.code)
        cse.statement = def

        return cse
    }

    private fun handleStrNode(node: Node): Expression? {
        if (node !is StrNode) {
            return null
        }

        return newLiteral(
            String(node.value.bytes()),
            TypeParser.createFrom("string", language),
            language.code
        )
    }
}
