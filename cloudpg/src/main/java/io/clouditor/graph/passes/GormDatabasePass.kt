package io.clouditor.graph.passes

import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.graph.declarations.TranslationUnitDeclaration
import de.fraunhofer.aisec.cpg.graph.statements.expressions.CallExpression
import de.fraunhofer.aisec.cpg.graph.statements.expressions.Expression
import de.fraunhofer.aisec.cpg.graph.statements.expressions.MemberCallExpression
import de.fraunhofer.aisec.cpg.graph.statements.expressions.UnaryOperator
import de.fraunhofer.aisec.cpg.graph.types.PointerType
import de.fraunhofer.aisec.cpg.graph.types.Type
import de.fraunhofer.aisec.cpg.processing.IVisitor
import de.fraunhofer.aisec.cpg.processing.strategy.Strategy
import io.clouditor.graph.*
import io.clouditor.graph.nodes.getStorageOrCreate

class GormDatabasePass : DatabaseOperationPass() {
    override fun accept(t: TranslationResult) {
        for (tu in t.translationUnits) {
            val app = t.findApplicationByTU(tu)

            // we need to find the connect first
            tu.accept(
                Strategy::AST_FORWARD,
                object : IVisitor<Node?>() {
                    fun visit(call: CallExpression) {
                        findConnect(t, tu, call, app)
                    }
                }
            )
        }

        for (tu in t.translationUnits) {
            val app = t.findApplicationByTU(tu)

            tu.accept(
                Strategy::AST_FORWARD,
                object : IVisitor<Node?>() {
                    fun visit(call: MemberCallExpression) {
                        findQuery(t, tu, call, app)
                    }
                }
            )
        }
    }

    private fun findConnect(
        result: TranslationResult,
        tu: TranslationUnitDeclaration,
        call: CallExpression,
        app: Application?
    ) {
        if (call.fqn == "postgres.Open") {
            call.arguments.firstOrNull()?.let { expr ->
                val dsn = resolveDSN(expr, app) as? String

                // split DSN into key/value pairs
                val map =
                    dsn?.split(" ")?.associate {
                        Pair(it.substringBefore("="), it.substringAfter("="))
                    }

                val host = map?.get("host")
                var port = map?.get("port")?.toShortOrNull() ?: 5432

                if (host != null) {
                    createDatabaseConnect(result, host, call, app)
                }
            }
        }
    }

    private fun findQuery(
        result: TranslationResult,
        tu: TranslationUnitDeclaration,
        call: MemberCallExpression,
        app: Application?
    ) {
        // it can either be a direct call, without any chained selectors, such as Where
        val directCall =
            call.base?.type is PointerType && call.base?.type?.name?.localName == "gorm.DB*"

        // make sure, the base call is really to a gorm DB object
        if (call.name.localName == "First" || call.name.localName == "Find") {
            val calls = mutableListOf<CallExpression>(call)

            if (!directCall) {
                // if it is not a direct call, lets see, whether we have a chain of member calls
                // that go
                // to the base. We store the list of calls for future reference
                var memberCall: MemberCallExpression? = call.base as? MemberCallExpression
                var found = false
                while (memberCall != null) {
                    // add the call to the list of chained calls
                    calls += memberCall

                    // check, if its base is already of our database type
                    if (memberCall.base?.type is PointerType &&
                            memberCall.base?.type?.name?.localName == "gorm.DB*"
                    ) {
                        // found it, yay!
                        found = true

                        // we can break immediately
                        break
                    }

                    // otherwise, go to the next base
                    memberCall = memberCall.base as? MemberCallExpression
                }

                if (!found) {
                    // we did not find a link to a gorm.DB*, this is something else, ignore it
                    return
                }
            }

            val op =
                app?.functionalities?.filterIsInstance<DatabaseConnect>()?.firstOrNull()?.let {
                    val op = createDatabaseQuery(result, false, it, mutableListOf(), calls, app)
                    op.name = call.name

                    // loop through the calls and set DFG edges
                    calls.forEach {
                        when (it.name.localName) {
                            "First" -> handleFirst(it, op)
                            "Where" -> handleWhere(it, op)
                            "Find" -> handleFind(it, op)
                        }
                    }

                    op
                }

            if (op != null) {
                op.location = call.location
            }
        } else if (call.name.localName == "Create") {
            val op =
                app?.functionalities?.filterIsInstance<DatabaseConnect>()?.firstOrNull()?.let {
                    val op =
                        createDatabaseQuery(
                            result,
                            false,
                            it,
                            mutableListOf(),
                            mutableListOf(call),
                            app
                        )
                    op.name = call.name

                    handleCreate(call, op)

                    op
                }
            if (op != null) {
                op.location = call.location
            }
        } else if (call.name.localName == "Update") {
            val op =
                app?.functionalities?.filterIsInstance<DatabaseConnect>()?.firstOrNull()?.let {
                    val op =
                        createDatabaseQuery(
                            result,
                            true,
                            it,
                            mutableListOf(),
                            mutableListOf(call),
                            app
                        )
                    op.name = call.name

                    handleUpdate(call, op)

                    op
                }
            if (op != null) {
                op.location = call.location
            }
        }
    }

