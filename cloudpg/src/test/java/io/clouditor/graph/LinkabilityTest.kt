package io.clouditor.graph

import kotlin.io.path.Path
import kotlin.test.assertEquals
import org.junit.Test
import org.neo4j.driver.internal.InternalPath

class LinkabilityTest {

    // L1 out of scope
    // L2 out of scope

    @Test
    fun TestL3_Go() {
        val result =
            executePPG(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Identifiability/L3-linkability-of-inbound-data/Go"
                ),
                listOf(Path(".")),
                "MATCH p=(:PseudoIdentifier)--()-[:DFG*]->(h:HttpEndpoint) RETURN p"
            )
        assertEquals(2, result.count())

        result.first().apply {
            var path = this.get("p") as Array<*>
            val firstNode = (path.first() as InternalPath.SelfContainedSegment).start()
            assert(firstNode.labels().contains("PseudoIdentifier"))
            val lastNode = (path.last() as InternalPath.SelfContainedSegment).end()
            assert(lastNode.labels().contains("HttpEndpoint"))
        }
    }

    @Test
    fun TestL3_Python() {
        val result =
            executePPG(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Identifiability/L3-linkability-of-inbound-data/Python"
                ),
                listOf(Path(".")),
                "MATCH p=(:PseudoIdentifier)--()-[:DFG*]->(h:HttpEndpoint) RETURN p"
            )
        assertEquals(2, result.count())

        result.first().apply {
            var path = this.get("p") as Array<*>
            val firstNode = (path.first() as InternalPath.SelfContainedSegment).start()
            assert(firstNode.labels().contains("Identifier"))
            val lastNode = (path.last() as InternalPath.SelfContainedSegment).end()
            assert(lastNode.labels().contains("HttpEndpoint"))
        }
    }

    // L4 Linkability of Context out of scope

    @Test
    fun TestL5_Python() {
        val result =
            executePPG(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Linkability/L5-linkability-of-shared-data/Python"
                ),
                listOf(Path(".")),
                "MATCH p=(:PseudoIdentifier)--()-[:DFG*]->(h1:HttpRequest)-[:DFG*]->(h2:HttpRequest), (a1:Application), (a2:Application) WHERE (h1)--(a1) AND (h2)--(a2) RETURN p"
            )
        assertEquals(1, result.count())

        result.first().apply {
            var path = this.get("p") as Array<*>
            val firstNode = (path.first() as InternalPath.SelfContainedSegment).start()
            assert(firstNode.labels().contains("PseudoIdentifier"))
            val lastNode = (path.last() as InternalPath.SelfContainedSegment).end()
            assert(lastNode.labels().contains("HttpRequest"))
        }
    }

    @Test
    fun TestL5_Go() {
        val result =
            executePPG(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Linkability/L5-linkability-of-shared-data/Go"
                ),
                listOf(Path(".")),
                "MATCH p=(:PseudoIdentifier)--()-[:DFG*]->(h1:HttpRequest)-[:DFG*]->(h2:HttpRequest), (a1:Application), (a2:Application) WHERE (h1)--(a1) AND (h2)--(a2) RETURN p"
            )
        assertEquals(1, result.count())

        result.first().apply {
            var path = this.get("p") as Array<*>
            val firstNode = (path.first() as InternalPath.SelfContainedSegment).start()
            assert(firstNode.labels().contains("PseudoIdentifier"))
            val lastNode = (path.last() as InternalPath.SelfContainedSegment).end()
            assert(lastNode.labels().contains("HttpRequest"))
        }
    }

    @Test
    fun TestL6_Python() {
        val result =
            executePPG(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Linkability/L6-linkability-of-stored-data/Python"
                ),
                listOf(Path(".")),
                "MATCH p=(:PseudoIdentifier)--()-[:DFG*]->(h:HttpRequest)-[:DFG*]->(:DatabaseStorage) RETURN p"
            )
        assertEquals(1, result.count())

        result.first().apply {
            var path = this.get("p") as Array<*>
            val firstNode = (path.first() as InternalPath.SelfContainedSegment).start()
            assert(firstNode.labels().contains("PseudoIdentifier"))
            val lastNode = (path.last() as InternalPath.SelfContainedSegment).end()
            assert(lastNode.labels().contains("DatabaseStorage"))
        }
    }

    @Test
    fun TestL6_Go() {
        val result =
            executePPG(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Linkability/L6-linkability-of-stored-data/Go"
                ),
                listOf(Path(".")),
                "MATCH p=(:PseudoIdentifier)--()-[:DFG*]->(h:HttpRequest)-[:DFG*]->(:DatabaseStorage) RETURN p"
            )
        assertEquals(1, result.count())

        result.first().apply {
            var path = this.get("p") as Array<*>
            val firstNode = (path.first() as InternalPath.SelfContainedSegment).start()
            assert(firstNode.labels().contains("PseudoIdentifier"))
            val lastNode = (path.last() as InternalPath.SelfContainedSegment).end()
            assert(lastNode.labels().contains("DatabaseStorage"))
        }
    }

    @Test
    fun TestL7_Python() {
        val result =
            executePPG(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Linkability/L7-linkability-of-retrieved-data/Python"
                ),
                listOf(Path(".")),
                "MATCH p=(:PseudoIdentifier)--()-[:DFG*]->(h1:HttpRequest)-[:DFG*]->(ds:DatabaseStorage), (h2:HttpRequest), (a1:Application), (a2:Application) WHERE (h2)-[:DFG*]->()<--(ds) AND (h1)--(a1) AND (h2)--(a2) RETURN p"
            )
        assertEquals(1, result.count())

        result.first().apply {
            var path = this.get("p") as Array<*>
            val firstNode = (path.first() as InternalPath.SelfContainedSegment).start()
            assert(firstNode.labels().contains("PseudoIdentifier"))
            val lastNode = (path.last() as InternalPath.SelfContainedSegment).end()
            assert(lastNode.labels().contains("DatabaseStorage"))
        }
    }

    @Test
    fun TestL7_Go() {
        val result =
            executePPG(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Linkability/L7-linkability-of-retrieved-data/Go"
                ),
                listOf(Path(".")),
                "MATCH p=(:PseudoIdentifier)--()-[:DFG*]->(h1:HttpRequest)-[:DFG*]->(ds:DatabaseStorage), (h2:HttpRequest), (a1:Application), (a2:Application) WHERE (h2)-[:DFG*]->()<--(ds) AND (h1)--(a1) AND (h2)--(a2) RETURN p"
            )
        assertEquals(1, result.count())

        result.first().apply {
            var path = this.get("p") as Array<*>
            val firstNode = (path.first() as InternalPath.SelfContainedSegment).start()
            assert(firstNode.labels().contains("PseudoIdentifier"))
            val lastNode = (path.last() as InternalPath.SelfContainedSegment).end()
            assert(lastNode.labels().contains("DatabaseStorage"))
        }
    }
}
