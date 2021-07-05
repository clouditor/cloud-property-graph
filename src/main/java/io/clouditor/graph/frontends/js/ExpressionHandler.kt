package io.clouditor.graph.frontends.js

import de.fraunhofer.aisec.cpg.frontends.Handler
import de.fraunhofer.aisec.cpg.graph.NodeBuilder
import de.fraunhofer.aisec.cpg.graph.statements.expressions.Expression
import de.fraunhofer.aisec.cpg.graph.types.TypeParser
import de.fraunhofer.aisec.cpg.graph.types.UnknownType
import org.mozilla.javascript.Node
import org.mozilla.javascript.ast.*

class ExpressionHandler(lang: JavaScriptLanguageFrontend) :
    Handler<Expression, Node, JavaScriptLanguageFrontend>({ Expression() }, lang) {

    init {
        map.put(FunctionCall::class.java, ::handleFunctionCall)
        map.put(RegExpLiteral::class.java, ::handleRegExpLiteral)
        map.put(StringLiteral::class.java, ::handleStringLiteral)
        map.put(FunctionNode::class.java, ::handleFunctionNode)
    }

    private fun handleFunctionCall(node: Node): Expression? {
        if (node !is FunctionCall) {
            return null
        }

        val call =
            if (node.target is PropertyGet) {
                val name = ((node.target as PropertyGet).right as Name).identifier

                val base = lang.expressionHandler.handle((node.target as PropertyGet).left)

                NodeBuilder.newMemberCallExpression(
                    name,
                    name,
                    base,
                    null,
                    ".",
                    lang.getCodeFromRawNode(node)
                )
            } else {
                val name = (node.target as Name).identifier

                NodeBuilder.newCallExpression(name, name, lang.getCodeFromRawNode(node), false)
            }

        for (arg in node.arguments) {
            call.addArgument(handle(arg))
        }

        return call
    }

    private fun handleRegExpLiteral(node: Node): Expression? {
        if (node !is RegExpLiteral) {
            return null
        }

        return NodeBuilder.newLiteral(
            node.value,
            TypeParser.createFrom("string", false),
            lang.getCodeFromRawNode(node)
        )
    }

    private fun handleStringLiteral(node: Node): Expression? {
        if (node !is StringLiteral) {
            return null
        }

        return NodeBuilder.newLiteral(
            node.value,
            TypeParser.createFrom("string", false),
            lang.getCodeFromRawNode(node)
        )
    }

    private fun handleFunctionNode(node: Node): Expression? {
        if (node !is FunctionNode) {
            return null
        }

        // parse the function
        val func = lang.declarationHandler.handle(node)

        // we cannot directly return a function declaration as an expression, so we
        // wrap it into a reference expression
        val ref =
            NodeBuilder.newDeclaredReferenceExpression(
                "",
                UnknownType.getUnknownType(),
                lang.getCodeFromRawNode(node)
            )
        ref.refersTo = func

        return ref
    }
}
