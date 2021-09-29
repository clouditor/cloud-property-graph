package io.clouditor.graph.passes.js

import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.graph.declarations.TranslationUnitDeclaration
import de.fraunhofer.aisec.cpg.graph.statements.expressions.CallExpression
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
            // TODO use regex
            val envPath = rootPath.resolve("frontend").resolve(".env.production")
            envPath.toFile().walkTopDown().iterator().forEach { file ->
                Files.newBufferedReader(file.toPath()).use {
                    it.readLines().forEach {
                        val key = it.split(" = ")?.get(0)
                        val url = it.split(" = ")?.get(1)
                        map.put(key, url)
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
        var url = JSValueResolver(app).resolve(call.arguments.first())

        // second parameter contains (optional) options
        val method = getMethod(call.arguments.getOrNull(1) as? InitializerListExpression)

        createHttpRequest(t, url as String, call, method, app)
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

    override fun cleanup() {
        // nothing to do
    }
}
