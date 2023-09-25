package io.clouditor.graph.passes.java

import de.fraunhofer.aisec.cpg.TranslationContext
import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.graph.declarations.Declaration
import de.fraunhofer.aisec.cpg.graph.declarations.TranslationUnitDeclaration
import de.fraunhofer.aisec.cpg.graph.declarations.VariableDeclaration
import de.fraunhofer.aisec.cpg.graph.statements.expressions.*
import de.fraunhofer.aisec.cpg.processing.IVisitor
import de.fraunhofer.aisec.cpg.processing.strategy.Strategy
import io.clouditor.graph.Application
import io.clouditor.graph.ValueResolver
import io.clouditor.graph.findApplicationByTU
import io.clouditor.graph.nodes.followDFGReverse
import io.clouditor.graph.nodes.followEOG
import io.clouditor.graph.passes.HttpClientPass

class JaxRsClientPass(ctx: TranslationContext) : HttpClientPass(ctx) {
    override fun accept(result: TranslationResult) {
        val translationUnits =
            result.components.stream().flatMap { it.translationUnits.stream() }.toList()
        for (tu in translationUnits) {
            tu.accept(
                Strategy::AST_FORWARD,
                object : IVisitor<Node>() {
                    fun visit(t: CallExpression) {
                        try {
                            // look for ClientBuilder.newClient (Jersey 3.x and 2.x)
                            if (t.name.toString() ==
                                    "jakarta.ws.rs.client.ClientBuilder.newClient" ||
                                    t.name.toString() ==
                                        "javax.ws.rs.client.ClientBuilder.newClient"
                            ) {
                                handleClient(result, t, tu)
                            }

                            // or ClientBuilder.newBuilder
                            if (t.name.toString() == "javax.ws.rs.client.ClientBuilder.newBuilder"
                            ) {
                                handleBuilder(result, t, tu)
                            }
                        } catch (t: Throwable) {
                            t.printStackTrace()
                        }
                    }
                }
            )
        }
    }

    private fun handleBuilder(
        t: TranslationResult,
        r: CallExpression,
        tu: TranslationUnitDeclaration
    ) {
        var builder: VariableDeclaration? = null
        val builderRefs = mutableListOf<DeclaredReferenceExpression>()

        // look for the builder itself, probably it is the DFG target
        val pair = followDFGTargetToDeclaration(r)
        pair?.let {
            builder = it.second as VariableDeclaration
            builderRefs += it.first
        }

        val buildCall =
            builder
                ?.followEOG { it.end is MemberCallExpression && it.end.name.localName == "build" }
                ?.lastOrNull()

        (buildCall?.end as? MemberCallExpression)?.let { handleClient(t, it, tu) }
    }

    override fun cleanup() {
        // nothing to do
    }

    private fun handleClient(
        t: TranslationResult,
        creationCall: CallExpression,
        tu: TranslationUnitDeclaration
    ) {
        val clientRefs = mutableListOf<DeclaredReferenceExpression>()

        // look for the client itself, probably it is the DFG target
        val pair = followDFGTargetToDeclaration(creationCall)
        pair?.let { clientRefs += it.first }

        val edges =
            creationCall.followEOG {
                it.end is MemberCallExpression && it.end.name.localName == "target"
            }
        edges?.let { it ->
            val last = it.last()

            val targetCall = last.end as MemberCallExpression

            handleTargetCall(targetCall, t, tu)
        }
    }

    private fun handleTargetCall(
        targetCall: MemberCallExpression,
        result: TranslationResult,
        tu: TranslationUnitDeclaration
    ) {
        val app = result.findApplicationByTU(tu)

        // assume that we are only on one client
        val env =
            app?.runsOn?.firstOrNull()?.labels?.filter { it.key.startsWith("env_") }?.mapKeys {
                it.key.substring(4)
            }
                ?: mutableMapOf()

        // first argument is the path
        val url =
            ValueResolver { node, resolver ->
                    when (node) {
                        is CallExpression -> {
                            // support for some special calls, i.e. format
                            if (node.name.localName == "getenv") {
                                // environment lookup on Java
                                val key = resolver.resolve(node.arguments.firstOrNull())

                                return@ValueResolver env[key] ?: ""
                            }

                            // return placeholder
                            return@ValueResolver "{${node.name}()}"
                        }
                        else -> return@ValueResolver "{${node?.name}}"
                    }
                }
                .resolve(targetCall.arguments.firstOrNull())

        targetCall.accept(
            Strategy::EOG_FORWARD,
            object : IVisitor<Node>() {
                fun visit(t: MemberCallExpression) {
                    // just look for a "get"
                    // TODO: actually look for client/builder... Hacky for now
                    if (t.name.localName == "get") {
                        handleGetCall(result, url.toString(), t, app)
                    }
                }
            }
        )
    }

    private fun handleGetCall(
        t: TranslationResult,
        url: String,
        getCall: MemberCallExpression,
        app: Application?
    ) {
        val httpRequest = createHttpRequest(t, url, getCall, "GET", null, app)

        val i = httpRequest.to.firstOrNull()
        val f = i?.handler

        // can follow remote call to literal?
        val path = f?.followDFGReverse { it.second is Literal<*> }
        path?.let {
            val last = it.last().second as Literal<*>
            println("value was: ${last.value}")
        }
    }

    private fun followDFGTargetToDeclaration(
        n: Node
    ): Pair<DeclaredReferenceExpression, Declaration?>? {
        // get the next dfg
        val ref = n.nextDFG.filterIsInstance<DeclaredReferenceExpression>().firstOrNull()

        // it is probably a ref, so we need to follow it back to the declaration
        if (ref is DeclaredReferenceExpression) {
            return Pair(ref, ref.refersTo)
        }

        return null
    }
}
