package io.clouditor.graph.passes

import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.graph.statements.expressions.CallExpression
import de.fraunhofer.aisec.cpg.passes.Pass
import io.clouditor.graph.*

abstract class DatabaseOperationPass : Pass() {

    protected fun createDatabaseConnect(
        t: TranslationResult,
        host: String,
        call: CallExpression,
        app: Application?
    ): DatabaseOperation {
        val db = getDatabaseForHost(t, host)

        val op = DatabaseConnect(db, call)
        op.location = app?.location

        return op
    }

    protected fun createDatabaseQuery(
        t: TranslationResult,
        modify: Boolean,
        connect: DatabaseConnect,
        call: CallExpression,
        app: Application?
    ): DatabaseOperation {
        val op = DatabaseQuery(modify, connect.to, call)
        op.location = app?.location

        return op
    }

    private fun getDatabaseForHost(t: TranslationResult, host: String): List<DatabaseService> {
        log.info("Looking for databases hosted at {}", host)

        return t.additionalNodes.filterIsInstance(DatabaseService::class.java).filter {
            it.name == host
        }
    }
}
