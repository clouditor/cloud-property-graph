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

        val url = PythonValueResolver(app).resolve(r.arguments.first())

        createHttpRequest(t, url as String, r, method, app)
    }
}
