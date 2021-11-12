package io.clouditor.graph.passes.js

import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.graph.declarations.TranslationUnitDeclaration
import de.fraunhofer.aisec.cpg.graph.statements.expressions.CallExpression
import de.fraunhofer.aisec.cpg.graph.statements.expressions.Expression
import de.fraunhofer.aisec.cpg.graph.statements.expressions.InitializerListExpression
import de.fraunhofer.aisec.cpg.graph.statements.expressions.KeyValueExpression
import de.fraunhofer.aisec.cpg.processing.IVisitor
import de.fraunhofer.aisec.cpg.processing.strategy.Strategy
import io.clouditor.graph.App
import io.clouditor.graph.ValueResolver
import io.clouditor.graph.findApplicationByTU
import io.clouditor.graph.passes.HttpClientPass
import java.nio.file.Files

class FetchPass : HttpClientPass() {
    var map = mutableMapOf<String, String>()

    override fun accept(t: TranslationResult) {
        val applications = listOf(App.rootPath)

        for (rootPath in applications) {
            val envPath = rootPath.resolve("frontend").resolve(".env.production")

            envPath.toFile().walkTopDown().iterator().forEach { file ->
                Files.newBufferedReader(file.toPath()).use { reader ->
                    reader.readLines().forEach {
                        val keyValue = it.split(" = ")
                        val key = keyValue[0]
                        val url = keyValue[1].trim('\"')
                        map["env_$key"] = url
                    }
                }
            }
        }

        for (tu in t.translationUnits) {
            tu.accept(
                Strategy::AST_FORWARD,
                object : IVisitor<Node?>() {
                    fun visit(call: CallExpression) {
                        handleCallExpression(t, tu, call)
                    }
                }
            )
        }
    }

    private fun handleCallExpression(
        t: TranslationResult,
        tu: TranslationUnitDeclaration,
        call: CallExpression
    ) {
        if (call.name == "fetch") {
            handleFetch(t, tu, call)
        }
    }

    private fun handleFetch(
        t: TranslationResult,
        tu: TranslationUnitDeclaration,
        call: CallExpression
    ) {
        val app = t.findApplicationByTU(tu)
        // add env vars as labels
        app?.runsOn?.forEach { it.labels = it.labels + map }
        // first parameter is the URL
        val url = JSValueResolver(app).resolve(call.arguments.first())
        // second parameter contains (optional) options
        val method = getMethod(call.arguments.getOrNull(1) as? InitializerListExpression)
        // TODO refactor this when stringify is resolved correctly in the cpg
        val body = getBody(call.arguments.getOrNull(1) as InitializerListExpression)
        createHttpRequest(t, url as String, call, method, body, app)
    }

    private fun getMethod(options: InitializerListExpression?): String {
        var method = "GET"

        // for now, assume that options is an object construct. it could also be a reference to
        // one we could extend the variable resolver to return a hashmap

        ValueResolver()
            .resolve(
                options?.initializers?.firstOrNull {
                    it is KeyValueExpression && it.key?.name == "method"
                }
            )
            ?.let { method = it as String }

        return method
    }

    private fun getBody(options: InitializerListExpression?): Expression {
        var body = Expression()

        ValueResolver()
            .resolve(
                options?.initializers?.firstOrNull {
                    it is KeyValueExpression && it.key?.name == "body"
                }
            )
        // TODO this results in a string right now because stringify is not parsed correctly
        // ?.let { body = it as Expression }
        // return body
        return Expression()
    }

    override fun cleanup() {
        // nothing to do
    }
}
