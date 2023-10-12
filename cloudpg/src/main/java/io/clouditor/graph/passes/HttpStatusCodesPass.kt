package io.clouditor.graph.passes

import de.fraunhofer.aisec.cpg.TranslationContext
import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.graph.statements.ReturnStatement
import de.fraunhofer.aisec.cpg.graph.statements.expressions.Block
import de.fraunhofer.aisec.cpg.passes.TranslationResultPass
import io.clouditor.graph.HttpEndpoint

class HttpStatusCodesPass(ctx: TranslationContext) : TranslationResultPass(ctx) {

    override fun cleanup() {}

    override fun accept(result: TranslationResult) {

        result.additionalNodes.filterIsInstance(HttpEndpoint::class.java).forEach {
            (it.handler?.body as Block).statements.forEach {
                if (it is ReturnStatement) {
                    // TODO
                }
            }
        }
    }
}
