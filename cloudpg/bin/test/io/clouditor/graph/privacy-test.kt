package io.clouditor.graph

import de.fraunhofer.aisec.cpg.graph.labels
import de.fraunhofer.aisec.cpg.sarif.PhysicalLocation
import de.fraunhofer.aisec.cpg.sarif.Region
import java.net.URI
import kotlin.io.path.Path
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.fail
import org.junit.jupiter.api.BeforeEach
import org.neo4j.driver.internal.InternalPath
import org.neo4j.ogm.config.Configuration
import org.neo4j.ogm.session.SessionFactory
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xmlunit.builder.Input
import org.xmlunit.xpath.JAXPXPathEngine

class PrivacyTest {
    val configuration =
        Configuration.Builder()
            .uri("bolt://localhost")
            .autoIndex("none")
            .credentials("neo4j", App.neo4jPassword)
            .build()

    val sessionFactory =
        SessionFactory(configuration, "de.fraunhofer.aisec.cpg.graph", "io.clouditor.graph")
    val session = sessionFactory.openSession()

    @BeforeEach
    fun init() {
        session.purgeDatabase()
    }

    @Test
    fun executePPG() {
        App.let {
            it.rootPath =
                Path(
                    "/Users/kunz/cloud-property-graph/ppg-testing-library/Non-Repudiation/NR1-credentials-non-repudiation/Go/zerolog"
                )
            it.paths = listOf(Path("."))
            it.labelsEnabled = true
            it.localMode = true
        }
        App.call()

        // define the expected outcome
        // 1) the tainted datum is detected (correctly, i.e. the right datum is tained)
        // 2) the LogOutput is detected (correctly)
        // 3) the taint flows via DFG (arbitrary # of nodes) to the LogOutput
        // 4) there is exactly one path (from 1) to 2))

        val expected_node = LogOutput()
        expected_node.let {
            it.name = "Info"
            it.location = PhysicalLocation(URI("server.go"), Region(35, 2, 35, 24))
        }

        println("Retrieving neo4j results...")
        // execute the query
        /* val result1 =
        session.query(
            "MATCH p=()-[:DFG*]->()-[:ARGUMENTS]-()-[:CALL]-(n:LogOutput) RETURN p",
            Utils.map()
        ) */

        val result =
            session.query(
                "MATCH p=(a)-[:DFG*]->(c)-[d:ARGUMENTS]-(e)-[f:CALL]-(g:LogOutput) WITH *, relationships(p) AS b RETURN p, a, b, c, d, e, f, g",
                emptyMap<String, String>()
            )

        // compare expected number of paths
        println("Found ${result.count()} results")
        // assertEquals(2, result.count())

        // compare expected nodes
        // assertEquals(expected_node, result.first().get("g"))
        var logoutput = result.first().get("g") as LogOutput
        assertEquals("Info", logoutput.name)
        assertContains(logoutput.labels, "LogOutput")
        // doesn't work because of special fields: assertEquals(PhysicalLocation(URI("server.go"),
        // Region(35, 2, 35, 24)), logoutput.location)
        println(result.first().get("b"))

        // compare expected nodes and edge types
        result.forEach {
            var path = it.get("p") as Array<*>
            println("result has ${path.size} self-contained paths")
            path.forEach {
                it as InternalPath.SelfContainedSegment
                // println(it.relationship().type())
                // println(it.end().labels())
                println(it.start().get("name"))
            }
            println(path)
            println(path.get(0))
            if (path.none {
                    it as InternalPath.SelfContainedSegment
                    it.start().get("name").toString() == "\"Msg\""
                }
            ) {
                fail("no node with the expected name")
            }
        }
    }

    // check if the data flow in question really flows from the tainted datum to the intended sink,
    // e.g. the tainted personal datum to  the log output
    fun resolveDFG(path: InternalPath.SelfContainedSegment, targetNode: Node): Boolean {
        // first should be the tainted datum
        if (path.start().labels().contains("Identifier") &&
                // last should be the exit point
                path.end().labels().contains("LogOutput")
        ) {
            return true
        }
        return false
    }

    // Deprecated: graphml is xml-based which is too cumbersome to analyze
    fun exportGraphML() {
        val result =
            session.query(
                "CALL apoc.export.graphml.all(null, {stream:true}) \n YIELD file, nodes, relationships, properties, data \n RETURN file, nodes, relationships, properties, data;",
                mapOf("stream" to "true")
            )
        var xmldata = result.first().get("data") as String

        /* val builderFactory = DocumentBuilderFactory.newInstance()
        val docBuilder = builderFactory.newDocumentBuilder()
        val doc = docBuilder.parse((result.first().get("data") as String).byteInputStream())
        assertThat(testXml, CompareMatcher.isIdenticalTo(controlXml))*/

        // To create a valid query, one can use local-name() or register a namespace, see
        // https://stackoverflow.com/questions/13702637/xpath-with-namespace-in-java
        val i =
            JAXPXPathEngine()
                .selectNodes(
                    "//*[name()=\"node\" and @*[name()=\"labels\"]=\":CallExpression:Expression:Node:Statement\"]",
                    Input.fromString(xmldata).build()
                )
        i.forEach {
            println(it.childNodes.length)
            it.childNodes.forEach { println(it.attributes.item(0).nodeValue) }
        }
        // store nodes in a map (node, labels)? how to check a certain path?
        // selectNodes(node@LogOutput); selectNodes(edge@source=node1)?
        // see https://extendsclass.com/xpath-tester.html
        // //*[name()="edge" and @*[name()="source"]=//*[name()="node" and
        // @*[name()="labels"]=":Functionality:LogOutput:Node"]/@id]
        // either write xpath queries for every test case, or leave out graphml stuff and just
        // describe in prose, or
        // write the asserts on Neo4J outputs

        /*val control: Source = Input.fromString("test-data/good.xml").build()
        // val test: Source = Input.fromByteArray(createTestDocument()).build())
        val test: Source = Input.fromByteArray(createTestDocument()).build())
        val diff: DifferenceEngine = DOMDifferenceEngine()
        diff.addDifferenceListener(object : ComparisonListener() {
            fun comparisonPerformed(comparison: Comparison, outcome: ComparisonResult?) {
                Assert.fail("found a difference: $comparison")
            }
        })
        diff.compare(control, test)*/
    }
}

fun NodeList.forEach(action: (Node) -> Unit) {
    (0 until this.length).asSequence().map { this.item(it) }.forEach { action(it) }
}
