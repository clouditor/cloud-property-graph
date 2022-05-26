package io.clouditor.graph

import kotlin.io.path.Path
import kotlin.test.assertEquals
import org.junit.Test
import org.neo4j.driver.internal.InternalPath

class PolicyNonComplianceTest {

    @Test
    fun TestNC1_Python() {
        val result =
            executePPG(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Policy-Non-Compliance/NC1-disproportionate-collection/Python"
                ),
                listOf(Path(".")),
                "MATCH p=(:PseudoIdentifier)--()-[:DFG*]->(h:HttpEndpoint) WHERE NOT EXISTS{ MATCH(h)-[:DFG*]->(i) WHERE (i:Expression) AND NOT (i:DeclaredReferenceExpression) AND (NOT (i:BinaryOperator) OR i.operatorCode <> \"=\") OR (i:IfStatement) OR (i:WhileStatment) OR (i)<-[:ARGUMENTS]-()} RETURN p"
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

    @Test
    fun TestNC1_Go1() {
        val result =
            executePPG(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Policy-Non-Compliance/NC1-disproportionate-collection/Go1"
                ),
                listOf(Path(".")),
                "MATCH p=(:PseudoIdentifier)--()-[:DFG*]->(h:HttpEndpoint) WHERE NOT EXISTS{ MATCH(h)-[:DFG*]->(i) WHERE (i:Expression) AND NOT (i:DeclaredReferenceExpression) AND (NOT (i:BinaryOperator) OR i.operatorCode <> \"=\") OR (i:IfStatement) OR (i:WhileStatment) OR (i)<-[:ARGUMENTS]-()} RETURN p"
            )
        assertEquals(1, result.count())

        result.first().apply {
            var path = this.get("p") as Array<*>
            val firstNode = (path.first() as InternalPath.SelfContainedSegment).start()
            println(firstNode)
            assert(firstNode.labels().contains("PseudoIdentifier"))
            val lastNode = (path.last() as InternalPath.SelfContainedSegment).end()
            println(lastNode)
            // assert(lastNode.labels().contains("LogOutput"))
        }
    }

