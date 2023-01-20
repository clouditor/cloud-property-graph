/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package io.clouditor.graph

import de.fraunhofer.aisec.cpg.TranslationConfiguration
import de.fraunhofer.aisec.cpg.TranslationManager
import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.frontends.golang.GoLanguage
import de.fraunhofer.aisec.cpg.frontends.python.PythonLanguage
import de.fraunhofer.aisec.cpg.frontends.typescript.JavaScriptLanguage
import de.fraunhofer.aisec.cpg.frontends.typescript.TypeScriptLanguage
import de.fraunhofer.aisec.cpg.graph.Name
import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.graph.allChildren
import de.fraunhofer.aisec.cpg.graph.declarations.TranslationUnitDeclaration
import de.fraunhofer.aisec.cpg.helpers.Benchmark
import io.clouditor.graph.nodes.Builder
import io.clouditor.graph.passes.*
import io.clouditor.graph.passes.golang.*
import io.clouditor.graph.passes.java.JaxRsClientPass
import io.clouditor.graph.passes.java.JaxRsPass
import io.clouditor.graph.passes.java.SpringBootPass
import io.clouditor.graph.passes.js.FetchPass
import io.clouditor.graph.passes.js.JSHttpPass
import io.clouditor.graph.passes.python.*
import io.clouditor.graph.passes.ruby.WebBrickPass
import io.clouditor.graph.testing.LocalTestingPass
import java.nio.file.Path
import java.util.concurrent.Callable
import kotlin.system.exitProcess
import org.neo4j.ogm.config.Configuration
import org.neo4j.ogm.session.SessionFactory
import picocli.CommandLine

@CommandLine.Command(
    name = "cloud-property-graph",
    mixinStandardHelpOptions = true,
    description = ["Builds the Cloud Property Graph and persists it into a graph database."]
)
object App : Callable<Int> {
    @CommandLine.Option(
        names = ["-k8s-n", "--kubernetes-namespaces"],
        description = ["The Kubernetes namespace to filter"]
    )
    var kubernetesNamespace = "default"

    @CommandLine.Option(
        names = ["--azure-resource-group"],
        description = ["The Azure resource group to filter"]
    )
    var azureResourceGroup = "default"

    @CommandLine.Option(names = ["--root"], description = ["The root path"], required = true)
    lateinit var rootPath: Path

    @CommandLine.Option(names = ["--neo4j-password"], description = ["The Neo4j password"])
    var neo4jPassword: String = "password"

    @CommandLine.Option(
        names = ["--enable-labels"],
        description =
            [
                "Whether or not to enable attaching labels to the graph extracted from annotations or specific passes."]
    )
    var labelsEnabled: Boolean = false

    @CommandLine.Option(
        names = ["--local-mode"],
        description =
            [
                "Whether or not Kubernetes and GitHub workflow files should be parsed to check the deployed containers the application is running on."]
    )
    var localMode: Boolean = false

    @CommandLine.Parameters(index = "0..*") lateinit var paths: List<Path>

    override fun call(): Int {
        val configuration =
            Configuration.Builder()
                .uri("bolt://localhost")
                .autoIndex("none")
                .credentials("neo4j", neo4jPassword)
                .build()

        val sessionFactory =
            SessionFactory(configuration, "de.fraunhofer.aisec.cpg.graph", "io.clouditor.graph")
        val session = sessionFactory.openSession()

        val result = doTranslate()

        val nodes = mutableListOf<Node>()
        nodes.addAll(result.allChildren())
        nodes.addAll(result.translationUnits)
        nodes.addAll(result.images)
        nodes.addAll(result.builders)
        nodes.addAll(result.computes)
        nodes.addAll(result.translationUnits)
        nodes.addAll(result.additionalNodes)

        session.beginTransaction().use { transaction ->
            session.purgeDatabase()

            val b = Benchmark(App::class.java, "Saving nodes to database")
            session.save(nodes)
            b.stop()

            transaction.commit()
        }

        session.clear()
        sessionFactory.close()

        return 0
    }

    fun doTranslate(): TranslationResult {

        val builder =
            TranslationConfiguration.builder()
                .topLevel(rootPath.toFile())
                .sourceLocations(paths.map { rootPath.resolve(it).toFile() })
                .defaultPasses()
                .defaultLanguages()
                /*.registerLanguage(
                    RubyLanguageFrontend::class.java,
                    RubyLanguageFrontend.RUBY_EXTENSIONS
                )*/
                .registerLanguage<TypeScriptLanguage>()
                .registerLanguage<JavaScriptLanguage>()
                .registerLanguage<PythonLanguage>()
                .registerLanguage<GoLanguage>()
                .debugParser(true)
                .registerPass(GitHubWorkflowPass())
                .registerPass(SpringBootPass())
                .registerPass(JaxRsPass())
                .registerPass(GolangHttpPass())
                .registerPass(GinGonicPass())
                .registerPass(WebBrickPass())
                .registerPass(JSHttpPass())
                .registerPass(FlaskPass())
                .apply {
                    if (localMode) {
                        // register the localTestingPass after the HTTP Passes since it needs HTTP
                        // request handlers
                        registerPass(LocalTestingPass())
                        registerPass(GolangHttpRequestPass())
                    } else {
                        registerPass(AzurePass())
                        registerPass(AzureClientSDKPass())
                        registerPass(KubernetesPass())
                        registerPass(IngressInvocationPass())
                    }
                }
                .registerPass(CryptographyPass())
                .registerPass(GoCryptoPass())
                .registerPass(JaxRsClientPass())
                .registerPass(FetchPass())
                .registerPass(RequestsPass())
                .registerPass(PythonLogPass())
                .registerPass(GolangLogPass())
                .registerPass(GormDatabasePass())
                .registerPass(PyMongoPass())
                .registerPass(Psycopg2Pass())
                .processAnnotations(true)

        if (labelsEnabled) {
            val edgesCache = BidirectionalEdgesCachePass()
            val labelPass = LabelExtractionPass()
            labelPass.edgesCachePass = edgesCache
            builder
                .registerPass(DFGExtensionPass())
                .registerPass(edgesCache)
                .registerPass(labelPass)
                .matchCommentsToNodes(true)
        }

        val config = builder.build()

        val analyzer = TranslationManager.builder().config(config).build()
        val o = analyzer.analyze()

        return o.get()
    }
}

fun main(args: Array<String>): Unit = exitProcess(CommandLine(App).execute(*args))

val TranslationResult.images: MutableList<Image>
    get() = this.scratch.computeIfAbsent("images") { mutableListOf<Image>() } as MutableList<Image>

val TranslationResult.builders: MutableList<Builder>
    get() =
        this.scratch.computeIfAbsent("builders") { mutableListOf<Builder>() } as
            MutableList<Builder>

val TranslationResult.computes: MutableList<Compute>
    get() =
        this.scratch.computeIfAbsent("computes") { mutableListOf<Compute>() } as
            MutableList<Compute>

val TranslationResult.additionalNodes: MutableList<Node>
    get() =
        this.scratch.computeIfAbsent("additionalNodes") { mutableListOf<Node>() } as
            MutableList<Node>

fun TranslationResult.findApplicationByTU(tu: TranslationUnitDeclaration): Application? {
    return this.additionalNodes.filterIsInstance(Application::class.java).firstOrNull {
        it.translationUnits.contains(tu)
    }
}

operator fun TranslationResult.plusAssign(node: Node) {
    this.additionalNodes += node
}

infix fun Name?.leq(s: String): Boolean {
    return this?.localName == s
}
