package io.clouditor.graph.passes.python

import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.graph.edge.Properties
import de.fraunhofer.aisec.cpg.graph.statements.expressions.MemberCallExpression
import de.fraunhofer.aisec.cpg.processing.IVisitor
import de.fraunhofer.aisec.cpg.processing.strategy.Strategy
import io.clouditor.graph.*
import io.clouditor.graph.nodes.getStorageOrCreate
import io.clouditor.graph.passes.DatabaseOperationPass

class Psycopg2Pass : DatabaseOperationPass() {

    val clients: MutableMap<Node, Pair<DatabaseConnect, List<DatabaseStorage>>> = mutableMapOf()
    private val executes: MutableMap<Node, DatabaseQuery> = mutableMapOf()

    override fun accept(t: TranslationResult) {
        for (tu in t.translationUnits) {
            val app = t.findApplicationByTU(tu)

            t.accept(
                Strategy::AST_FORWARD, // actually we want to have EOG_FORWARD, but that doesn't
                // work
                object : IVisitor<Node?>() {
                    fun visit(call: MemberCallExpression) {
                        if (call.name == "connect" && call.base.name == "psycopg2") {
                            handleConnect(t, call, app)
                        }
                    }
                }
            )

            // There is no need for us to continue, if we have not found any clients
            if (clients.isEmpty()) {
                log.info("Found no clients in {}, we are not processing this any further", tu.name)
                continue
            }

            // in order to avoid ordering problems, we need to do this one step at a time, so first
            // looking for a cursor.
            t.accept(
                Strategy::AST_FORWARD, // actually we want to have EOG_FORWARD, but that doesn't
                // work
                object : IVisitor<Node?>() {
                    fun visit(call: MemberCallExpression) {
                        clients[call.base]?.let {
                            if (call.name == "cursor") {
                                handleCursor(call, it)
                            }
                        }
                    }
                }
            )

            t.accept(
                Strategy::AST_FORWARD, // actually we want to have EOG_FORWARD, but that doesn't
                // work
                object : IVisitor<Node?>() {
                    fun visit(call: MemberCallExpression) {
                        clients[call.base]?.let {
                            if (call.name == "execute") {
                                handleExecute(t, call, app, it)
                            }
                        }
                    }
                }
            )

            t.accept(
                Strategy::AST_FORWARD, // actually we want to have EOG_FORWARD, but that doesn't
                // work
                object : IVisitor<Node?>() {
                    fun visit(call: MemberCallExpression) {
                        clients[call.base]?.let {
                            if (call.name == "fetchall") {
                                handleFetchAll(t, call, app, it)
                            }
                        }
                    }
                }
            )
        }
    }

    private fun handleFetchAll(
        t: TranslationResult,
        call: MemberCallExpression,
        app: Application?,
        it: Pair<DatabaseConnect, List<DatabaseStorage>>
    ) {
        val lastExecute = getLastExecute(call)

        val target = call.nextDFG.iterator().next()

        val op = lastExecute?.let { executes[it] }
        op?.let {
            // add it to the list of calls
            op.calls.plusAssign(call)

            // add DFG flow towards the target of the fetchall call
            op.addNextDFG(target)
        }
    }

    private fun getLastExecute(call: MemberCallExpression): Node? {
        // find the last execute by going backwards in the EOG
        var prev = call.prevEOG.firstOrNull()

        while (prev != null) {
            if (executes.containsKey(prev)) {
                return prev
            }

            prev = prev.prevEOG.firstOrNull()
        }

        return null
    }

    private fun handleExecute(
        result: TranslationResult,
        call: MemberCallExpression,
        app: Application?,
        pair: Pair<DatabaseConnect, List<DatabaseStorage>>
    ) {
        // first argument is the SQL statement
        val sql = PythonValueResolver(app).resolve(call.arguments.firstOrNull()) as? String

        // tokenize
        val op = buildOpFromSQL(sql, result, call, app, pair)
        op?.let { storeDeclarationOrReference(executes, call, op) }
    }

    private fun buildOpFromSQL(
        sql: String?,
        result: TranslationResult,
        call: MemberCallExpression,
        app: Application?,
        pair: Pair<DatabaseConnect, List<DatabaseStorage>>
    ): DatabaseQuery? {
        val (connect, dbStorage) = pair
        val regex = Regex("SELECT (.*) FROM ([^ ]*).*")
        val something = regex.matchEntire(sql ?: "")

        something?.let { matchResult ->
            val table = matchResult.groups[2]?.value
            val dbName = dbStorage.firstOrNull()?.name
            val storage = connect.to.map { it.getStorageOrCreate(table ?: "", dbName) }

            val op = createDatabaseQuery(result, false, connect, storage, mutableListOf(call), app)
            op.name = call.name

            // in the select case, the arguments are just arguments to the query itself and flow
            // towards the op
            call.arguments.forEach { it.addNextDFG(op) }

            return op
        }

        return null
    }

    private fun handleCursor(
        call: MemberCallExpression,
        pair: Pair<DatabaseConnect, List<DatabaseStorage>>
    ) {
        // the DFG target of this call expression is the cursor, we are interested in
        val target = call.nextDFG.iterator().next()

        // also add any call to cursor() to our client list, because they are the basis for the
        // actual execution, but are basically just a proxy for the client object
        storeDeclarationOrReference(clients, target, pair)
    }

    private fun handleConnect(
        result: TranslationResult,
        call: MemberCallExpression,
        app: Application?
    ) {
        val resolver = PythonValueResolver(app)
        // resolve the connection details
        val host =
            resolver.resolve(
                call.argumentsPropertyEdge
                    .firstOrNull { it.getProperty(Properties.NAME) == "host" }
                    ?.end
            ) as?
                String

        val db =
            resolver.resolve(
                call.argumentsPropertyEdge
                    .firstOrNull { it.getProperty(Properties.NAME) == "database" }
                    ?.end
            ) as?
                String

        // create a new DB operation (connect)
        val connect = createDatabaseConnect(result, host ?: "", call, app)

        val storages = connect.to.map { it.getStorageOrCreate(db ?: "postgres") }

        // the DFG target of this call expression is the client, we are interested in
        val target = call.nextDFG.iterator().next()

        storeDeclarationOrReference(clients, target, Pair(connect, storages))
    }

    override fun cleanup() {
        // nothing to do
    }
}
