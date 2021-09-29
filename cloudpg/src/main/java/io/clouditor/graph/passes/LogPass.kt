package io.clouditor.graph.passes

import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.graph.declarations.TranslationUnitDeclaration
import de.fraunhofer.aisec.cpg.graph.statements.expressions.MemberCallExpression
import de.fraunhofer.aisec.cpg.passes.Pass
import io.clouditor.graph.*

abstract class LogPass : Pass() {
    override fun cleanup() {}

    protected fun handleLog(
        t: TranslationResult,
        m: MemberCallExpression,
        tu: TranslationUnitDeclaration
    ){
        // get the application this is running in
        val application = t.findApplicationByTU(tu)

        // check, if application runs somewhere with resource logging
        val log =
                application
                        ?.runsOn
                        ?.firstOrNull()
                        ?.nextDFG
                        ?.filterIsInstance<ResourceLogging>()
                        ?.map { it }
                        ?: emptyList()

        val out = LogOutput(m, log as List<Logging>, m.arguments.firstOrNull())
        out.location = m.location
        out.name = m.name

        // add DFG from expression to sink
        out.to.forEach { out.value.nextDFG.add((it)) }

        t += out

        application?.functionalities?.plusAssign(out)
    }
}