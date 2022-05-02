package io.clouditor.graph

import kotlin.io.path.Path
import org.junit.Test
import org.neo4j.driver.internal.InternalPath

class PolicyNonComplianceTest {

    @Test
    fun TestNC1() {
        val result =
            executePPG(
                Path(
                    "/Users/kunz/cloud-property-graph/ppg-testing-library/Policy-Non-Compliance/NC3-disproportionate-processing/Python"
                ),
                listOf(Path(".")),
                ""
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
            assert(firstNode.labels().contains("PseudoIdentifier"))
            // the last node should be the LogOutput
            val lastNode = (path.last() as InternalPath.SelfContainedSegment).end()
            println(lastNode)
            // assert(lastNode.labels().contains("LogOutput"))
        }
    }

    // fun TestNC2(){} out of scope
    // fun TestNC3(){} out of scope
    // fun TestNC4(){} out of scope

    @Test
    fun TestNC5() {
        val result =
            executePPG(
                Path(
                    "/Users/kunz/cloud-property-graph/ppg-testing-library/Policy-Non-Compliance/NC3-disproportionate-processing/Python"
                ),
                listOf(Path(".")),
                "MATCH p=(:PseudoIdentifier)--()-[:DFG*]->(:DatabaseOperation)-->(s:DatabaseStorage) WHERE NOT EXISTS((:DatabaseOperation)<-[:DFG]-(s)) RETURN p, s"
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
            assert(firstNode.labels().contains("PseudoIdentifier"))
            // the last node should be the LogOutput
            val lastNode = (path.last() as InternalPath.SelfContainedSegment).end()
            println(lastNode)
            // assert(lastNode.labels().contains("LogOutput"))
        }
    }
}
