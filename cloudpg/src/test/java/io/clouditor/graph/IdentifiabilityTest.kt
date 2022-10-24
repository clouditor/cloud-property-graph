package io.clouditor.graph

import kotlin.io.path.Path
import kotlin.test.assertEquals
import org.junit.Test
import org.junit.jupiter.api.Tag
import org.neo4j.driver.internal.InternalPath

@Tag("TestingLibrary")
open class IdentifiabilityTest {

    // fun testI1(){}
    // fun testI2(){}

    @Test
    fun testI3_Go() {
        val result =
            executePPGAndQuery(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Identifiability/I3-identifying-inbound-data/Go"
                ),
                listOf(Path(".")),
                "MATCH p=(:Identifier)--()-[:DFG*]->(h:HttpEndpoint) RETURN p"
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

    @Test
    fun testI3_Go_Validation() {
        val result =
            executePPGAndQuery(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Identifiability/I3-identifying-inbound-data/Go-validation"
                ),
                listOf(Path(".")),
                "MATCH p=(:Identifier)--()-[:DFG*]->(h:HttpEndpoint) RETURN p"
            )
        assertEquals(0, result.count())
    }

    @Test
    fun testI3_Python() {
        val result =
            executePPGAndQuery(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Identifiability/I3-identifying-inbound-data/Python"
                ),
                listOf(Path(".")),
                "MATCH p=(:Identifier)--()-[:DFG*]->(h:HttpEndpoint) RETURN p"
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

    @Test
    fun testI3_Python_Validation() {
        val result =
            executePPGAndQuery(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Identifiability/I3-identifying-inbound-data/Python-validation"
                ),
                listOf(Path(".")),
                "MATCH p=(:Identifier)--()-[:DFG*]->(h:HttpEndpoint) RETURN p"
            )
        assertEquals(0, result.count())
    }

    // I4 Identifying Context out of scope

    @Test
    fun testI5_Go() {
        val result =
            executePPGAndQuery(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Identifiability/I5-identifying-shared-data/Go"
                ),
                listOf(Path(".")),
                "MATCH p=(:Identifier)--()-[:DFG*]->(h1:HttpRequest)-[:DFG*]->(h2:HttpRequest), (a1:Application), (a2:Application) WHERE (h1)--(a1) AND (h2)--(a2) RETURN p"
            )
        assertEquals(1, result.count())

