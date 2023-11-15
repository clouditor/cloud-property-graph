package io.clouditor.graph.passes.python

import de.fraunhofer.aisec.cpg.TranslationContext
import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.graph.statements.expressions.CallExpression
import de.fraunhofer.aisec.cpg.graph.statements.expressions.MemberCallExpression
import de.fraunhofer.aisec.cpg.graph.statements.expressions.MemberExpression
import de.fraunhofer.aisec.cpg.passes.SymbolResolver
import de.fraunhofer.aisec.cpg.passes.order.DependsOn
import de.fraunhofer.aisec.cpg.processing.IVisitor
import de.fraunhofer.aisec.cpg.processing.strategy.Strategy
import io.clouditor.graph.*
import io.clouditor.graph.nodes.getStorageOrCreate
import io.clouditor.graph.passes.DatabaseOperationPass
import java.net.URI

@Suppress("UNUSED_PARAMETER")
@DependsOn(SymbolResolver::class)
class PyMongoPass(ctx: TranslationContext) : DatabaseOperationPass(ctx) {

    val clients: MutableMap<Node, DatabaseConnect> = mutableMapOf()
    val dbs: MutableMap<Node, Pair<DatabaseConnect, List<DatabaseStorage>>> = mutableMapOf()
    val collections: MutableMap<Node, Pair<DatabaseConnect, List<DatabaseStorage>>> = mutableMapOf()

    override fun accept(result: TranslationResult) {
        val translationUnits =
            result.components.stream().flatMap { it.translationUnits.stream() }.toList()
        for (tu in translationUnits) {
            val app = result.findApplicationByTU(tu)

            tu.accept(
                Strategy::AST_FORWARD, // actually we want to have EOG_FORWARD, but that doesn't
                // work
                object : IVisitor<Node>() {
                    fun visit(t: CallExpression) {
                        // TODO: actually, this should be a ConstructExpression, but currently, it
                        // is parsed as a CallExpression in the CPG
                        if (t.name.localName == "MongoClient") {
                            handleClientCreate(result, t, app)
                        }
                    }
                }
            )

            // There is no need for us to continue, if we have not found any clients
            // FIXME: clients is empty because of the error described in line 112
            if (clients.isEmpty()) {
                log.info("Found no clients in {}, we are not processing this any further", tu.name)
                continue
            }

            tu.accept(
                Strategy::AST_FORWARD, // actually we want to have EOG_FORWARD, but that doesn't
                // work
                object : IVisitor<Node>() {
                    fun visit(t: MemberExpression) {
                        // We are interested in member expression to a base that is in our clients
                        // map. This means that
                        // a database object is created from this client.
                        clients[t.base]?.let { handleDBObjectCreate(result, t, app, it) }
                    }
                }
            )

            tu.accept(
                Strategy::AST_FORWARD, // actually we want to have EOG_FORWARD, but that doesn't
                // work
                object : IVisitor<Node>() {
                    fun visit(t: MemberExpression) {
                        // The process is then repeated for a database object, to create a
                        // collections object.
                        dbs[t.base]?.let { handleCollectionsObjectCreate(result, t, app, it) }
                    }
                }
            )

            log.debug(
                "Trying to find queries to collections. We have {} references to collections",
                collections.size
            )

            tu.accept(
                Strategy::AST_FORWARD, // actually we want to have EOG_FORWARD, but that doesn't
                // work
                object : IVisitor<Node>() {
                    fun visit(t: MemberCallExpression) {
                        collections[t.base!!]?.let { handleQuery(result, t, app, it) }
                    }
                }
            )
        }
    }

    private fun handleClientCreate(
        result: TranslationResult,
        call: CallExpression,
        app: Application?
    ) {
        // resolve the connection string
        val connectionString =
            PythonValueResolver(app).resolve(call.arguments.firstOrNull()) as? String

        // and parse it as a URI
        val uri = connectionString?.let { URI(it) }

        // create a new DB operation (connect)
        val connect = createDatabaseConnect(result, uri?.host ?: "", call, app)

        // the DFG target of this call expression is the client, we are interested in
        // FIXME: this is not true for the new CPG version -> bug?
        //  CPG should usually return the associated VariableDeclaration as the next DFG Edge
        //  However, we don't get any values at all for prev/next EOG/DFG edges
        if (call.nextDFG.iterator().hasNext()) {
            val target = call.nextDFG.iterator().next()
            storeDeclarationOrReference(clients, target, connect)
        } else {
            System.err.println("Failed to resolve target of Client creation")
        }
    }

    private fun handleDBObjectCreate(
        t: TranslationResult,
        memberExpression: MemberExpression,
        app: Application?,
        connect: DatabaseConnect
    ) {
        // a connect operation could potentially connect to multiple services in the graph, e.g. if
        // the services are reachable by proxies, or are clustered. Thus technically, we have
        // multiple database storage nodes (one per service)
        val storages =
            connect.to?.map { it.getStorageOrCreate(memberExpression.name.localName) }
                ?: emptyList()

        // the DFG target of this member expression is the DB object, we are interested in
        val target = memberExpression.nextDFG.iterator().next()

        // store a pair of the connect operation and possible storage(s)
        storeDeclarationOrReference(dbs, target, Pair(connect, storages))

        log.debug("Storing reference to mongo DB object: {}", memberExpression.name)
    }

    private fun handleCollectionsObjectCreate(
        t: TranslationResult,
        memberExpression: MemberExpression,
        app: Application?,
        pair: Pair<DatabaseConnect, List<DatabaseStorage>>
    ) {
        val (connect, _) = pair

        // again, multiple services, multiple storages
        val storages =
            connect.to?.map {
                val clPair = dbs[memberExpression.base]
                // name should be the same in all storages of all the dbs of all the clients of all
                // the services
                val parentName = clPair?.second?.firstOrNull()?.name

                it.getStorageOrCreate(memberExpression.name.localName, parentName?.localName)
            }
                ?: emptyList()

        // the DFG target of this member expression is the DB object, we are interested in
        val target = memberExpression.nextDFG.iterator().next()

        // store a pair of the connect operation and possible storage(s)
        storeDeclarationOrReference(collections, target, Pair(connect, storages))

        log.debug("Storing reference to mongo collection object: {}", memberExpression.name)
    }

    private fun handleQuery(
        t: TranslationResult,
        mce: MemberCallExpression,
        app: Application?,
        pair: Pair<DatabaseConnect, List<DatabaseStorage>>
    ) {
        val (connect, storage) = pair
        var op: DatabaseQuery? = null
        if (mce.name.localName == "insert_one") {
            op = createDatabaseQuery(t, true, connect, storage, listOf(mce), app)

            // data flows from first argument to op
            mce.arguments.firstOrNull()?.addNextDFG(op)
        }

        if (mce.name.localName == "find" || mce.name.localName == "find_one") {
            op = createDatabaseQuery(t, false, connect, storage, listOf(mce), app)
            // data flows from first argument to op
            mce.arguments.firstOrNull()?.addNextDFG(op)

            // and towards the DFG target(s) of the call
            mce.nextDFG.forEach { op.addNextDFG(it) }
        }

        if (op != null) {
            op.name = mce.name
        }
    }

    override fun cleanup() {
        // nothing to do
    }
}
