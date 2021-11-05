package io.clouditor.graph.passes.java

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.graph.declarations.RecordDeclaration
import de.fraunhofer.aisec.cpg.graph.declarations.TranslationUnitDeclaration
import de.fraunhofer.aisec.cpg.graph.statements.expressions.MemberCallExpression
import de.fraunhofer.aisec.cpg.graph.types.Type
import de.fraunhofer.aisec.cpg.graph.types.TypeParser
import de.fraunhofer.aisec.cpg.graph.types.UnknownType
import de.fraunhofer.aisec.cpg.processing.IVisitor
import de.fraunhofer.aisec.cpg.processing.strategy.Strategy
import de.fraunhofer.aisec.cpg.sarif.PhysicalLocation
import de.fraunhofer.aisec.cpg.sarif.Region
import io.clouditor.graph.*
import io.clouditor.graph.nodes.getStorageOrCreate
import io.clouditor.graph.passes.DatabaseOperationPass
import java.net.URI
import java.nio.file.Files

class SpringDatabasePass : DatabaseOperationPass() {
    override fun accept(t: TranslationResult) {
        var topLevel = t.translationManager.config.topLevel

        val app = t.translationUnits.firstOrNull()?.let { t.findApplicationByTU(it) }

        app?.let {
            // look for properties file (hardcoded for now)
            // val file = topLevel.resolve(app.name).resolve("src/main/resources/application.yml")
            val file =
                topLevel.resolve("deployments/docker-compose/nurse_api_config/application.yml")

            val mapper = ObjectMapper(YAMLFactory())
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            mapper.registerModule(KotlinModule.Builder().build())

            Files.newBufferedReader(file.toPath()).use {
                val properties = mapper.readValue(it, SpringApplicationProperties::class.java)

                val url = URI.create(properties.spring.datasource.url.substring(5))

                val connect = createDatabaseConnect(t, url.host, null, app)
                connect.location = PhysicalLocation(file.toURI(), Region())

                t += connect

                for (tu in t.translationUnits) {
                    // we need to find the repository classes
                    tu.accept(
                        Strategy::AST_FORWARD,
                        object : IVisitor<Node?>() {
                            fun visit(record: RecordDeclaration) {
                                findConnect(t, tu, record, app, connect)
                            }

                            fun visit(call: MemberCallExpression) {
                                findQuery(t, tu, call, app, connect)
                            }
                        }
                    )
                }
            }
        }
    }

    private fun findQuery(
        t: TranslationResult,
        tu: TranslationUnitDeclaration?,
        call: MemberCallExpression,
        app: Application,
        connect: DatabaseConnect
    ) {
        if (call.name == "save") {
            // first argument is the object to save
            val toStore = call.arguments.firstOrNull()

            connect.to.forEach {
                val storage =
                    it.getStorageOrCreate(deriveName(toStore?.type ?: UnknownType.getUnknownType()))

                val query =
                    createDatabaseQuery(t, true, connect, listOf(storage), listOf(call), app)
    query.name = "save"
                
                t += query
            }

            println(call)
        }
    }

    private fun findConnect(
        t: TranslationResult,
        tu: TranslationUnitDeclaration?,
        record: RecordDeclaration,
        app: Application?,
        op: DatabaseConnect
    ) {
        var crudType =
            TypeParser.createFrom("org.springframework.data.repository.CrudRepository", false)

        if (record.superClasses.contains(crudType)) {
            // a little bit of a hack, since we cannot get the generics from the CrudRepository
            val type =
                record.methods
                    .firstOrNull { it.name == "findById" && it.type !is UnknownType }
                    ?.type

            if (type != null) {
                op.to.forEach {
                    val storage = it.getStorageOrCreate(deriveName(type))
                }
            }
        }
    }

    private fun deriveName(type: Type): String {
        // short name
        val shortName = type.name.substringAfterLast(".")

        return shortName.toLowerCase() + "s"
    }

    override fun cleanup() {}
}
