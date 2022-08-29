package io.clouditor.graph

import kotlin.io.path.Path
import kotlin.test.assertEquals
import org.junit.Test
import org.junit.jupiter.api.Tag
import org.neo4j.driver.internal.InternalPath

@Tag("TestingLibrary")
open class LinkabilityTest {

    // L1 out of scope
    // L2 out of scope

    @Test
    fun testL3Go() {
        val result =
            executePPGAndQuery(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Linkability/L3-linkability-of-inbound-data/Go"
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
    fun testL3GoValidation() {
        val result =
            executePPGAndQuery(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Linkability/L3-linkability-of-inbound-data/Go-validation"
                ),
                listOf(Path(".")),
                "MATCH p=(:PseudoIdentifier)--()-[:DFG*]->(h:HttpEndpoint) RETURN p"
            )
        assertEquals(0, result.count())
    }

    @Test
    fun testL3Python() {
        val result =
            executePPGAndQuery(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Linkability/L3-linkability-of-inbound-data/Python"
                ),
                listOf(Path(".")),
                "MATCH p=(:PseudoIdentifier)--()-[:DFG*]->(h:HttpEndpoint) RETURN p"
            )
        assertEquals(2, result.count())

        result.first().apply {
            val path = this["p"] as Array<*>
            val firstNode = (path.first() as InternalPath.SelfContainedSegment).start()
            assert(firstNode.labels().contains("PseudoIdentifier"))
            val lastNode = (path.last() as InternalPath.SelfContainedSegment).end()
            assert(lastNode.labels().contains("HttpEndpoint"))
        }
    }

