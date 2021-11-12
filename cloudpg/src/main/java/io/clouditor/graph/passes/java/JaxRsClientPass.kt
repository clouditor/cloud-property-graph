package io.clouditor.graph.passes.java

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

class JaxRsClientPass : HttpClientPass() {
    override fun accept(t: TranslationResult) {
        for (tu in t.translationUnits) {
            tu.accept(
                Strategy::AST_FORWARD,
                object : IVisitor<Node?>() {
                    fun visit(r: StaticCallExpression) {
                        try {
                            // look for ClientBuilder.newClient (Jersey 3.x and 2.x)
                            if (r.fqn == "jakarta.ws.rs.client.ClientBuilder.newClient" ||
                                    r.fqn == "javax.ws.rs.client.ClientBuilder.newClient"
                            ) {
                                handleClient(t, r, tu)
                            }

                            // or ClientBuilder.newBuilder
                            if (r.fqn == "javax.ws.rs.client.ClientBuilder.newBuilder") {
                                handleBuilder(t, r, tu)
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
        r: StaticCallExpression,
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
                ?.followEOG { it.end is MemberCallExpression && it.end.name == "build" }
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
        var client: VariableDeclaration? = null
        val clientRefs = mutableListOf<DeclaredReferenceExpression>()
        var target: VariableDeclaration? = null
        val targetRefs = mutableListOf<DeclaredReferenceExpression>()
        var targetToClient = mutableMapOf<VariableDeclaration, VariableDeclaration>()

        // look for the client itself, probably it is the DFG target
        val pair = followDFGTargetToDeclaration(creationCall)
        pair?.let {
            client = it.second as VariableDeclaration
            clientRefs += it.first
        }

        val edges =
            creationCall.followEOG { it.end is MemberCallExpression && it.end.name == "target" }
        edges?.let { it ->
            val last = it.last()

            val targetCall = last.end as MemberCallExpression

            handleTargetCall(targetCall, t, tu)
        }
    }

    private fun handleTargetCall(
        targetCall: MemberCallExpression,
        t: TranslationResult,
        tu: TranslationUnitDeclaration
    ) {
        val app = t.findApplicationByTU(tu)

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
                        is StaticCallExpression -> {
                            // support for some special calls, i.e. format
                            if (node.name == "getenv") {
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
            object : IVisitor<Node?>() {
                fun visit(mce: MemberCallExpression) {
                    // just look for a "get"
                    // TODO: actually look for client/builder... Hacky for now
                    if (mce.name == "get") {
                        handleGetCall(t, url.toString(), mce, app)
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
        val httpRequest = createHttpRequest(t, url, getCall, "GET", Expression(), app)

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
