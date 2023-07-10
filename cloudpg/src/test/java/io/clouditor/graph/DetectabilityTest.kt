package io.clouditor.graph

import kotlin.io.path.Path
import kotlin.test.assertEquals
import org.junit.Test
import org.junit.jupiter.api.Tag
import org.neo4j.driver.internal.InternalPath

@Tag("TestingLibrary")
open class DetectabilityTest {

    // D1 Dectectable Credentials out of scope

    @Test
    fun testD2Python() {
        val result =
            executePPGAndQuery(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Detectability/D2-detectable-communication/Python"
                ),
                listOf(Path(".")),
                "MATCH p=(i:PseudoIdentifier)--()-[:DFG*]->(:HttpRequest) RETURN p"
            )
        assertEquals(1, result.count())

        result.first().apply {
            var path = this.get("p") as Array<*>
            // the first node should be the label
            val firstNode = (path.first() as InternalPath.SelfContainedSegment).start()
            assert(firstNode.labels().contains("PseudoIdentifier"))
            // the last node should be the LogOutput
            val lastNode = (path.last() as InternalPath.SelfContainedSegment).end()
            assert(lastNode.labels().contains("HttpRequest"))
        }
    }

    @Test
    fun testD2PythonValidation() {
        val result =
            executePPGAndQuery(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Detectability/D2-detectable-communication/Python-validation"
                ),
                listOf(Path(".")),
                "MATCH p=(i:PseudoIdentifier)--()-[:DFG*]->(:HttpRequest) RETURN p"
            )
        // we expect exactly one threat path
        assertEquals(0, result.count())
    }

    @Test
    fun testD2Go() {
        val result =
            executePPGAndQuery(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Detectability/D2-detectable-communication/Go"
                ),
                listOf(Path(".")),
                "MATCH p=(i:PseudoIdentifier)--()-[:DFG*]->(:HttpRequest) RETURN p"
            )
        // we expect exactly one threat path
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

    @Test
    fun testD2GoValidation() {
        val result =
            executePPGAndQuery(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Detectability/D2-detectable-communication/Go-validation"
                ),
                listOf(Path(".")),
                "MATCH p=(i:PseudoIdentifier)--()-[:DFG*]->(:HttpRequest) RETURN p"
            )
        // we expect exactly one threat path
        assertEquals(0, result.count())
    }

    // D3 Detectable Outliers out of scope

    // FIXME: The new CPG fails to parse VariableDeclarations for ":=" statements
    @Test
    fun testD4Go() {
        val result =
            executePPGAndQuery(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Detectability/D4-detectable-at-storage/Go"
                ),
                listOf(Path(".")),
                "MATCH p=(:PseudoIdentifier)--()-[:DFG*]->(he:HttpEndpoint)-[:DFG*]->(ds:DatabaseStorage) WHERE (he)--(:FunctionDeclaration)-[:EOG*]->({name:\"HttpStatus.CONFLICT\"}) RETURN p"
            )
        assertEquals(2, result.count())
    }

    @Test
    fun testD4Go_validation() {
        val result =
            executePPGAndQuery(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Detectability/D4-detectable-at-storage/Go-validation"
                ),
                listOf(Path(".")),
                "MATCH p=(:PseudoIdentifier)--()-[:DFG*]->(he:HttpEndpoint)-[:DFG*]->(ds:DatabaseStorage) WHERE (he)--(:FunctionDeclaration)-[:EOG*]->({name:\"HttpStatus.CONFLICT\"}) RETURN p"
            )
        assertEquals(0, result.count())
    }

    // FIXME: The new CPG gives no values for prev/next EOG/DFG for CallExpressions, but the CloudPG
    //      depends on it to resolve the target of the Client creation!
    // FIXME: DeclaredArgumentExpression has its "refersTo" value null for named arguments
    //      (e.g. "requests.post(url, json = personal_data)"), was VariableDeclaration before
    @Test
    fun testD4Python() {
        val result =
            executePPGAndQuery(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Detectability/D4-detectable-at-storage/Python"
                ),
                listOf(Path(".")),
                "MATCH p=(:PseudoIdentifier)--()-[:DFG*]->(he:HttpEndpoint)-[:DFG*]->(ds:DatabaseStorage) WHERE (he)--(:FunctionDeclaration)-[:EOG*]->({name:\"HttpStatus.CONFLICT\"}) RETURN p"
            )
        assertEquals(2, result.count())
    }

    @Test
    fun testD4PythonValidation() {
        val result =
            executePPGAndQuery(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Detectability/D4-detectable-at-storage/Python-validation"
                ),
                listOf(Path(".")),
                "MATCH p=(:PseudoIdentifier)--()-[:DFG*]->(he:HttpEndpoint)-[:DFG*]->(ds:DatabaseStorage) WHERE (he)--(:FunctionDeclaration)-[:EOG*]->({name:\"HttpStatus.CONFLICT\"}) RETURN p"
            )
        assertEquals(0, result.count())
    }

    @Test
    fun testD5Go() {
        val result =
            executePPGAndQuery(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Detectability/D5-detectable-at-retrieval/Go"
                ),
                listOf(Path(".")),
                "MATCH p=(:PseudoIdentifier)--()-[:DFG*]->(:HttpEndpoint)-[:DFG*]->(ds:DatabaseStorage) WHERE (:HttpRequest)-[:DFG*]->()<-[:DFG]-(ds) AND (:HttpEndpoint)--(:FunctionDeclaration)-[:EOG*]->({name:\"HttpStatus.NOT_FOUND\"}) RETURN p"
            )
        assertEquals(2, result.count())
    }

    @Test
    fun testD5GoValidation() {
        val result =
            executePPGAndQuery(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Detectability/D5-detectable-at-retrieval/Go-validation"
                ),
                listOf(Path(".")),
                "MATCH p=(:PseudoIdentifier)--()-[:DFG*]->(:HttpEndpoint)-[:DFG*]->(ds:DatabaseStorage) WHERE (:HttpRequest)-[:DFG*]->()<-[:DFG]-(ds) AND (:HttpEndpoint)--(:FunctionDeclaration)-[:EOG*]->({name:\"HttpStatus.NOT_FOUND\"}) RETURN p"
            )
        assertEquals(0, result.count())
    }

    @Test
    fun testD5Python() {
        val result =
            executePPGAndQuery(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Detectability/D5-detectable-at-retrieval/Python"
                ),
                listOf(Path(".")),
                "MATCH p=(:PseudoIdentifier)--()-[:DFG*]->(:HttpEndpoint)-[:DFG*]->(ds:DatabaseStorage) WHERE (:HttpRequest)-[:DFG*]->()<-[:DFG]-(ds) AND (:HttpEndpoint)--(:FunctionDeclaration)-[:EOG*]->({name:\"HttpStatus.NOT_FOUND\"}) RETURN p"
            )
        assertEquals(2, result.count())
    }

    @Test
    fun testD5PythonValidation() {
        val result =
            executePPGAndQuery(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Detectability/D5-detectable-at-retrieval/Python-validation"
                ),
                listOf(Path(".")),
                "MATCH p=(:PseudoIdentifier)--()-[:DFG*]->(:HttpEndpoint)-[:DFG*]->(ds:DatabaseStorage) WHERE (:HttpRequest)-[:DFG*]->()<-[:DFG]-(ds) AND (:HttpEndpoint)--(:FunctionDeclaration)-[:EOG*]->({name:\"HttpStatus.NOT_FOUND\"}) RETURN p"
            )
        assertEquals(0, result.count())
    }
}
