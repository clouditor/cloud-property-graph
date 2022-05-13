package io.clouditor.graph

import kotlin.io.path.Path
import kotlin.test.assertEquals
import org.junit.Test
import org.neo4j.driver.internal.InternalPath

class DetectabilityTest {

    // D1 dectectable credentials out of scope

    // D2 detectable communication
    @Test
    fun TestD2_Python() {
        val result =
            executePPG(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Detectability/D2-detectable-communication/Python"
                ),
                listOf(Path(".")),
                "MATCH p=(i:PseudoIdentifier)--()-[:DFG*]->(:HttpRequest) RETURN p"
            )
        // compare expected number of paths
        println("Found ${result.count()} results")
        // TODO
        assertEquals(0, result.count())
    }

    // D2 detectable communication
    @Test
    fun TestD2_Go() {
        val result =
            executePPG(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Detectability/D2-detectable-communication/Go"
                ),
                listOf(Path(".")),
                "MATCH p=(i:PseudoIdentifier)--()-[:DFG*]->(:HttpRequest) RETURN p"
            )
        // compare expected number of paths
        println("Found ${result.count()} results")
        assertEquals(1, result.count())

        // compare expected nodes
        result.first().apply {
            // get the path; the path contains multiple sub-paths, each one connecting two nodes via
            // an edge
            var path = this.get("p") as Array<*>
            // the first node should be the label
            val firstNode = (path.first() as InternalPath.SelfContainedSegment).start()
            assert(firstNode.labels().contains("PseudoIdentifier"))
            // the last node should be the LogOutput
            val lastNode = (path.last() as InternalPath.SelfContainedSegment).end()
            assert(lastNode.labels().contains("HttpRequest"))
        }
    }

    // D3 detectable outliers out of scope

    // D4 detectable at storage
    @Test
    fun TestD4() {
        val result =
            executePPG(
                Path(
                    "/Users/kunz/cloud-property-graph/ppg-testing-library/Detectability/D4-detectable-at-storage/Python"
                ),
                listOf(Path(".")),
                "MATCH p = (i:PseudoIdentifier)−−()−[:DFG*]−>()−[:EOG*]−>(h) WHERE h.name = \"HttpStatus.NOT_FOUND\" RETURN p, i, h"
            )
        // compare expected number of paths
        println("Found ${result.count()} results")
        assertEquals(1, result.count())
    }

    // D5 detectable at retrieval
    @Test
    fun TestD5() {
        val result =
            executePPG(
                Path(
                    "/Users/kunz/cloud-property-graph/ppg-testing-library/Detectability/D5-detectable-at-retrieval/Python"
                ),
                listOf(Path(".")),
                // TODO first check for storage of identifier in the DB
                "MATCH p = (i:PseudoIdentifier)−−()−[:DFG*]−>()−[:EOG*]−>(h) WHERE h.name = \"HttpStatus.CONFLICT\" RETURN p, i, h"
            )
        // compare expected number of paths
        println("Found ${result.count()} results")
        assertEquals(1, result.count())
    }
}
