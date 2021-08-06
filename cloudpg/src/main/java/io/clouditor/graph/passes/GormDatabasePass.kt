package io.clouditor.graph.passes

import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.graph.declarations.TranslationUnitDeclaration
import de.fraunhofer.aisec.cpg.graph.statements.expressions.CallExpression
import de.fraunhofer.aisec.cpg.graph.statements.expressions.Expression
import de.fraunhofer.aisec.cpg.graph.statements.expressions.MemberCallExpression
import de.fraunhofer.aisec.cpg.graph.types.PointerType
import de.fraunhofer.aisec.cpg.processing.IVisitor
import de.fraunhofer.aisec.cpg.processing.strategy.Strategy
import io.clouditor.graph.*

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
                    val op = createDatabaseConnect(result, host, call, app)

                    result += op
                    app?.functionalitys?.plusAssign(op)
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
        // make sure, the base call is really to a gorm DB object
        if (call.base.type is PointerType && call.base.type.name == "gorm.DB*") {
            if (call.name == "Where" || call.name == "Find") {
                val op =
                    app?.functionalitys?.filterIsInstance<DatabaseConnect>()?.firstOrNull()?.let {
                        createDatabaseQuery(result, false, it, call, app)
                    }

                if (op != null) {
                    op.location = call.location
                    op.name = "SELECT"
                    result += op
                    app.functionalitys?.plusAssign(op)
                }
            }
        }
    }

    override fun cleanup() {}

    fun resolveDSN(expr: Expression, app: Application?): Any? {
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
                        if (node.name == "Sprintf") {
                            val str = resolver.resolve(node.arguments.firstOrNull()) as String
                            val arguments = node.arguments.drop(1)

                            return@ValueResolver str.format(
                                *arguments.map { resolver.resolve(it) }.toTypedArray()
                            )
                        }

                        // a little bit of a hack, this is specific to our use case, since the
                        // stdlib doesnt have that built-in
                        if (node.name == "EnvOrDefault") {
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
