package io.clouditor.graph.testing

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.passes.Pass
import io.clouditor.graph.*
import io.clouditor.graph.passes.golang.appendPath
import java.nio.file.Files

class LocalTestingPass : Pass() {

    override fun accept(t: TranslationResult) {
        val applications = listOf(App.rootPath)

        for (rootPath in applications) {
            // TODO find solution for python/go folder structure
            val workflowPath = rootPath.resolve("config")
            workflowPath.toFile().walkTopDown().iterator().forEach { file ->
                if (file.extension == "yml") {
                    val mapper = ObjectMapper(YAMLFactory())
                    mapper.registerModule(KotlinModule())

                    Files.newBufferedReader(file.toPath()).use {
                        val config = mapper.readValue(it, TestConfig::class.java)
                        handleConf(config, t)
                        // val handler = WorkflowHandler(t, rootPath)
                        // handler.handleWorkflow(workflow)
                    }
                }
            }
        }
    }

    private fun handleConf(conf: TestConfig, t: TranslationResult) {
        val controllers =
            t.additionalNodes.filter { it is HttpRequestHandler }.map { it as HttpRequestHandler }

        // TODO map / foreach
        for (service in conf.services) {
            if (service.type == "server") {
                for (controller in controllers) {
                    for (endpoint in controller.httpEndpoints) {
                        var url = service.host?.appendPath(controller.path)
                        url = url?.appendPath(endpoint.path)

                        val proxy =
                            ProxiedEndpoint(
                                listOf(endpoint),
                                NoAuthentication(),
                                endpoint.handler,
                                endpoint.method,
                                endpoint.path,
                                null,
                                url
                            )
                        proxy.addNextDFG(endpoint)
                        t += proxy
                    }
                }
            }
        }

        for (service in conf.services) {
            if (service.name == "postgres") {
                val db =
                    RelationalDatabaseService(
                        mutableListOf<DatabaseStorage>(),
                        null,
                        null,
                        null,
                        null,
                        mapOf()
                    )
                db.name = service.name
                t += db
            }
            if (service.name == "mongo") {
                val db =
                    DocumentDatabaseService(
                        mutableListOf<DatabaseStorage>(),
                        null,
                        null,
                        null,
                        null,
                        mapOf()
                    )
                db.name = service.name
                t += db
            }
        }
    }

    override fun cleanup() {}
}
