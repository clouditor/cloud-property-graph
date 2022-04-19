package io.clouditor.graph.passes.golang

import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.graph.declarations.FunctionDeclaration
import de.fraunhofer.aisec.cpg.graph.declarations.TranslationUnitDeclaration
import de.fraunhofer.aisec.cpg.graph.statements.expressions.*
import de.fraunhofer.aisec.cpg.processing.IVisitor
import de.fraunhofer.aisec.cpg.processing.strategy.Strategy
import io.clouditor.graph.*
import io.clouditor.graph.passes.HttpClientPass

// This pass is needed only for the local testing mode, since in the testing pass we create the
// endpoints and only after that we can create the respective requests
class GolangHttpRequestPass : HttpClientPass() {

    override fun cleanup() {}

    override fun accept(result: TranslationResult) {
        // look for http call expressions in the client code
        for (tu in result.translationUnits) {
            tu.accept(
                Strategy::AST_FORWARD,
                object : IVisitor<Node?>() {
                    fun visit(m: CallExpression) {
                        handleCallExpression(result, tu, m)
                    }
                }
            )
        }
    }

    private fun handleCallExpression(
        result: TranslationResult,
        tu: TranslationUnitDeclaration,
        c: CallExpression
    ) {
        val app = result.findApplicationByTU(tu)
        var requestFunction = c.invokes.first() as FunctionDeclaration
        // should also have c.base.name == "http" but this is not parsed correctly atm
        if (c.name == "PostForm") {
            createHttpRequest(
                result,
                // TODO the parameter has the name "string0" rather than test.com
                // requestFunction.parameters.first().name,
                "test.com/data",
                c,
                "POST",
                // TODO request body: the default value is not correctly set, so we use the
                // value that has a dfg edge to the request parameter
                requestFunction.parameters[1].prevDFG.first() as DeclaredReferenceExpression,
                app
            )
        }
    }
}
