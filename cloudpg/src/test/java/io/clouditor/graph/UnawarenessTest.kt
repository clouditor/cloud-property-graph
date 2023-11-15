package io.clouditor.graph

import kotlin.io.path.Path
import kotlin.test.assertEquals
import org.junit.Test
import org.junit.jupiter.api.Tag
import org.neo4j.driver.internal.InternalPath

@Tag("TestingLibrary")
open class UnawarenessTest {

    // U1 No Transparency out of scope
    // U2 No User-Friendly Privacy Control out of scope

    @Test
    fun testU3Go() {
        val result =
            executePPGAndQuery(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Unawareness/U3-no-access-or-portability/Go"
                ),
                listOf(Path("client.go"), Path("server.go")),
                "MATCH p=(:PseudoIdentifier)--()-[:DFG*]->(h1:HttpRequest)-[:DFG*]->(ds:DatabaseStorage), (a:Application), (h2:HttpRequest) WHERE NOT EXISTS ((:HttpRequest)-[:DFG*]->()-[:CALLS]-()<-[:DFG]-(ds:DatabaseStorage)) AND ((h1)--(a)) AND ((h2)--(a)) RETURN p"
            )
        assertEquals(1, result.count())

        result.first().apply {
            val path = this["p"] as Array<*>
            println("result has ${path.size} sub-paths")
            val firstNode = (path.first() as InternalPath.SelfContainedSegment).start()
            assert(firstNode.labels().contains("PseudoIdentifier"))
            val lastNode = (path.last() as InternalPath.SelfContainedSegment).end()
            assert(lastNode.labels().contains("DatabaseStorage"))
        }
    }

    @Test
    fun testU3GoValidation() {
        val result =
            executePPGAndQuery(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Unawareness/U3-no-access-or-portability/Go-validation"
                ),
                listOf(Path("client.go"), Path("server.go")),
                "MATCH p=(:PseudoIdentifier)--()-[:DFG*]->(h1:HttpRequest)-[:DFG*]->(ds:DatabaseStorage), (a:Application), (h2:HttpRequest) WHERE NOT EXISTS ((:HttpRequest)-[:DFG*]->()-[:CALLS]-()<-[:DFG]-(ds:DatabaseStorage)) AND ((h1)--(a)) AND ((h2)--(a)) RETURN p"
            )
        assertEquals(0, result.count())
    }

    @Test
    fun testU3Python() {
        val result =
            executePPGAndQuery(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Unawareness/U3-no-access-or-portability/Python"
                ),
                listOf(Path("client.py"), Path("server.py")),
                "MATCH p=(:PseudoIdentifier)--()-[:DFG*]->(h1:HttpRequest)-[:DFG*]->(ds:DatabaseStorage), (a:Application), (h2:HttpRequest) WHERE NOT EXISTS ((:HttpRequest)-[:DFG*]->()-[:CALLS]-()<-[:DFG]-(ds:DatabaseStorage)) AND ((h1)--(a)) AND ((h2)--(a)) RETURN p"
            )
        assertEquals(1, result.count())

        result.first().apply {
            var path = this["p"] as Array<*>
            val firstNode = (path.first() as InternalPath.SelfContainedSegment).start()
            assert(firstNode.labels().contains("PseudoIdentifier"))
            val lastNode = (path.last() as InternalPath.SelfContainedSegment).end()
            assert(lastNode.labels().contains("DatabaseStorage"))
        }
    }

    @Test
    fun testU3PythonValidation() {
        val result =
            executePPGAndQuery(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Unawareness/U3-no-access-or-portability/Python-validation"
                ),
                listOf(Path("client.py"), Path("server.py")),
                "MATCH p=(:PseudoIdentifier)--()-[:DFG*]->(h1:HttpRequest)-[:DFG*]-(do1:DatabaseOperation)-[:DFG]->(ds:DatabaseStorage), (a:Application), (h2:HttpRequest) WHERE NOT EXISTS ((:HttpRequest)-[:DFG*]->()-[:CALLS]-()<-[:DFG]-(ds:DatabaseStorage)) AND ((h1)--(a)) AND ((h2)--(a)) RETURN p"
            )
        assertEquals(0, result.count())
    }

    // Due to missing differentiation between CRUD operations, this test results in a false negative
    @Test
    fun testU4GoMissingDELETE() {
        val result =
            executePPGAndQuery(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Unawareness/U4-no-erasure-or-rectification/Go-missing-DELETE"
                ),
                listOf(Path("client.go"), Path("server.go")),
                "MATCH p=(:PseudoIdentifier)--()-[:DFG*]->(hr1:HttpRequest)-[:DFG*]-(do1:DatabaseOperation)-[:DFG]->(ds:DatabaseStorage), (a:Application), (hr2:HttpRequest) WHERE NOT EXISTS ((hr2)-[:DFG*]->()<-[:DFG]-(ds)) AND ((hr1)--(a)--(hr2)) RETURN p"
            )
        assertEquals(1, result.count())

        result.first().apply {
            var path = this.get("p") as Array<*>
            println("result has ${path.size} sub-paths")
            val firstNode = (path.first() as InternalPath.SelfContainedSegment).start()
            // assert(firstNode.labels().contains("PseudoIdentifier"))
            val lastNode = (path.last() as InternalPath.SelfContainedSegment).end()
            // assert(lastNode.labels().contains("DatabaseStorage"))
        }
    }

    // Due to missing differentiation between CRUD operations, this test results in a false negative
    @Test
    fun testU4GoMissingPUT() {
        val result =
            executePPGAndQuery(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Unawareness/U4-no-erasure-or-rectification/Go-missing-PUT"
                ),
                listOf(Path("client.go"), Path("server.go")),
                "MATCH p=(:PseudoIdentifier)--()-[:DFG*]->(hr1:HttpRequest)-[:DFG*]-(do1:DatabaseOperation)-[:DFG]->(ds:DatabaseStorage), (a:Application), (hr2:HttpRequest) WHERE NOT EXISTS ((hr2)-[:DFG*]->()<-[:DFG]-(ds)) AND ((hr1)--(a)--(hr2)) RETURN p"
            )
        assertEquals(1, result.count())

        result.first().apply {
            var path = this.get("p") as Array<*>
            println("result has ${path.size} sub-paths")
            val firstNode = (path.first() as InternalPath.SelfContainedSegment).start()
            assert(firstNode.labels().contains("PseudoIdentifier"))
            val lastNode = (path.last() as InternalPath.SelfContainedSegment).end()
            assert(lastNode.labels().contains("DatabaseStorage"))
        }
    }

    // Not applicable: @Test fun testU4_Go_Validation() {}

    // Due to missing differentiation between CRUD operations, this test results in a false negative
    @Test
    fun testU4PythonMissingDELETE() {
        val result =
            executePPGAndQuery(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Unawareness/U4-no-erasure-or-rectification/Go-missing-DELETE"
                ),
                listOf(Path("client.py"), Path("server.py")),
                "MATCH p=(:PseudoIdentifier)--()-[:DFG*]->(hr1:HttpRequest)-[:DFG*]-(do1:DatabaseOperation)-[:DFG]->(ds:DatabaseStorage), (a:Application), (hr2:HttpRequest) WHERE NOT EXISTS ((hr2)-[:DFG*]->()<-[:DFG]-(ds)) AND ((hr1)--(a)--(hr2)) RETURN p"
            )
        assertEquals(1, result.count())

        result.first().apply {
            val path = this["p"] as Array<*>
            println("result has ${path.size} sub-paths")
            val firstNode = (path.first() as InternalPath.SelfContainedSegment).start()
            assert(firstNode.labels().contains("PseudoIdentifier"))
            val lastNode = (path.last() as InternalPath.SelfContainedSegment).end()
            assert(lastNode.labels().contains("DatabaseStorage"))
        }
    }

    // Due to missing differentiation between CRUD operations, this test results in a false negative
    @Test
    fun testU4PythonMissingPUT() {
        val result =
            executePPGAndQuery(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Unawareness/U4-no-erasure-or-rectification/Go-missing-PUT"
                ),
                listOf(Path("client.py"), Path("server.py")),
                "MATCH p=(:PseudoIdentifier)--()-[:DFG*]->(hr1:HttpRequest)-[:DFG*]-(do1:DatabaseOperation)-[:DFG]->(ds:DatabaseStorage), (a:Application), (hr2:HttpRequest) WHERE NOT EXISTS ((hr2)-[:DFG*]->()<-[:DFG]-(ds)) AND ((hr1)--(a)--(hr2)) RETURN p"
            )
        assertEquals(1, result.count())

        result.first().apply {
            val path = this["p"] as Array<*>
            println("result has ${path.size} sub-paths")
            val firstNode = (path.first() as InternalPath.SelfContainedSegment).start()
            assert(firstNode.labels().contains("PseudoIdentifier"))
            val lastNode = (path.last() as InternalPath.SelfContainedSegment).end()
            assert(lastNode.labels().contains("DatabaseStorage"))
        }
    }

    // Not applicable: @Test fun testU4_Python_Validation()

    // U5 Insufficient Consent Support out of scope

}
