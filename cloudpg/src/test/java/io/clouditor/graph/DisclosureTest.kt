package io.clouditor.graph

import kotlin.io.path.Path
import kotlin.test.assertEquals
import org.junit.Test
import org.junit.jupiter.api.Tag
import org.neo4j.driver.internal.InternalPath

// Disclosure is not a category in LINDDUN GO
@Tag("TestingLibrary")
class DisclosureTest {

    @Test
    fun testDisclosureGo() {
        val result =
            executePPG(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Disclosure/unencrypted-transmission/Go"
                ),
                listOf(Path(".")),
                "MATCH p=(:PseudoIdentifier)--()-[:DFG*]->(h:ProxiedEndpoint) WHERE NOT EXISTS ((h)--(:TransportEncryption)) RETURN p"
            )
        // compare expected number of paths
        println("Found ${result.count()} results")
        assertEquals(1, result.count())

        // compare expected nodes
        result.first().apply {
            var path = this.get("p") as Array<*>
            println("result has ${path.size} sub-paths")
            // the first node should be the label
            val firstNode = (path.first() as InternalPath.SelfContainedSegment).start()
            assert(firstNode.labels().contains("PseudoIdentifier"))
            // the last node should be the Endpoint
            val lastNode = (path.last() as InternalPath.SelfContainedSegment).end()
            assert(lastNode.labels().contains("HttpEndpoint"))
        }
    }

    @Test
    fun testDisclosureGoValidation() {
        val result =
            executePPG(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Disclosure/unencrypted-transmission/Go-validation"
                ),
                listOf(Path(".")),
                "MATCH p=(:PseudoIdentifier)--()-[:DFG*]->(h:ProxiedEndpoint) WHERE NOT EXISTS ((h)--(:TransportEncryption)) RETURN p"
            )
        // compare expected number of paths
        println("Found ${result.count()} results")
        assertEquals(0, result.count())
    }

    @Test
    fun testDisclosurePython() {
        val result =
            executePPG(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Disclosure/unencrypted-transmission/Python"
                ),
                listOf(Path(".")),
                "MATCH p=(:PseudoIdentifier)--()-[:DFG*]->(h:ProxiedEndpoint) WHERE NOT EXISTS ((h)--(:TransportEncryption)) RETURN p"
            )
        assertEquals(1, result.count())

        result.first().apply {
            val path = this["p"] as Array<*>
            println("result has ${path.size} sub-paths")
            // the first node should be the label
            val firstNode = (path.first() as InternalPath.SelfContainedSegment).start()
            assert(firstNode.labels().contains("PseudoIdentifier"))
            // the last node should be the Endpoint
            val lastNode = (path.last() as InternalPath.SelfContainedSegment).end()
            assert(lastNode.labels().contains("HttpEndpoint"))
        }
    }

    @Test
    fun testDisclosurePythonValidation() {
        val result =
            executePPG(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Disclosure/unencrypted-transmission/Python-validation"
                ),
                listOf(Path(".")),
                "MATCH p=(:PseudoIdentifier)--()-[:DFG*]->(h:ProxiedEndpoint) WHERE NOT EXISTS ((h)--(:TransportEncryption)) RETURN p"
            )
        // no paths expected
        assertEquals(0, result.count())
    }
}