        result.first().apply {
            var path = this.get("p") as Array<*>
            val firstNode = (path.first() as InternalPath.SelfContainedSegment).start()
            assert(firstNode.labels().contains("Identifier"))
            val lastNode = (path.last() as InternalPath.SelfContainedSegment).end()
            assert(lastNode.labels().contains("HttpRequest"))
        }
    }

    @Test
    fun testI5_Go_Validation() {
        val result =
            executePPGAndQuery(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Identifiability/I5-identifying-shared-data/Go-validation"
                ),
                listOf(Path(".")),
                "MATCH p=(:Identifier)--()-[:DFG*]->(h1:HttpRequest)-[:DFG*]->(h2:HttpRequest), (a1:Application), (a2:Application) WHERE (h1)--(a1) AND (h2)--(a2) RETURN p"
            )
        assertEquals(0, result.count())
    }

    @Test
    fun testI5_Python() {
        val result =
            executePPGAndQuery(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Identifiability/I5-identifying-shared-data/Python"
                ),
                listOf(Path(".")),
                "MATCH p=(:Identifier)--()-[:DFG*]->(h1:HttpRequest)-[:DFG*]->(h2:HttpRequest), (a1:Application), (a2:Application) WHERE (h1)--(a1) AND (h2)--(a2) RETURN p"
            )
        assertEquals(1, result.count())

        result.first().apply {
            var path = this.get("p") as Array<*>
            val firstNode = (path.first() as InternalPath.SelfContainedSegment).start()
            assert(firstNode.labels().contains("Identifier"))
            val lastNode = (path.last() as InternalPath.SelfContainedSegment).end()
            assert(lastNode.labels().contains("HttpRequest"))
        }
    }

    @Test
    fun testI5_Python_Validation() {
        val result =
            executePPGAndQuery(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Identifiability/I5-identifying-shared-data/Python-validation"
                ),
                listOf(Path(".")),
                "MATCH p=(:Identifier)--()-[:DFG*]->(h1:HttpRequest)-[:DFG*]->(h2:HttpRequest), (a1:Application), (a2:Application) WHERE (h1)--(a1) AND (h2)--(a2) RETURN p"
            )
        assertEquals(0, result.count())
    }

    @Test
    fun testI6_Python() {
        val result =
            executePPGAndQuery(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Identifiability/I6-identifying-stored-data/Python"
                ),
                listOf(Path(".")),
                "MATCH p=(:Identifier)--()-[:DFG*]->(h:HttpRequest)-[:DFG*]->(:DatabaseStorage) RETURN p"
            )
        // assertEquals(1, result.count())

        result.first().apply {
            var path = this.get("p") as Array<*>
            val firstNode = (path.first() as InternalPath.SelfContainedSegment).start()
            // assert(firstNode.labels().contains("Identifier"))
            val lastNode = (path.last() as InternalPath.SelfContainedSegment).end()
            // assert(lastNode.labels().contains("DatabaseStorage"))
        }
    }

    @Test
    fun testI6_Python_Validation() {
        val result =
            executePPGAndQuery(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Identifiability/I6-identifying-stored-data/Python-validation"
                ),
                listOf(Path(".")),
                "MATCH p=(:Identifier)--()-[:DFG*]->(h:HttpRequest)-[:DFG*]->(:DatabaseStorage) RETURN p"
            )
        assertEquals(0, result.count())
    }

    @Test
    fun testI6_Go() {
        val result =
            executePPGAndQuery(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Identifiability/I6-identifying-stored-data/Go"
                ),
                listOf(Path(".")),
                "MATCH p=(:Identifier)--()-[:DFG*]->(h:HttpRequest)-[:DFG*]->(:DatabaseStorage) RETURN p"
            )
        // assertEquals(1, result.count())

        result.first().apply {
            var path = this.get("p") as Array<*>
            val firstNode = (path.first() as InternalPath.SelfContainedSegment).start()
            // assert(firstNode.labels().contains("Identifier"))
            val lastNode = (path.last() as InternalPath.SelfContainedSegment).end()
            // assert(lastNode.labels().contains("DatabaseStorage"))
        }
    }

    @Test
    fun testI6_Go_Validation() {
        val result =
            executePPGAndQuery(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Identifiability/I6-identifying-stored-data/Go-validation"
                ),
                listOf(Path(".")),
                "MATCH p=(:Identifier)--()-[:DFG*]->(h:HttpRequest)-[:DFG*]->(:DatabaseStorage) RETURN p"
            )
        assertEquals(0, result.count())
    }

    @Test
    fun testI7_Python() {
        val result =
            executePPGAndQuery(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Identifiability/I7-identifying-retrieved-data/Python"
                ),
                listOf(Path(".")),
                "MATCH p=(:Identifier)--()-[:DFG*]->(h1:HttpRequest)-[:DFG*]->(ds:DatabaseStorage), (h2:HttpRequest), (a1:Application), (a2:Application) WHERE (h2)-[:DFG*]->()<--(ds) AND (h1)--(a1) AND (h2)--(a2) RETURN p"
            )
        // assertEquals(1, result.count())

        result.first().apply {
            var path = this.get("p") as Array<*>
            val firstNode = (path.first() as InternalPath.SelfContainedSegment).start()
            // assert(firstNode.labels().contains("Identifier"))
            val lastNode = (path.last() as InternalPath.SelfContainedSegment).end()
            // assert(lastNode.labels().contains("DatabaseStorage"))
        }
    }

    @Test
    fun testI7_Python_Validation() {
        val result =
            executePPGAndQuery(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Identifiability/I7-identifying-retrieved-data/Python-validation"
                ),
                listOf(Path(".")),
                "MATCH p=(:Identifier)--()-[:DFG*]->(h1:HttpRequest)-[:DFG*]->(ds:DatabaseStorage), (h2:HttpRequest), (a1:Application), (a2:Application) WHERE (h2)-[:DFG*]->()<--(ds) AND (h1)--(a1) AND (h2)--(a2) RETURN p"
            )
        assertEquals(0, result.count())
    }

    @Test
    fun testI7_Go() {
        val result =
            executePPGAndQuery(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Identifiability/I7-identifying-retrieved-data/Go"
                ),
                listOf(Path(".")),
                "MATCH p=(:Identifier)--()-[:DFG*]->(h1:HttpRequest)-[:DFG*]->(ds:DatabaseStorage), (h2:HttpRequest), (a1:Application), (a2:Application) WHERE (h2)-[:DFG*]->()<--(ds) AND (h1)--(a1) AND (h2)--(a2) RETURN p"
            )
        // assertEquals(1, result.count())

        result.first().apply {
            var path = this.get("p") as Array<*>
            val firstNode = (path.first() as InternalPath.SelfContainedSegment).start()
            // assert(firstNode.labels().contains("Identifier"))
            val lastNode = (path.last() as InternalPath.SelfContainedSegment).end()
            // assert(lastNode.labels().contains("DatabaseStorage"))
        }
    }

    @Test
    fun testI7_Go_Validation() {
        val result =
            executePPGAndQuery(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Identifiability/I7-identifying-retrieved-data/Go-validation"
                ),
                listOf(Path(".")),
                "MATCH p=(:Identifier)--()-[:DFG*]->(h1:HttpRequest)-[:DFG*]->(ds:DatabaseStorage), (h2:HttpRequest), (a1:Application), (a2:Application) WHERE (h2)-[:DFG*]->()<--(ds) AND (h1)--(a1) AND (h2)--(a2) RETURN p"
            )
        assertEquals(0, result.count())
    }
}