    private fun handleFind(call: CallExpression, op: DatabaseQuery) {
        // find should have at last one argument, the first is the target, second is an optional
        // where specifier. A little trick: follow the same approach as for First, first
        handleFirst(call, op)

        // then, check if we have a second argument
        call.arguments.getOrNull(1)?.let {
            // add it as an incoming DFG edge
            op.addPrevDFG(it)
        }
    }

    private fun handleWhere(call: CallExpression, op: DatabaseQuery) {
        // simply add all arguments as incoming DFG edges
        call.arguments.forEach { op.addPrevDFG(it) }
    }

    private fun handleFirst(call: CallExpression, op: DatabaseQuery) {
        // first should have one argument, that specifies the variable in which it is
        // stored
        var target = call.arguments.firstOrNull()

        // it could be wrapped in a UnaryExpression, if a non-pointer object was
        // supplied. this
        // is actually something we should fix at the CPG level
        if (target is UnaryOperator) {
            target = target.input
        }

        // add a DFG edge towards our target
        if (target != null) {
            op.addNextDFG(target)

            // add storage
            op.to.forEach {
                val storage = it.getStorageOrCreate(deriveName(target.type))
                op.storage.add(storage)

                // also add DFG edges from or towards the storage, depending on the query type
                if (op.isModify) {
                    op.addNextDFG(storage)
                } else {
                    op.addPrevDFG(storage)
                }
            }
        }
    }

    private fun handleCreate(call: CallExpression, op: DatabaseQuery) {
        // create should have one argument, that specifies the object which is stored
        var target = call.arguments.firstOrNull()

        // add a DFG edge towards our target
        if (target != null) {
            target.addNextDFG(op)

            // add storage
            op.to.forEach {
                val storage = it.getStorageOrCreate(deriveName(target.type))
                op.storage.add(storage)

                op.addNextDFG(storage)
            }
        }
    }

    private fun handleUpdate(call: CallExpression, op: DatabaseQuery) {
        var target = call.arguments.firstOrNull()
        // for update calls, a model condition may be specified which has the actual target
        if (call.base.name == "Model") {
            target = (call.base as CallExpression).arguments.first()
        }

        // add a DFG edge towards our target
        if (target != null) {
            target.addNextDFG(op)

            // add storage
            op.to.forEach {
                val storage = it.getStorageOrCreate(deriveName(target.type))
                op.storage.add(storage)

                // also add DFG edge towards the storage
                op.addNextDFG(storage)
            }
        }
    }

    private fun deriveName(type: Type): String {
        // short name
        val shortName = type.name.toString().substringAfterLast(".").substringBefore("*")

        return shortName.toLowerCase() + "s"
    }

    override fun cleanup() {}

    private fun resolveDSN(expr: Expression, app: Application?): Any? {
        // assume that we are only on one client
        val env =
            app?.runsOn?.firstOrNull()?.labels?.filter { it.key.startsWith("env_") }?.mapKeys {
                it.key.substring(4)
            }
                ?: mutableMapOf()

        return ValueResolver { node, resolver ->
                when (node) {
                    is CallExpression -> {
                        // support for some special calls, i.e. format
                        if (node.name.localName == "Sprintf") {
                            val str = resolver.resolve(node.arguments.firstOrNull()) as String
                            val arguments = node.arguments.drop(1)

                            return@ValueResolver str.format(
                                *arguments.map { resolver.resolve(it) }.toTypedArray()
                            )
                        }

                        // a little bit of a hack, this is specific to our use case, since the
                        // stdlib doesnt have that built-in
                        if (node.name.localName == "EnvOrDefault") {
                            // environment lookup on python
                            val key = resolver.resolve(node.arguments.firstOrNull())

                            return@ValueResolver env[key] ?: resolver.resolve(node.arguments[1])
                        }

                        // return placeholder
                        return@ValueResolver "{${node.name}()}"
                    }
                    else -> return@ValueResolver "{${node?.name}}"
                }
            }
            .resolve(expr)
    }
}
