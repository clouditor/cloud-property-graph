package io.clouditor.graph.passes

import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.graph.declarations.VariableDeclaration
import de.fraunhofer.aisec.cpg.graph.edge.PropertyEdge
import de.fraunhofer.aisec.cpg.graph.statements.expressions.CallExpression
import de.fraunhofer.aisec.cpg.graph.statements.expressions.DeclaredReferenceExpression
import de.fraunhofer.aisec.cpg.passes.Pass
import io.clouditor.graph.*

abstract class DatabaseOperationPass : Pass() {

    protected fun createDatabaseConnect(
        t: TranslationResult,
        host: String,
        call: CallExpression,
        app: Application?
    ): DatabaseConnect {
        val db = getDatabaseForHost(t, host)

        val op = DatabaseConnect(listOf(call), null, db)
        op.location = app?.location

        return op
    }

    protected fun createDatabaseQuery(
        t: TranslationResult,
        modify: Boolean,
        connect: DatabaseConnect,
        storage: List<DatabaseStorage>,
        calls: List<CallExpression>,
        app: Application?
    ): DatabaseQuery {
        val op = DatabaseQuery(modify, calls, storage, connect.to)
        op.location = app?.location

        storage.forEach {
            if (op.isModify) {
                op.addNextDFG(it)
            } else {
                op.addPrevDFG(it)
            }
        }

        return op
    }

    private fun getDatabaseForHost(t: TranslationResult, host: String): List<DatabaseService> {
        log.info("Looking for databases hosted at {}", host)

        return t.additionalNodes.filterIsInstance(DatabaseService::class.java).filter {
            it.name == host
        }
    }

    internal fun <T> storeDeclarationOrReference(map: MutableMap<Node, T>, target: Node, obj: T) {
        // store it in our clients/connection map
        map[target] = obj

        // if this is variable declaration, follow the DFG to its REFERS_TO references and store
        // them as well
        if (target is VariableDeclaration) {
            target.nextDFG.forEach {
                if (it is DeclaredReferenceExpression && it.refersTo == target) {
                    map[it] = obj
                }
            }
        }
    }
}
