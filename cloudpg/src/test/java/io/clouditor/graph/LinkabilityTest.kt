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
                    "/Users/kunz/cloud-property-graph/ppg-testing-library/Linkability/L3-linkability-of-inbound-data/Go"
                ),
                listOf(Path(".")),
                ""
            )
        assertEquals(1, result.count())

        result.first().apply {
            var path = this.get("p") as Array<*>
            val firstNode = (path.first() as InternalPath.SelfContainedSegment).start()
            assert(firstNode.labels().contains("Identifier"))
            val lastNode = (path.last() as InternalPath.SelfContainedSegment).end()
            assert(lastNode.labels().contains("LogOutput"))
        }
    }

    @Test
    fun TestL3_Python() {
        val result =
            executePPG(
                Path(
                    "/Users/kunz/cloud-property-graph/ppg-testing-library/Linkability/L3-linkability-of-inbound-data/Python"
                ),
                listOf(Path(".")),
                ""
            )
        assertEquals(1, result.count())

        result.first().apply {
            var path = this.get("p") as Array<*>
            val firstNode = (path.first() as InternalPath.SelfContainedSegment).start()
            assert(firstNode.labels().contains("Identifier"))
            val lastNode = (path.last() as InternalPath.SelfContainedSegment).end()
            assert(lastNode.labels().contains(""))
        }
    }

    // ID4 out of scope

    // fun TestID5(){}
    // fun TestID6(){}
    // fun TestID7(){}

}
