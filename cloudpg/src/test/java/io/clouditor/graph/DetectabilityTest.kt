package io.clouditor.graph

import kotlin.io.path.Path
import kotlin.test.assertEquals
import org.junit.Test
import org.neo4j.driver.internal.InternalPath

class DetectabilityTest {

    // D1 Dectectable Credentials out of scope

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
        // we expect exactly one threat path
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
    fun TestD2_Python_validation() {
        val result =
            executePPG(
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
    fun TestD2_Go_validation() {
        val result =
            executePPG(
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

    @Test
    fun TestD4_Go() {
        val result =
            executePPG(
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
    fun TestD4_Go_validation() {
        val result =
            executePPG(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Detectability/D4-detectable-at-storage/Go-validation"
                ),
                listOf(Path(".")),
                "MATCH p=(:PseudoIdentifier)--()-[:DFG*]->(he:HttpEndpoint)-[:DFG*]->(ds:DatabaseStorage) WHERE (he)--(:FunctionDeclaration)-[:EOG*]->({name:\"HttpStatus.CONFLICT\"}) RETURN p"
            )
        assertEquals(0, result.count())
    }

    @Test
    fun TestD4_Python() {
        val result =
            executePPG(
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
    fun TestD4_Python_validation() {
        val result =
            executePPG(
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
    fun TestD5_Go() {
        val result =
            executePPG(
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
    fun TestD5_Go_validation() {
        val result =
            executePPG(
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
    fun TestD5_Python() {
        val result =
            executePPG(
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
    fun TestD5_Python_validation() {
        val result =
            executePPG(
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
