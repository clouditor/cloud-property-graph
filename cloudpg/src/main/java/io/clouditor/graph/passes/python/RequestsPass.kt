package io.clouditor.graph.passes.python

import de.fraunhofer.aisec.cpg.TranslationContext
import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.graph.declarations.TranslationUnitDeclaration
import de.fraunhofer.aisec.cpg.graph.statements.expressions.*
import de.fraunhofer.aisec.cpg.processing.IVisitor
import de.fraunhofer.aisec.cpg.processing.strategy.Strategy
import io.clouditor.graph.*
import io.clouditor.graph.passes.HttpClientPass

class RequestsPass(ctx: TranslationContext) : HttpClientPass(ctx) {

    override fun cleanup() {
        // nothing to do
    }

    override fun accept(result: TranslationResult) {
        // if (this.lang is PythonLanguageFrontend) {
        for (tu in result.translationUnits) {
            tu.accept(
                Strategy::AST_FORWARD,
                object : IVisitor<Node>() {
                    override fun visit(t: Node) {
                        when (t) {
                            is MemberCallExpression -> {
                                // look for requests.get()
                                if (t.name.localName == "get" &&
                                        t.base?.name?.localName == "requests"
                                ) {
                                    handleClientRequest(tu, result, t, "GET")
                                } else if (t.name.localName == "post" &&
                                        t.base?.name?.localName == "requests"
                                ) {
                                    handleClientRequest(tu, result, t, "POST")
                                }
                            }
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

        val url = PythonValueResolver(app).resolve(r.arguments.first())

        // FIXME: Safety measures added later; they were not necessary with the previous CPG version.
        // FIXME: This can mean that the expected value differs from before (not null/empty).
        createHttpRequest(t, url as String, r, method, r.arguments.getOrNull(1), app)
    }
}