    @Test
    fun TestNC1_Go2() {
        val result =
            executePPG(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Policy-Non-Compliance/NC1-disproportionate-collection/Go2"
                ),
                listOf(Path(".")),
                "MATCH p=(:PseudoIdentifier)--()-[:DFG*]->(h:HttpEndpoint) WHERE NOT EXISTS{ MATCH(h)-[:DFG*]->(i) WHERE (i:Expression) AND NOT (i:DeclaredReferenceExpression) AND (NOT (i:BinaryOperator) OR i.operatorCode <> \"=\") OR (i:IfStatement) OR (i:WhileStatment) OR (i)<-[:ARGUMENTS]-()} RETURN p"
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
            println(firstNode)
            assert(firstNode.labels().contains("PseudoIdentifier"))
            // the last node should be the LogOutput
            val lastNode = (path.last() as InternalPath.SelfContainedSegment).end()
            println(lastNode)
            // assert(lastNode.labels().contains("LogOutput"))
        }
    }

    @Test
    fun TestNC1_Go_Validation() {
        val result =
            executePPG(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Policy-Non-Compliance/NC1-disproportionate-collection/Go-validation"
                ),
                listOf(Path(".")),
                // TODO check if not exists h-*-expression
                "MATCH p=(:PseudoIdentifier)--()-[:DFG*]->(h:HttpEndpoint) WHERE NOT EXISTS{ MATCH(h)-[:DFG*]->(i) WHERE (i:Expression) AND NOT (i:DeclaredReferenceExpression) AND (NOT (i:BinaryOperator) OR i.operatorCode <> \"=\") OR (i:IfStatement) OR (i:WhileStatment) OR (i)<-[:ARGUMENTS]-()} RETURN p"
            )
        assertEquals(0, result.count())
    }

    // NC2() out of scope
    // NC3() out of scope
    // NC4() out of scope

    @Test
    fun TestNC5_Go1() {
        val result =
            executePPG(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Policy-Non-Compliance/NC5-disproportionate-storage/Go1"
                ),
                listOf(Path(".")),
                "MATCH p=(:PseudoIdentifier)--()-[:DFG*]->(:DatabaseOperation)-[:DFG]->(s:DatabaseStorage) WHERE NOT EXISTS((:DatabaseOperation)<-[:DFG]-(s)) RETURN p, s"
                // TODO test query: MATCH
                // p=()-[:DFG*]->(do1:DatabaseOperation)-[:DFG]->(ds:DatabaseStorage),
                // (a:Application) WHERE NOT EXISTS (()<-[:DFG]-(ds:DatabaseStorage)) RETURN p
                )
        assertEquals(1, result.count())

        result.first().apply {
            var path = this.get("p") as Array<*>
            // the first node should be the label
            val firstNode = (path.first() as InternalPath.SelfContainedSegment).start()
            assert(firstNode.labels().contains("PseudoIdentifier"))
            // the last node should be the LogOutput
            val lastNode = (path.last() as InternalPath.SelfContainedSegment).end()
            // assert(lastNode.labels().contains("LogOutput"))
        }
    }

    @Test
    fun TestNC5_Go2() {
        val result =
            executePPG(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Policy-Non-Compliance/NC5-disproportionate-storage/Go2"
                ),
                listOf(Path(".")),
                "MATCH p=(:PseudoIdentifier)--()-[:DFG*]->(:DatabaseOperation)-[:DFG]->(s:DatabaseStorage) WHERE NOT EXISTS((:DatabaseOperation)<-[:DFG]-(s)) RETURN p, s"
                // TODO test query: MATCH
                // p=()-[:DFG*]->(do1:DatabaseOperation)-[:DFG]->(ds:DatabaseStorage),
                // (a:Application) WHERE NOT EXISTS (()<-[:DFG]-(ds:DatabaseStorage)) RETURN p
                )
        assertEquals(1, result.count())

        result.first().apply {
            var path = this.get("p") as Array<*>
            // the first node should be the label
            val firstNode = (path.first() as InternalPath.SelfContainedSegment).start()
            assert(firstNode.labels().contains("PseudoIdentifier"))
            // the last node should be the LogOutput
            val lastNode = (path.last() as InternalPath.SelfContainedSegment).end()
            // assert(lastNode.labels().contains("LogOutput"))
        }
    }

    @Test
    fun TestNC5_Python1() {
        val result =
            executePPG(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Policy-Non-Compliance/NC5-disproportionate-storage/Python1"
                ),
                listOf(Path(".")),
                "MATCH p=(:PseudoIdentifier)--()-[:DFG*]->(:DatabaseOperation)-[:DFG]->(s:DatabaseStorage) WHERE NOT EXISTS((:DatabaseOperation)<-[:DFG]-(s)) RETURN p, s"
                // TODO test query: MATCH
                // p=()-[:DFG*]->(do1:DatabaseOperation)-[:DFG]->(ds:DatabaseStorage),
                // (a:Application) WHERE NOT EXISTS (()<-[:DFG]-(ds:DatabaseStorage)) RETURN p
                )
        assertEquals(1, result.count())

        result.first().apply {
            var path = this.get("p") as Array<*>
            // the first node should be the label
            val firstNode = (path.first() as InternalPath.SelfContainedSegment).start()
            assert(firstNode.labels().contains("PseudoIdentifier"))
            // the last node should be the LogOutput
            val lastNode = (path.last() as InternalPath.SelfContainedSegment).end()
            // assert(lastNode.labels().contains("LogOutput"))
        }
    }

    @Test
    fun TestNC5_Python2() {
        val result =
            executePPG(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Policy-Non-Compliance/NC5-disproportionate-storage/Python2"
                ),
                listOf(Path(".")),
                "MATCH p=(:PseudoIdentifier)--()-[:DFG*]->(:DatabaseOperation)-[:DFG]->(s:DatabaseStorage) WHERE NOT EXISTS((:DatabaseOperation)<-[:DFG]-(s)) RETURN p, s"
                // TODO test query: MATCH
                // p=()-[:DFG*]->(do1:DatabaseOperation)-[:DFG]->(ds:DatabaseStorage),
                // (a:Application) WHERE NOT EXISTS (()<-[:DFG]-(ds:DatabaseStorage)) RETURN p
                )
        assertEquals(1, result.count())

        result.first().apply {
            var path = this.get("p") as Array<*>
            // the first node should be the label
            val firstNode = (path.first() as InternalPath.SelfContainedSegment).start()
            assert(firstNode.labels().contains("PseudoIdentifier"))
            // the last node should be the LogOutput
            val lastNode = (path.last() as InternalPath.SelfContainedSegment).end()
            // assert(lastNode.labels().contains("LogOutput"))
        }
    }

    // TODO implement code
    @Test
    fun TestNC5_Python_Validation() {
        val result =
            executePPG(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Policy-Non-Compliance/NC5-disproportionate-storage/Python-validation"
                ),
                listOf(Path(".")),
                "MATCH p=(:PseudoIdentifier)--()-[:DFG*]->(:DatabaseOperation)-->(s:DatabaseStorage) WHERE NOT EXISTS((:DatabaseOperation)<-[:DFG]-(s)) RETURN p, s"
            )
        assertEquals(0, result.count())
    }
}
