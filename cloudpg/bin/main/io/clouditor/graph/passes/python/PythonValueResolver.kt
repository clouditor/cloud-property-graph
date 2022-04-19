package io.clouditor.graph.passes.python

import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.graph.statements.expressions.Expression
import de.fraunhofer.aisec.cpg.graph.statements.expressions.MemberCallExpression
import io.clouditor.graph.Application
import io.clouditor.graph.ValueResolver

class PythonValueResolver(
    app: Application?,
    override val cannotResolve: (Node?, ValueResolver) -> Any? =
            { node: Node?, resolver: ValueResolver ->
        // assume that we are only on one client
        val env =
            app?.runsOn?.firstOrNull()?.labels?.filter { it.key.startsWith("env_") }?.mapKeys {
                it.key.substring(4)
            }
                ?: mutableMapOf()

        when (node) {
            is MemberCallExpression -> {
                var base = resolver.resolve(node.base as Expression?).toString()

                // support for some special calls, i.e. format
                val s =
                    if (node.name == "format") {
                        // basic sub for now, loop through arguments
                        for (i in 0 until node.arguments.size) {
                            // sub on base
                            base =
                                base.replace("{$i}", resolver.resolve(node.arguments[i]).toString())
                        }

                        base
                    } else if (node.base.name == "environ" && node.name == "get") {
                        // environment lookup on python
                        val key = resolver.resolve(node.arguments.firstOrNull())

                        env[key] ?: resolver.resolve(node.arguments[1])
                    } else {
                        // return placeholder
                        "{${node.name}()}"
                    }
                s
            }
            else -> "{${node?.name}}"
        }
    }
) : ValueResolver()