    @Test
    fun testL3PythonValidation() {
        val result =
            executePPGAndQuery(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Linkability/L3-linkability-of-inbound-data/Python-validation"
                ),
                listOf(Path(".")),
                "MATCH p=(:PseudoIdentifier)--()-[:DFG*]->(h:HttpEndpoint) RETURN p"
            )
        assertEquals(0, result.count())
    }

    // L4 Linkability of Context out of scope

    @Test
    fun testL5Python() {
        val result =
            executePPGAndQuery(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Linkability/L5-linkability-of-shared-data/Python"
                ),
                listOf(Path(".")),
                "MATCH p=(:PseudoIdentifier)--()-[:DFG*]->(h1:HttpRequest)-[:DFG*]->(h2:HttpRequest), (a1:Application), (a2:Application) WHERE (h1)--(a1) AND (h2)--(a2) RETURN p"
            )
        // assertEquals(1, result.count())

        result.first().apply {
            val path = this["p"] as Array<*>
            val firstNode = (path.first() as InternalPath.SelfContainedSegment).start()
            // assert(firstNode.labels().contains("PseudoIdentifier"))
            val lastNode = (path.last() as InternalPath.SelfContainedSegment).end()
            // assert(lastNode.labels().contains("HttpRequest"))
        }
    }

    @Test
    fun testL5PythonValidation() {
        val result =
            executePPGAndQuery(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Linkability/L5-linkability-of-shared-data/Python-validation"
                ),
                listOf(Path(".")),
                "MATCH p=(:PseudoIdentifier)--()-[:DFG*]->(h1:HttpRequest)-[:DFG*]->(h2:HttpRequest), (a1:Application), (a2:Application) WHERE (h1)--(a1) AND (h2)--(a2) RETURN p"
            )
        assertEquals(0, result.count())
    }

    @Test
    fun testL5Go() {
        val result =
            executePPGAndQuery(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Linkability/L5-linkability-of-shared-data/Go"
                ),
                listOf(Path(".")),
                "MATCH p=(:PseudoIdentifier)--()-[:DFG*]->(h1:HttpRequest)-[:DFG*]->(h2:HttpRequest), (a1:Application), (a2:Application) WHERE (h1)--(a1) AND (h2)--(a2) RETURN p"
            )
        // assertEquals(1, result.count())

        result.first().apply {
            val path = this.get("p") as Array<*>
            val firstNode = (path.first() as InternalPath.SelfContainedSegment).start()
            // assert(firstNode.labels().contains("PseudoIdentifier"))
            val lastNode = (path.last() as InternalPath.SelfContainedSegment).end()
            // assert(lastNode.labels().contains("HttpRequest"))
        }
    }

    @Test
    fun testL5GoValidation() {
        val result =
            executePPGAndQuery(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Linkability/L5-linkability-of-shared-data/Go-validation"
                ),
                listOf(Path(".")),
                "MATCH p=(:PseudoIdentifier)--()-[:DFG*]->(h1:HttpRequest)-[:DFG*]->(h2:HttpRequest), (a1:Application), (a2:Application) WHERE (h1)--(a1) AND (h2)--(a2) RETURN p"
            )
        assertEquals(0, result.count())
    }

    @Test
    fun testL6Python() {
        val result =
            executePPGAndQuery(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Linkability/L6-linkability-of-stored-data/Python"
                ),
                listOf(Path(".")),
                "MATCH p=(:PseudoIdentifier)--()-[:DFG*]->(h:HttpRequest)-[:DFG*]->(:DatabaseStorage) RETURN p"
            )
        // assertEquals(1, result.count())

        result.first().apply {
            val path = this["p"] as Array<*>
            val firstNode = (path.first() as InternalPath.SelfContainedSegment).start()
            // assert(firstNode.labels().contains("PseudoIdentifier"))
            val lastNode = (path.last() as InternalPath.SelfContainedSegment).end()
            // assert(lastNode.labels().contains("DatabaseStorage"))
        }
    }

    @Test
    fun testL6PythonValidation() {
        val result =
            executePPGAndQuery(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Linkability/L6-linkability-of-stored-data/Python-validation"
                ),
                listOf(Path(".")),
                "MATCH p=(:PseudoIdentifier)--()-[:DFG*]->(h:HttpRequest)-[:DFG*]->(:DatabaseStorage) RETURN p"
            )
        assertEquals(0, result.count())
    }

    @Test
    fun testL6Go() {
        val result =
            executePPGAndQuery(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Linkability/L6-linkability-of-stored-data/Go"
                ),
                listOf(Path(".")),
                "MATCH p=(:PseudoIdentifier)--()-[:DFG*]->(h:HttpRequest)-[:DFG*]->(:DatabaseStorage) RETURN p"
            )
        // assertEquals(1, result.count())

        result.first().apply {
            var path = this.get("p") as Array<*>
            val firstNode = (path.first() as InternalPath.SelfContainedSegment).start()
            // assert(firstNode.labels().contains("PseudoIdentifier"))
            val lastNode = (path.last() as InternalPath.SelfContainedSegment).end()
            // assert(lastNode.labels().contains("DatabaseStorage"))
        }
    }

    @Test
    fun testL6GoValidation() {
        val result =
            executePPGAndQuery(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Linkability/L6-linkability-of-stored-data/Go-validation"
                ),
                listOf(Path(".")),
                "MATCH p=(:PseudoIdentifier)--()-[:DFG*]->(h:HttpRequest)-[:DFG*]->(:DatabaseStorage) RETURN p"
            )
        assertEquals(0, result.count())
    }

    @Test
    fun testL7Python() {
        val result =
            executePPGAndQuery(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Linkability/L7-linkability-of-retrieved-data/Python"
                ),
                listOf(Path(".")),
                "MATCH p=(:PseudoIdentifier)--()-[:DFG*]->(h1:HttpRequest)-[:DFG*]->(ds:DatabaseStorage), (h2:HttpRequest), (a1:Application), (a2:Application) WHERE (h2)-[:DFG*]->()<--(ds) AND (h1)--(a1) AND (h2)--(a2) RETURN p"
            )
        // assertEquals(1, result.count())

        result.first().apply {
            var path = this.get("p") as Array<*>
            val firstNode = (path.first() as InternalPath.SelfContainedSegment).start()
            // assert(firstNode.labels().contains("PseudoIdentifier"))
            val lastNode = (path.last() as InternalPath.SelfContainedSegment).end()
            // assert(lastNode.labels().contains("DatabaseStorage"))
        }
    }

    @Test
    fun testL7PythonValidation() {
        val result =
            executePPGAndQuery(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Linkability/L7-linkability-of-retrieved-data/Python-validation"
                ),
                listOf(Path(".")),
                "MATCH p=(:PseudoIdentifier)--()-[:DFG*]->(h1:HttpRequest)-[:DFG*]->(ds:DatabaseStorage), (h2:HttpRequest), (a1:Application), (a2:Application) WHERE (h2)-[:DFG*]->()<--(ds) AND (h1)--(a1) AND (h2)--(a2) RETURN p"
            )
        assertEquals(0, result.count())
    }

    @Test
    fun testL7Go() {
        val result =
            executePPGAndQuery(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Linkability/L7-linkability-of-retrieved-data/Go"
                ),
                listOf(Path(".")),
                "MATCH p=(:PseudoIdentifier)--()-[:DFG*]->(h1:HttpRequest)-[:DFG*]->(ds:DatabaseStorage), (h2:HttpRequest), (a1:Application), (a2:Application) WHERE (h2)-[:DFG*]->()<--(ds) AND (h1)--(a1) AND (h2)--(a2) RETURN p"
            )
        // assertEquals(1, result.count())

        result.first().apply {
            var path = this.get("p") as Array<*>
            val firstNode = (path.first() as InternalPath.SelfContainedSegment).start()
            // assert(firstNode.labels().contains("PseudoIdentifier"))
            val lastNode = (path.last() as InternalPath.SelfContainedSegment).end()
            // assert(lastNode.labels().contains("DatabaseStorage"))
        }
    }

    @Test
    fun testL7GoValidation() {
        val result =
            executePPGAndQuery(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Linkability/L7-linkability-of-retrieved-data/Go-validation"
                ),
                listOf(Path(".")),
                "MATCH p=(:PseudoIdentifier)--()-[:DFG*]->(h1:HttpRequest)-[:DFG*]->(ds:DatabaseStorage), (h2:HttpRequest), (a1:Application), (a2:Application) WHERE (h2)-[:DFG*]->()<--(ds) AND (h1)--(a1) AND (h2)--(a2) RETURN p"
            )
        assertEquals(0, result.count())
    }
}
