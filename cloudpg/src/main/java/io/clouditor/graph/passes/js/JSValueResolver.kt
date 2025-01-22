package io.clouditor.graph.passes.js

import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.graph.statements.expressions.*
import io.clouditor.graph.Application
import io.clouditor.graph.ValueResolver

class JSValueResolver(
    app: Application?,
    override val cannotResolve: (Node?, ValueResolver) -> Any? = { node: Node?, _: ValueResolver ->
        // assume that we are only on one client
        val env =
            app?.runsOn?.firstOrNull()?.labels?.filter { it.key.startsWith("env_") }?.mapKeys {
                it.key.substring(4)
            }
                ?: mutableMapOf()

        when (node) {
            is Expression -> {
                val s =
                    if (node.code?.startsWith("process.env.") == true) {
                        val envVarName =
                            node.code?.split("process.env.")?.get(1)?.split("!")?.get(0)
                        env[envVarName]
                    } else {
                        "{${node.name}}"
                    }
                s
            }
            else -> "{${node?.name}}"
        }
    }
) : ValueResolver()
