package io.clouditor.graph.passes

import de.fraunhofer.aisec.cpg.TranslationContext
import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.graph.Name
import de.fraunhofer.aisec.cpg.graph.declarations.TranslationUnitDeclaration
import de.fraunhofer.aisec.cpg.graph.statements.expressions.MemberCallExpression
import de.fraunhofer.aisec.cpg.passes.TranslationResultPass
import io.clouditor.graph.*

abstract class LogPass(ctx: TranslationContext) : TranslationResultPass(ctx) {
    override fun cleanup() {}

    protected fun handleLog(
        t: TranslationResult,
        m: MemberCallExpression,
        name: String,
        tu: TranslationUnitDeclaration
    ) {
        // get the application this is running in
        val application = t.findApplicationByTU(tu)

        // we assume that this is going to std out, for example resulting in log collection in
        // k8s; the kubernetes resource is only connected if it found

        // check, if application runs somewhere with resource logging
        val log =
            application
                ?.runsOn
                ?.firstOrNull()
                ?.nextDFG
                ?.filterIsInstance<ResourceLogging>()
                ?.firstOrNull()

        val out = LogOperation(m, log, m.arguments.firstOrNull())
        out.location = m.location
        out.name = Name(name)

        // add DFG from expression to sink
        out.logging?.let { out.value.nextDFG.add((it)) }

        t += out

        application?.functionalities?.plusAssign(out)
    }
}
