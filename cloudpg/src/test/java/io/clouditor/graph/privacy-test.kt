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
import org.neo4j.ogm.session.Utils

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
        // session.purgeDatabase()
    }

    @Test
    fun executePPG() {
        App.let {
            it.rootPath = Path("/Users/kunz/cloud-property-graph")
            it.paths =
                listOf(
                    Path(
                        "ppg-testing-library/Non-Repudiation/NR1-credentials-non-repudiation/Go/zerolog"
                    )
                )
            it.labelsEnabled = true
            it.localMode = true
        }
        // App.call()

        // define the expected outcome
        // 1) the tainted datum is detected (correctly, i.e. the right datum is tained)
        // 2) the LogOutput is detected (correctly)
        // 3) the taint flows via DFG (arbitrary # of nodes) to the LogOutput
        // 4) there is exactly one path

        // expected number of paths val path_num = 2
        val expected = "\"Msg\""
        // val expectedray = arrayOf("\"Msg\"", "\"Msg\"")
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

        // more targeted query that returns all entities directly
        val result =
            session.query(
                "MATCH p=(a)-[:DFG*]->(c)-[d:ARGUMENTS]-(e)-[f:CALL]-(g:LogOutput) WITH *, relationships(p) AS b RETURN p, a, b, c, d, e, f, g",
                emptyMap<String, String>()
            )

        // compare expected number of paths
        println("Found ${result.count()} results")
        assertEquals(2, result.count())

        // compare expected nodes
        // assertEquals(expected_node, result.first().get("g"))
        var logoutput = result.first().get("g") as LogOutput
        assertEquals("Info", logoutput.name)
        assertContains(logoutput.labels, "LogOutput")
        // doesn't work because of special fields: assertEquals(PhysicalLocation(URI("server.go"), Region(35, 2, 35, 24)), logoutput.location)

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
                    it.start().get("name").toString() == expected
                }
            ) {
                fail("no node with the expected name")
            }
        }
    }
}
