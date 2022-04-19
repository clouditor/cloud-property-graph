package io.clouditor.graph

import kotlin.io.path.Path
import kotlin.test.assertEquals
import org.junit.Test
import org.neo4j.driver.internal.InternalPath

class NonRepudiationTest {

    @Test
    fun TestNR1() {
        val result =
            executePPG(
                Path(
                    "/Users/kunz/cloud-property-graph/ppg-testing-library/Non-Repudiation/NR1-credentials-non-repudiation/Go/zerolog"
                ),
                listOf(Path(".")),
                "MATCH p=(:Identifier)-[:LABELEDNODE]-(a)-[:DFG*]->(c)-[d:ARGUMENTS]-(e)-[f:CALL]-(g:LogOutput) WITH *, relationships(p) AS b RETURN p, a, b, c, d, e, f, g"
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
            assert(firstNode.labels().contains("Identifier"))
            // the last node should be the LogOutput
            val lastNode = (path.last() as InternalPath.SelfContainedSegment).end()
            assert(lastNode.labels().contains("LogOutput"))
        } // compare expected number of paths
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
            assert(firstNode.labels().contains("Identifier"))
            // the last node should be the LogOutput
            val lastNode = (path.last() as InternalPath.SelfContainedSegment).end()
            assert(lastNode.labels().contains("LogOutput"))
        }
    }

    // TODO: this requires some form of digital signature or something similar
    // fun TestNR2() {}

    // Out of scope
    // fun TestNR3() {}

    // TODO: should extend NR2 where a digitally signed message is also stored in a db
    // fun TestNR4() {}

    // TODO: should extend NR4 where the data is also retrieved
    // fun TestNR5() {}
}
