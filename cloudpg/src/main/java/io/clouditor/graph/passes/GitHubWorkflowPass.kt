package io.clouditor.graph.passes

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import de.fraunhofer.aisec.cpg.TranslationContext
import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.passes.TranslationResultPass
import io.clouditor.graph.App
import io.clouditor.graph.github.Workflow
import io.clouditor.graph.github.WorkflowHandler
import java.nio.file.Files

class GitHubWorkflowPass(ctx: TranslationContext) : TranslationResultPass(ctx) {
    override fun cleanup() {}

    override fun accept(t: TranslationResult) {
        val applications = listOf(App.rootPath)

        for (rootPath in applications) {
            val workflowPath = rootPath.resolve(".github").resolve("workflows")

            workflowPath.toFile().walkTopDown().iterator().forEach { file ->
                if (file.extension == "yml") {
                    val mapper = ObjectMapper(YAMLFactory())
                    mapper.registerModule(KotlinModule())

                    Files.newBufferedReader(file.toPath()).use {
                        val workflow = mapper.readValue(it, Workflow::class.java)

                        val handler = WorkflowHandler(t, rootPath)
                        handler.handleWorkflow(workflow)
                    }
                }
            }
        }
    }
}
