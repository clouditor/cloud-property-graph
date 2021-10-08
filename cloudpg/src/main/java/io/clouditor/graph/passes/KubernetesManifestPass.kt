package io.clouditor.graph.passes

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.passes.Pass
import io.clouditor.graph.App
import io.clouditor.graph.kubernetes.Manifest
import io.clouditor.graph.kubernetes.ManifestHandler
import java.nio.file.Files

class KubernetesManifestPass : Pass() {
    override fun cleanup() {}

    override fun accept(t: TranslationResult) {
        val applications = listOf(App.rootPath)

        for (rootPath in applications) {
            val workflowPath = rootPath.resolve("kubernetes")

            workflowPath.toFile().walkTopDown().iterator().forEach { file ->
                if (file.extension == "yml") {
                    val mapper = ObjectMapper(YAMLFactory())
                    mapper.registerModule(KotlinModule())
                    val handler = ManifestHandler(t, rootPath)

                    Files.newBufferedReader(file.toPath()).use {
                        // split file into several manifest files
                        var man = ""
                        val it = file.readLines().iterator()
                        for (line in it) {
                            if (line == "---" || !it.hasNext()) {
                                val manifest = mapper.readValue(man, Manifest::class.java)
                                handler.handleManifest(manifest)
                                man = ""
                            } else {
                                man += line + "\n"
                            }
                        }
                    }
                }
            }
        }
    }
}
