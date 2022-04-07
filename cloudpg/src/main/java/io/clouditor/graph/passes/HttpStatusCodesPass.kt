package io.clouditor.graph.passes

import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.graph.statements.CompoundStatement
import de.fraunhofer.aisec.cpg.graph.statements.ReturnStatement
import de.fraunhofer.aisec.cpg.passes.Pass
import io.clouditor.graph.HttpEndpoint
import io.clouditor.graph.additionalNodes

class HttpStatusCodesPass : Pass() {

    override fun cleanup() {}

    override fun accept(result: TranslationResult?) {

        result?.additionalNodes?.filterIsInstance(HttpEndpoint::class.java)?.forEach {
            (it.handler?.body as CompoundStatement).statements.forEach {
                if (it is ReturnStatement) {}
            }
        }
    }
}
