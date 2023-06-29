package io.clouditor.graph.github

import com.azure.core.management.Region
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.graph.Name
import io.clouditor.graph.*
import io.clouditor.graph.docker.DockerCompose
import io.clouditor.graph.nodes.Builder
import io.clouditor.graph.nodes.location
import io.clouditor.graph.passes.locationForRegion
import java.lang.IllegalArgumentException
import java.nio.file.Files
import java.nio.file.Path

class WorkflowHandler(private val result: TranslationResult, val rootPath: Path) {

    fun handleWorkflow(workflow: Workflow) {
        workflow.jobs.values.forEach { handleJob(it, workflow) }
    }

    private fun handleJob(job: Job, workflow: Workflow) {
        job.steps.forEach { handleStep(it, job, workflow) }
    }

    private fun handleStep(step: Step, job: Job, workflow: Workflow) {
        var path = rootPath.toString()
        workflow.on.push?.get("paths")?.let {
            // little bit of hack now, until we would support multi-modules
            path = rootPath.resolve(it.first()).toString()
        }

        // if the step has a working directory, append that to the path
        step.workingDirectory?.let { path = rootPath.resolve(it).toString() }

        step.run?.let { run ->
            val commands = run.lines()

            commands
                .firstOrNull { it.trim().startsWith("scp") && it.contains("docker-compose") }
                ?.let { command ->
                    val application =
                        result.additionalNodes.filterIsInstance(Application::class.java)
                            .firstOrNull { it.name.localName == Path.of(path).fileName.toString() }
                    val rr = command.split(" ")

                    // look for the host
                    val host = rr.firstOrNull { it.contains("@") }?.split('@', ':')?.get(1)

                    // look for the path
                    val composePath = rr.firstOrNull { it.contains("docker-compose.yml") }

                    if (composePath != null && host != null) {
                        // just create a VM for now, since we do not have a AWS pass (VM is in US)
                        val compute =
                            VirtualMachine(
                                null,
                                null,
                                null,
                                null,
                                null,
                                null, // TODO(all): NetworkInterface missing
                                result.locationForRegion(Region.US_EAST),
                                mutableMapOf()
                            )
                        compute.name = Name(host)
                        application?.runsOn?.plusAssign(compute)

                        result += compute

                        val mapper = ObjectMapper(YAMLFactory())
                        mapper.registerModule(
                            KotlinModule.Builder()
                                .withReflectionCacheSize(512)
                                .configure(KotlinFeature.NullToEmptyCollection, false)
                                .configure(KotlinFeature.NullToEmptyMap, false)
                                .configure(KotlinFeature.NullIsSameAsDefault, false)
                                .configure(KotlinFeature.SingletonSupport, false)
                                .configure(KotlinFeature.StrictNullChecks, false)
                                .build()
                        )

                        Files.newBufferedReader(rootPath.resolve(composePath)).use { reader ->
                            val compose = mapper.readValue(reader, DockerCompose::class.java)

                            for (pair in compose.services) {
                                pair.value.ports.forEach {
                                    val port = it.split(":").first().toShort()
                                    val networkService =
                                        NetworkService(
                                            compute,
                                            ArrayList(listOf(host)),
                                            listOf(),
                                            ArrayList(listOf(port)),
                                            null,
                                            compute.geoLocation,
                                            mutableMapOf()
                                        )
                                    networkService.name = Name(host)

                                    result += networkService
                                }
                            }

                            println(compose)
                        }
                    }
                }

            commands.firstOrNull { it.trim().startsWith("docker push") }?.let { command ->
                val name =
                    doEnv(command.substringAfter("docker push").split(":").first().trim(), step)
                        .trim('"')

                // filter out the translation units belonging to these applications, until cpg#341
                // is
                // solved
                val translationUnits =
                    result.components.stream().flatMap { it.translationUnits.stream() }.toList()
                val tus =
                    translationUnits.filter {
                        val tuPath = Path.of(it.name.localName)

                        try {
                            tuPath.startsWith(Path.of(path).toAbsolutePath().normalize()) ||
                                tuPath.startsWith(path)
                        } catch (e: IllegalArgumentException) {
                            false
                        }
                    }

                // create a new application based on the path
                // TODO: Use component from the new CPG API
                val application =
                    Application(
                        mutableListOf(),
                        "Java",
                        mutableListOf(),
                        tus,
                    )
                application.name = Name(Path.of(path).fileName.toString())

                result.additionalNodes += application

                // we need to assume, that GH stores its images in the US
                val image = Image(application, result.location("US"), mapOf())
                image.name = Name(name)

                result.images += image

                val builder = Builder(mutableListOf(image))
                step.name?.let { builder.name = Name(it) }

                result.builders += builder

                // build a DFG node from the builder to the image
                builder.addNextDFG(image)
            }
        }
    }

    private fun doEnv(n: String, step: Step): String {
        var name = n

        step.env?.entries?.forEach { name = name.replace("\${${it.key}}", it.value, false) }

        return name
    }
}
