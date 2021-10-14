package io.clouditor.graph

import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.graph.declarations.Declaration
import de.fraunhofer.aisec.cpg.graph.declarations.FieldDeclaration
import de.fraunhofer.aisec.cpg.graph.declarations.VariableDeclaration
import de.fraunhofer.aisec.cpg.graph.statements.expressions.*

/**
 * The value resolver tries to resolve the value of an [Expression] basically by following edges
 * until we reach a [Literal].
 *
 * It contains some advanced mechanics such as resolution of values of arrays, if they contain
 * literal values. Furthermore, its behaviour can be adjusted by implementing the [cannotResolve]
 * function, which is called when the default behaviour would not be able to resolve the value. This
 * way, language specific features such as string formatting can be modelled.
 */
open class ValueResolver(
    /**
     * Contains a reference to a function that gets called if the value cannot be resolved by the
     * standard behaviour.
     */
    open val cannotResolve: (Node?, ValueResolver) -> Any? = { node: Node?, _: ValueResolver ->
        // end of the line, lets just keep the expression name
        if (node != null) {
            "{${node.name}}"
        } else {
            null
        }
    }
) {

    private fun resolveDeclaration(decl: Declaration?): Any? {
        when (decl) {
            is VariableDeclaration -> return resolve(decl.initializer)
            is FieldDeclaration -> return resolve(decl.initializer)
        }

        return cannotResolve(decl, this)
    }

    /** Tries to resolve this expression. Anything can happen. */
    fun resolve(expr: Expression?): Any? {
        when (expr) {
            is Literal<*> -> return expr.value?.toString() ?: ""
            is DeclaredReferenceExpression -> return resolveDeclaration(expr.refersTo)
            is BinaryOperator -> {
                // resolve lhs
                val lhsValue = resolve(expr.lhs)

                // resolve rhs
                val rhsValue = resolve(expr.rhs)

                if (expr.operatorCode == "+") {
                    if (lhsValue is String) {
                        return lhsValue + rhsValue
                    } else if (lhsValue is Int && rhsValue is Number) {
                        return lhsValue + rhsValue.toInt()
                    } else if (lhsValue is Long && rhsValue is Number) {
                        return lhsValue + rhsValue.toLong()
                    } else if (lhsValue is Short && rhsValue is Number) {
                        return lhsValue + rhsValue.toShort()
                    } else if (lhsValue is Byte && rhsValue is Number) {
                        return lhsValue + rhsValue.toByte()
                    } else if (lhsValue is Double && rhsValue is Number) {
                        return lhsValue + rhsValue.toDouble()
                    } else if (lhsValue is Float && rhsValue is Number) {
                        return lhsValue + rhsValue.toDouble()
                    }
                } else if (expr.operatorCode == "-") {
                    if (lhsValue is Int && rhsValue is Number) {
                        return lhsValue - rhsValue.toInt()
                    } else if (lhsValue is Long && rhsValue is Number) {
                        return lhsValue - rhsValue.toLong()
                    } else if (lhsValue is Short && rhsValue is Number) {
                        return lhsValue - rhsValue.toShort()
                    } else if (lhsValue is Byte && rhsValue is Number) {
                        return lhsValue - rhsValue.toByte()
                    } else if (lhsValue is Double && rhsValue is Number) {
                        return lhsValue - rhsValue.toDouble()
                    } else if (lhsValue is Float && rhsValue is Number) {
                        return lhsValue - rhsValue.toDouble()
                    }
                } else if (expr.operatorCode == "/") {
                    if (lhsValue is Int && rhsValue is Number) {
                        return lhsValue / rhsValue.toInt()
                    } else if (lhsValue is Long && rhsValue is Number) {
                        return lhsValue / rhsValue.toLong()
                    } else if (lhsValue is Short && rhsValue is Number) {
                        return lhsValue / rhsValue.toShort()
                    } else if (lhsValue is Byte && rhsValue is Number) {
                        return lhsValue / rhsValue.toByte()
                    } else if (lhsValue is Double && rhsValue is Number) {
                        return lhsValue / rhsValue.toDouble()
                    } else if (lhsValue is Float && rhsValue is Number) {
                        return lhsValue / rhsValue.toDouble()
                    }
                } else if (expr.operatorCode == "*") {
                    if (lhsValue is Int && rhsValue is Number) {
                        return lhsValue * rhsValue.toInt()
                    } else if (lhsValue is Long && rhsValue is Number) {
                        return lhsValue * rhsValue.toLong()
                    } else if (lhsValue is Short && rhsValue is Number) {
                        return lhsValue * rhsValue.toShort()
                    } else if (lhsValue is Byte && rhsValue is Number) {
                        return lhsValue * rhsValue.toByte()
                    } else if (lhsValue is Double && rhsValue is Number) {
                        return lhsValue * rhsValue.toDouble()
                    } else if (lhsValue is Float && rhsValue is Number) {
                        return lhsValue * rhsValue.toDouble()
                    }
                }

                return cannotResolve(expr, this)
            }
            is CastExpression -> {
                return this.resolve(expr.expression)
            }
            is ArraySubscriptionExpression -> {
                val array =
                    (expr.arrayExpression as? DeclaredReferenceExpression)?.refersTo as?
                        VariableDeclaration
                val ile = array?.initializer as? InitializerListExpression

                ile?.let {
                    return resolve(
                        it.initializers
                            .filterIsInstance(KeyValueExpression::class.java)
                            .firstOrNull { kve ->
                                (kve.key as? Literal<*>)?.value ==
                                    (expr.subscriptExpression as? Literal<*>)?.value
                            }
                            ?.value
                    )
                }

                return cannotResolve(expr, this)
            }
            is ConditionalExpression -> {
                // assume that condition is a binary operator
                if (expr.condition is BinaryOperator) {
                    val lhs = resolve((expr.condition as? BinaryOperator)?.lhs)
                    val rhs = resolve((expr.condition as? BinaryOperator)?.rhs)

                    return if (lhs == rhs) {
                        resolve(expr.thenExpr)
                    } else {
                        resolve(expr.elseExpr)
                    }
                }

                return cannotResolve(expr, this)
            }
        }

        return cannotResolve(expr, this)
    }
}
