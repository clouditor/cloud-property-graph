package io.clouditor.graph

import kotlin.io.path.Path
import kotlin.test.assertEquals
import org.junit.Test
import org.neo4j.driver.internal.InternalPath

class NonRepudiationTest {

    @Test
    fun TestNR2_Python_DigitalSignature() {
        val result =
            executePPG(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Non-Repudiation/NR2-non-repudiation-of-sending/Python-DigitalSignature"
                ),
                listOf(Path(".")),
                "MATCH p=(:PseudoIdentifier)--(n)-[:DFG*]->(hr:HttpRequest)-[:DFG*]->(:HttpEndpoint) WHERE (:Signature)--(n)-[:DFG*]->(hr) AND (:Signature)-[:SIGNATURE]->()-[:DFG*]->(hr) RETURN p"
            )
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
    fun TestNR2_Python_DigitalSignature_Validation() {
        val result =
            executePPG(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Non-Repudiation/NR2-non-repudiation-of-sending/Python-DigitalSignature-validation"
                ),
                listOf(Path(".")),
                "MATCH p=(:PseudoIdentifier)--(n)-[:DFG*]->(hr:HttpRequest)-[:DFG*]->(:HttpEndpoint) WHERE (:Signature)--(n)-[:DFG*]->(hr) AND (:Signature)-[:SIGNATURE]->()-[:DFG*]->(hr) RETURN p"
            )
        assertEquals(0, result.count())
    }

    @Test
    fun TestNR2_Python_Logging() {
        val result =
            executePPG(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Non-Repudiation/NR2-non-repudiation-of-sending/Python-Logging"
                ),
                listOf(Path(".")),
                "MATCH p=(:Identifier)--()-[:DFG*]->()-[:ARGUMENTS]-()-[:CALL]-(:LogOutput) RETURN p"
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
    fun TestNR2_Python_Logging_Validation() {
        val result =
            executePPG(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Non-Repudiation/NR2-non-repudiation-of-sending/Python-Logging-validation"
                ),
                listOf(Path(".")),
                "MATCH p=(:Identifier)--()-[:DFG*]->()-[:ARGUMENTS]-()-[:CALL]-(:LogOutput) RETURN p"
            )
        assertEquals(0, result.count())
    }

    @Test
    fun TestNR2_Go_DigitalSignature() {
        val result =
            executePPG(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Non-Repudiation/NR2-non-repudiation-of-sending/Go-DigitalSignature"
                ),
                listOf(Path(".")),
                "MATCH p=(:PseudoIdentifier)--(n)-[:DFG*]->(hr:HttpRequest)-[:DFG*]->(:HttpEndpoint) WHERE (:Signature)--(n)-[:DFG*]->(hr) AND (:Signature)-[:SIGNATURE]->()-[:DFG*]->(hr) RETURN p"
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
    fun TestNR2_Go_DigitalSignature_Validation() {
        val result =
            executePPG(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Non-Repudiation/NR2-non-repudiation-of-sending/Go-DigitalSignature-validation"
                ),
                listOf(Path(".")),
                "MATCH p=(:PseudoIdentifier)--(n)-[:DFG*]->(hr:HttpRequest)-[:DFG*]->(:DatabaseStorage) WHERE (:Signature)--(n)-[:DFG*]->(hr) AND (:Signature)-[:SIGNATURE]->()-[:DFG*]->(hr) RETURN p"
            )
        assertEquals(0, result.count())
    }

    @Test
    fun TestNR2_Go_Logging() {
        val result =
            executePPG(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Non-Repudiation/NR2-non-repudiation-of-sending/Go-Logging"
                ),
                listOf(Path(".")),
                "MATCH p=(:Identifier)-[:LABELEDNODE]-()-[:DFG*]->()-[:ARGUMENTS]-()-[:CALL]-(g:LogOutput) RETURN p"
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
    fun TestNR2_Go_Logging_Validation() {
        val result =
            executePPG(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Non-Repudiation/NR2-non-repudiation-of-sending/Go-Logging-validation"
                ),
                listOf(Path(".")),
                "MATCH p=(:Identifier)-[:LABELEDNODE]-()-[:DFG*]->()-[:ARGUMENTS]-()-[:CALL]-(g:LogOutput) RETURN p"
            )
        assertEquals(0, result.count())
    }

    // NR3 Non-repudiation of receipt out of scope

    // Due to missing field-sensitivity in HTTP requests, there is an additional false-positive
    // threat detected here
    @Test
    fun TestNR4_Go() {
        val result =
            executePPG(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Non-Repudiation/NR4-non-reputable-storage/Go-DigitalSignature"
                ),
                listOf(Path(".")),
                "MATCH p=(:PseudoIdentifier)--(n)-[:DFG*]->(hr:HttpRequest)-[:DFG*]->(:DatabaseStorage) WHERE (:Signature)--(n)-[:DFG*]->(hr) AND (:Signature)-[:SIGNATURE]->()-[:DFG*]->(hr) RETURN p"
            )
        assertEquals(1, result.count())

        result.first().apply {
            var path = this.get("p") as Array<*>
            val firstNode = (path.first() as InternalPath.SelfContainedSegment).start()
            assert(firstNode.labels().contains("PseudoIdentifier"))
            val lastNode = (path.last() as InternalPath.SelfContainedSegment).end()
            assert(lastNode.labels().contains("DatabaseStorage"))
        }
    }

    @Test
    fun TestNR4_Go_Validation() {
        val result =
            executePPG(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Non-Repudiation/NR4-non-reputable-storage/Go-DigitalSignature-validation"
                ),
                listOf(Path(".")),
                "MATCH p=(:PseudoIdentifier)--(n)-[:DFG*]->(hr:HttpRequest)-[:DFG*]->(:DatabaseStorage) WHERE (:Signature)--(n)-[:DFG*]->(hr) AND (:Signature)-[:SIGNATURE]->()-[:DFG*]->(hr) RETURN p"
            )
        assertEquals(0, result.count())
    }

    // Due to missing field-sensitivity in HTTP requests, there is an additional false-positive
    // threat detected here
    @Test
    fun TestNR4_Python() {
        val result =
            executePPG(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Non-Repudiation/NR4-non-reputable-storage/Python-DigitalSignature"
                ),
                listOf(Path(".")),
                "MATCH p=(:PseudoIdentifier)--(n)-[:DFG*]->(hr:HttpRequest)-[:DFG*]->(:DatabaseStorage) WHERE (:Signature)--(n)-[:DFG*]->(hr) AND (:Signature)-[:SIGNATURE]->()-[:DFG*]->(hr) RETURN p"
            )
        assertEquals(1, result.count())

        result.first().apply {
            var path = this.get("p") as Array<*>
            val firstNode = (path.first() as InternalPath.SelfContainedSegment).start()
            assert(firstNode.labels().contains("PseudoIdentifier"))
            val lastNode = (path.last() as InternalPath.SelfContainedSegment).end()
            assert(lastNode.labels().contains("DatabaseStorage"))
        }
    }

    @Test
    fun TestNR4_Python_Validation() {
        val result =
            executePPG(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Non-Repudiation/NR4-non-reputable-storage/Python-DigitalSignature-validation"
                ),
                listOf(Path(".")),
                "MATCH p=(:PseudoIdentifier)--(n)-[:DFG*]->(hr:HttpRequest)-[:DFG*]-(:DatabaseStorage) WHERE (:Signature)--(n)-[:DFG*]->(hr) AND (:Signature)-[:SIGNATURE]->()-[:DFG*]->(hr) RETURN p"
            )
        assertEquals(0, result.count())
    }

    // Due to missing field-sensitivity in HTTP requests, there is an additional false-positive
    // threat detected here
    @Test
    fun TestNR5_Python() {
        val result =
            executePPG(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Non-Repudiation/NR5-non-repudiation-of-retrieved-data/Python-DigitalSignature"
                ),
                listOf(Path(".")),
                "MATCH p=(:PseudoIdentifier)--()-[:DFG*]->(h:HttpRequest)-[:DFG*]->(d:DatabaseStorage), (a:Application), (a2:Application), (h2:HttpRequest), (n) WHERE (n)--(:Signature) AND (h)--(a) AND (a2:Application)--(h2:HttpRequest)-[:DFG*]->()<--(:DatabaseStorage) RETURN p"
            )
        // in this case, 2 paths are expected because there are two HttpEndpoints that the
        // Identifier crosses: A proxied endpoint and the actual one
        assertEquals(2, result.count())

        result.first().apply {
            var path = this.get("p") as Array<*>
            val firstNode = (path.first() as InternalPath.SelfContainedSegment).start()
            assert(firstNode.labels().contains("PseudoIdentifier"))
            val lastNode = (path.last() as InternalPath.SelfContainedSegment).end()
            assert(lastNode.labels().contains("DatabaseStorage"))
        }
    }

    // Due to missing field-sensitivity in HTTP requests, there is an additional false-positive
    // threat detected here
    @Test
    fun TestNR5_Go() {
        val result =
            executePPG(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Non-Repudiation/NR5-non-repudiation-of-retrieved-data/Go-DigitalSignature"
                ),
                listOf(Path(".")),
                "MATCH p=(:PseudoIdentifier)--()-[:DFG*]->(h:HttpRequest)-[:DFG*]->(d:DatabaseStorage), (a:Application), (a2:Application), (h2:HttpRequest), (n) WHERE (n)--(:Signature) AND (h)--(a) AND (a2:Application)--(h2:HttpRequest)-[:DFG*]->()<--(:DatabaseStorage) RETURN p"
            )
        assertEquals(1, result.count())

        result.first().apply {
            var path = this.get("p") as Array<*>
            val firstNode = (path.first() as InternalPath.SelfContainedSegment).start()
            assert(firstNode.labels().contains("PseudoIdentifier"))
            val lastNode = (path.last() as InternalPath.SelfContainedSegment).end()
            assert(lastNode.labels().contains("DatabaseStorage"))
        }
    }

    @Test
    fun TestNR5_Go_Validation() {
        val result =
            executePPG(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Non-Repudiation/NR5-non-repudiation-of-retrieved-data/Go-validation"
                ),
                listOf(Path(".")),
                "MATCH p=(:PseudoIdentifier)--(n)-[:DFG*]->(h:HttpRequest)-[:DFG*]->(d:DatabaseStorage), (a:Application), (a2:Application), (h2:HttpRequest) WHERE (n)--(:Signature) AND (a)--(h) AND (a2:Application)--(h2:HttpRequest)-[:DFG*]->()<--(:DatabaseStorage) AND a<>a2 RETURN p"
            )
        assertEquals(0, result.count())
    }

    @Test
    fun TestNR5_Python_Validation() {
        val result =
            executePPG(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/Non-Repudiation/NR5-non-repudiation-of-retrieved-data/Python-validation"
                ),
                listOf(Path(".")),
                "MATCH p=(:PseudoIdentifier)--(n)-[:DFG*]->(h:HttpRequest)-[:DFG*]->(d:DatabaseStorage), (a:Application), (a2:Application), (h2:HttpRequest) WHERE (n)--(:Signature) AND (a)--(h) AND (a2:Application)--(h2:HttpRequest)-[:DFG*]->()<--(:DatabaseStorage) AND a<>a2 RETURN p"
            )
        assertEquals(0, result.count())
    }
}
