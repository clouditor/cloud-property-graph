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
        assertEquals(1, result.count())

        result.first().apply {
            var path = this.get("p") as Array<*>
            val firstNode = (path.first() as InternalPath.SelfContainedSegment).start()
            assert(firstNode.labels().contains("PseudoIdentifier"))
            val lastNode = (path.last() as InternalPath.SelfContainedSegment).end()
            assert(lastNode.labels().contains("HttpEndpoint"))
        }
    }

    // The PPG currently does not pass this test, since it cannot differentiate different fields
    // sent in the same HttpRequest
    @Test
    fun TestNC1_Python_fieldsensitive() {
        val result =
            executePPG(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Policy-Non-Compliance/NC1-disproportionate-collection/Python-fieldsensitive"
                ),
                listOf(Path(".")),
                "MATCH p=({name:'name'})--()-[:DFG*]->(h:HttpEndpoint) WHERE NOT EXISTS{ MATCH(h)-[:DFG*]->(i) WHERE (i:Expression) AND NOT (i:DeclaredReferenceExpression) AND (NOT (i:BinaryOperator) OR i.operatorCode <> \"=\") OR (i:IfStatement) OR (i:WhileStatment) OR (i)<-[:ARGUMENTS]-()} RETURN p"
            )
        // in this case, 2 paths are expected because there are two HttpEndpoints that the
        // Identifier crosses: A proxied endpoint and the actual one
        assertEquals(2, result.count())

        result.first().apply {
            var path = this.get("p") as Array<*>
            val firstNode = (path.first() as InternalPath.SelfContainedSegment).start()
            assert(firstNode.labels().contains("PseudoIdentifier"))
            val lastNode = (path.last() as InternalPath.SelfContainedSegment).end()
            assert(lastNode.labels().contains("HttpEndpoint"))
        }
    }

    @Test
    fun TestNC1_Python_validation() {
        val result =
            executePPG(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Policy-Non-Compliance/NC1-disproportionate-collection/Python-validation"
                ),
                listOf(Path(".")),
                "MATCH p=({name:'name'})--()-[:DFG*]->(h:HttpEndpoint) WHERE NOT EXISTS{ MATCH(h)-[:DFG*]->(i) WHERE (i:Expression) AND NOT (i:DeclaredReferenceExpression) AND (NOT (i:BinaryOperator) OR i.operatorCode <> \"=\") OR (i:IfStatement) OR (i:WhileStatment) OR (i)<-[:ARGUMENTS]-()} RETURN p"
            )
        assertEquals(0, result.count())
    }

    @Test
    fun TestNC1_Go() {
        val result =
            executePPG(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Policy-Non-Compliance/NC1-disproportionate-collection/Go"
                ),
                listOf(Path(".")),
                "MATCH p=(:PseudoIdentifier)--()-[:DFG*]->(h:HttpEndpoint) WHERE NOT EXISTS{ MATCH(h)-[:DFG*]->(i) WHERE (i:Expression) AND NOT (i:DeclaredReferenceExpression) AND (NOT (i:BinaryOperator) OR i.operatorCode <> \"=\") OR (i:IfStatement) OR (i:WhileStatment) OR (i)<-[:ARGUMENTS]-()} RETURN p"
            )
        // in this case, 2 paths are expected because there are two HttpEndpoints that the
        // Identifier crosses: A proxied endpoint and the actual one
        assertEquals(2, result.count())

        result.first().apply {
            var path = this.get("p") as Array<*>
            val firstNode = (path.first() as InternalPath.SelfContainedSegment).start()
            assert(firstNode.labels().contains("PseudoIdentifier"))
            val lastNode = (path.last() as InternalPath.SelfContainedSegment).end()
            assert(lastNode.labels().contains("HttpEndpoint"))
        }
    }

    // The PPG currently does not pass this test, since it cannot differentiate different fields
    // sent in the same HttpRequest
    @Test
    fun TestNC1_Go_fieldsensitive() {
        val result =
            executePPG(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Policy-Non-Compliance/NC1-disproportionate-collection/Go-fieldsensitive"
                ),
                listOf(Path(".")),
                "MATCH p=(:PseudoIdentifier)--()-[:DFG*]->(h:HttpEndpoint) WHERE NOT EXISTS{ MATCH(h)-[:DFG*]->(i) WHERE (i:Expression) AND NOT (i:DeclaredReferenceExpression) AND (NOT (i:BinaryOperator) OR i.operatorCode <> \"=\") OR (i:IfStatement) OR (i:WhileStatment) OR (i)<-[:ARGUMENTS]-()} RETURN p"
            )
        // in this case, 2 paths are expected because there are two HttpEndpoints that the
        // Identifier crosses: A proxied endpoint and the actual one
        assertEquals(2, result.count())

        result.first().apply {
            var path = this.get("p") as Array<*>
            val firstNode = (path.first() as InternalPath.SelfContainedSegment).start()
            assert(firstNode.labels().contains("PseudoIdentifier"))
            val lastNode = (path.last() as InternalPath.SelfContainedSegment).end()
            assert(lastNode.labels().contains("HttpEndpoint"))
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
