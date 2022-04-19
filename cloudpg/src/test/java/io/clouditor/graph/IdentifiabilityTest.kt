package io.clouditor.graph

import kotlin.io.path.Path
import org.junit.Test
import org.neo4j.driver.internal.InternalPath

class IdentifiabilityTest {

    // fun TestID1(){}
    // fun TestID2(){}

    @Test
    fun TestID3() {
        println(System.getenv("JAVA_HOME"))
        val result =
            executePPG(
                Path(
                    "/Users/kunz/cloud-property-graph/ppg-testing-library/Identifiability/I3-identifying-inbound-data/Python"
                ),
                listOf(Path(".")),
                "MATCH p=(:Identifier)-[:LABELEDNODE]-(a)-[:DFG*]->(b) WITH *, relationships(p) AS b RETURN p, a, b"
            )
        // compare expected number of paths
        println("Found ${result.count()} results")
        // assertEquals(1, result.count())

        // compare expected nodes
        result.first().apply {
            // get the path; the path contains multiple sub-paths, each one connecting two nodes via
            // an edge
            var path = this.get("p") as Array<*>
            println("result has ${path.size} sub-paths")
            // the first node should be the label
            val firstNode = (path.first() as InternalPath.SelfContainedSegment).start()
            println(firstNode)
            // assert(firstNode.labels().contains("Identifier"))
            // the last node should be the LogOutput
            val lastNode = (path.last() as InternalPath.SelfContainedSegment).end()
            println(lastNode)
            // assert(lastNode.labels().contains("LogOutput"))
        }
    }

    // fun TestID4(){}
    // fun TestID5(){}
    // fun TestID6(){}
    // fun TestID7(){}

}
