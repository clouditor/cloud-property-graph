package io.clouditor.graph

import kotlin.io.path.Path
import kotlin.test.assertEquals
import org.junit.Test
import org.neo4j.driver.internal.InternalPath

class UnawarenessTest {

    // fun TestU1(){} out of scope
    // fun TestU2(){} out of scope

    @Test
    fun TestU3_Go() {
        val result =
            executePPG(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Unawareness/U3-no-access-or-portability/Go"
                ),
                listOf(Path(".")),
                "MATCH p=(:PseudoIdentifier)--()-[:DFG*]->(o:DatabaseOperation)-->(d:DatabaseStorage), (a:Application), (p:DatabaseOperation) WHERE NOT EXISTS((p)<--(d:DatabaseStorage)) AND ((o)--(a)) AND ((p)--(a)) RETURN p,a,d"
            )
        // compare expected number of paths
        println("Found ${result.count()} results")
        assertEquals(1, result.count())

        // TODO compare expected nodes
        result.first().apply {
            // get the path; the path contains multiple sub-paths, each one connecting two nodes via
            // an edge
            var path = this.get("p") as Array<*>
            println("result has ${path.size} sub-paths")
            // the first node should be the label
            val firstNode = (path.first() as InternalPath.SelfContainedSegment).start()
            // the last node should be the LogOutput
            val lastNode = (path.last() as InternalPath.SelfContainedSegment).end()
        }
    }

    @Test
    fun TestU3_Go_Validation() {
        val result =
            executePPG(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Unawareness/U3-no-access-or-portability/Go-validation"
                ),
                listOf(Path(".")),
                "MATCH p=(:PseudoIdentifier)--()-[:DFG*]->(do1:DatabaseOperation)-->(ds:DatabaseStorage), (a:Application), (do2:DatabaseOperation) WHERE NOT EXISTS (()-[:CALLS]-(do2)<--(ds:DatabaseStorage)) AND ((do1)--(a)) AND ((do2)--(a)) RETURN p, a, ds"
            )
        // compare expected number of paths
        println("Found ${result.count()} results")
        assertEquals(0, result.count())
    }

    @Test
    fun TestU3_Python() {
        val result =
            executePPG(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Unawareness/U3-no-access-or-portability/Python"
                ),
                listOf(Path(".")),
                // TODO add (:PseudoIdentifier)--
                "MATCH p=()-[:DFG*]->(do1:DatabaseOperation)-->(ds:DatabaseStorage), (a:Application), (do2:DatabaseOperation) WHERE NOT EXISTS (()-[:CALLS]-(do2)<--(ds:DatabaseStorage)) AND ((do1)--(a)) AND ((do2)--(a)) RETURN p, a, ds"
            )
        // compare expected number of paths
        println("Found ${result.count()} results")
        assertEquals(1, result.count())

        // compare expected nodes
        result.first().apply {
            // get the path; the path contains multiple sub-paths, each one connecting two nodes via
            // an edge
            var path = this.get("p") as Array<*>
            println("result has ${path.size} sub-paths")
            // the first node should be the label
            val firstNode = (path.first() as InternalPath.SelfContainedSegment).start()
            // the last node should be the LogOutput
            val lastNode = (path.last() as InternalPath.SelfContainedSegment).end()
        }
    }

    @Test
    fun TestU3_Python_Validation() {
        val result =
            executePPG(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Unawareness/U3-no-access-or-portability/Python-validation"
                ),
                listOf(Path(".")),
                // TODO add (:PseudoIdentifier)--
                "MATCH p=()-[:DFG*]->(do1:DatabaseOperation)-->(ds:DatabaseStorage), (a:Application), (do2:DatabaseOperation) WHERE NOT EXISTS (()-[:CALLS]-(do2)<--(ds:DatabaseStorage)) AND ((do1)--(a)) AND ((do2)--(a)) RETURN p, a, ds"
            )
        // compare expected number of paths
        println("Found ${result.count()} results")
        assertEquals(0, result.count())
    }

    // TODO: we don't cover this yet because we cannot detect a delete operation in a query...
    @Test
    fun TestU4_Go_missing_DELETE() {
        val result =
            executePPG(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Unawareness/U4-no-erasure-or-rectification/Go-missing-DELETE"
                ),
                listOf(Path(".")),
                "MATCH p=(:PseudoIdentifier)--()-[:DFG*]->(o:DatabaseOperation)-->(d:DatabaseStorage), (a:Application), (p:DatabaseOperation) WHERE NOT EXISTS((p)<--(d:DatabaseStorage)) AND ((o)--(a)) AND ((p)--(a)) RETURN p,a,d"
            )
        // compare expected number of paths
        println("Found ${result.count()} results")
        assertEquals(0, result.count())
    }

    @Test
    fun TestU4_Python_Validation() {
        val result =
            executePPG(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Unawareness/U4-no-erasure-or-rectification/Python-validation"
                ),
                listOf(Path(".")),
                "MATCH p=(:PseudoIdentifier)--()-[:DFG*]->(do1:DatabaseOperation)-[:DFG]->(ds:DatabaseStorage), (a:Application) WHERE NOT EXISTS (()<-[:DFG]-(ds:DatabaseStorage)) RETURN p"
            )
        // compare expected number of paths
        println("Found ${result.count()} results")
        assertEquals(0, result.count())
    }

    // fun TestU5(){} out of scope

}
