package io.clouditor.graph

import kotlin.io.path.Path
import kotlin.test.assertEquals
import org.junit.Test
import org.neo4j.driver.internal.InternalPath

class UnawarenessTest {

    // fun TestU1(){} out of scope
    // fun TestU2(){} out of scope

    // TODO
    @Test
    fun TestU3() {
        val result =
            executePPG(
                Path(
                    "/Users/kunz/cloud-property-graph/ppg-testing-library/Unawareness/U3-no-access-or-portability"
                ),
                listOf(Path(".")),
                "MATCH (:PseudoIdentifier)--()-[:DFG*]->(o:DatabaseOperation)-->(d:DatabaseStorage), (a:Application), (p:DatabaseOperation)\n" +
                    "WHERE NOT EXISTS (()-[:CALLS]-(p)<--(d:DatabaseStorage))\n" +
                    "AND ((o)--(a)) AND ((p)--(a)) RETURN a, d"
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

    @Test fun TestU4() {}
    // TODO fun TestU4.2 with user role check --> mock auth server?

    // fun TestU5(){} out of scope

}
