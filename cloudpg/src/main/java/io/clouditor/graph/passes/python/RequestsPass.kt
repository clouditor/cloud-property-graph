package io.clouditor.graph.passes.python

import de.fraunhofer.aisec.cpg.ExperimentalPython
import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.graph.declarations.TranslationUnitDeclaration
import de.fraunhofer.aisec.cpg.graph.statements.expressions.*
import de.fraunhofer.aisec.cpg.processing.IVisitor
import de.fraunhofer.aisec.cpg.processing.strategy.Strategy
import io.clouditor.graph.*
import io.clouditor.graph.passes.HttpClientPass

@ExperimentalPython
class RequestsPass : HttpClientPass() {

    override fun cleanup() {
        // nothing to do
    }

    override fun accept(t: TranslationResult) {
        // if (this.lang is PythonLanguageFrontend) {
        for (tu in t.translationUnits) {
            tu.accept(
                Strategy::AST_FORWARD,
                object : IVisitor<Node?>() {
                    fun visit(r: MemberCallExpression) {
                        // look for requests.get()
                        if (r.name == "get" && r.base.name == "requests") {
                            handleClientRequest(tu, t, r, "GET")
                        } else if (r.name == "post" && r.base.name == "requests") {
                            handleClientRequest(tu, t, r, "POST")
                        }
                    }
                }
            )
        }
        // }
    }

    private fun handleClientRequest(
        tu: TranslationUnitDeclaration,
        t: TranslationResult,
        r: MemberCallExpression,
        method: String
    ) {
        val app = t.findApplicationByTU(tu)

        // assume that we are only on one client
        val env =
            app?.runsOn?.firstOrNull()?.labels?.filter { it.key.startsWith("env_") }?.mapKeys {
                it.key.substring(4)
            }
                ?: mutableMapOf()

        val url =
            ValueResolver { node, resolver ->
                    when (node) {
                        is MemberCallExpression -> {
                            var base = resolver.resolve(node.base as Expression?).toString()

                            // support for some special calls, i.e. format
                            if (node.name == "format") {
                                // basic sub for now, loop through arguments
                                for (i in 0 until node.arguments.size) {
                                    // sub on base
                                    base =
                                        base.replace(
                                            "{$i}",
                                            resolver.resolve(node.arguments[i]).toString()
                                        )
                                }

                                return@ValueResolver base
                            } else if (node.base.name == "environ" && node.name == "get") {
                                // environment lookup on python
                                val key = resolver.resolve(node.arguments.firstOrNull())

                                return@ValueResolver env[key] ?: ""
                            }

                            // return placeholder
                            return@ValueResolver "{${node.name}()}"
                        }
                        else -> return@ValueResolver "{${node?.name}}"
                    }
                }
                .resolve(r.arguments.first())

        createHttpRequest(t, url as String, r, method, app)
    }
}
