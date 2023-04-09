package io.clouditor.graph

import io.clouditor.graph.utils.DatabaseQueryType
import kotlin.io.path.Path
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import org.junit.Test
import org.junit.jupiter.api.Tag
import org.neo4j.driver.internal.InternalPath
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@Tag("TestingLibrary")
open class GDPRComplianceChecks {

    @Test
    fun checkComplianceToArticle16() {
        val result =
            executePPGAndQuery(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/GDPRComplianceChecks/RightToRectification/Python"
                ),
                listOf(Path(".")),
                "MATCH path1=(ps1:PseudoIdentifier)--()-[:DFG*]->(hr1:HttpRequest {name: 'POST'})-[:TO]->(he1:HttpEndpoint)--()-[:DFG*]->(d1:DatabaseQuery {type: 'CREATE'}) WHERE NOT EXISTS { MATCH path2=(ps1)--()-[:DFG*]->(hr3:HttpRequest {name: 'PUT'})-[:TO]->(he3:HttpEndpoint {method: 'PUT'})--()-[:DFG*]->(d2:DatabaseQuery) WHERE (d2.type='UPDATE') AND (d1)-[:STORAGE]->(:DatabaseStorage)<-[:STORAGE]-(d2) } RETURN path1"
            )

        // create a list for all pseudoidentifiers with no update call connected to them via a data flow
        val listOfAllPseudoIdentifierWithNoUpdateByIdentity = mutableListOf<Long>()
        // iterate over all paths and add to the list
        result.forEach {
            var path = it.get("path1") as Array<*>

            // the first node is the pseudoidentifier because of the query
            val firstNode = (path.first() as InternalPath.SelfContainedSegment).start()
            if (firstNode.labels().contains("PseudoIdentifier")) {
                // add the pseudoidentifier to the list if it is not already in it
                if (!listOfAllPseudoIdentifierWithNoUpdateByIdentity.contains(firstNode.id()))
                    listOfAllPseudoIdentifierWithNoUpdateByIdentity.add(firstNode.id())
            }
        }
        // if the code is compliant to article 16, the list should be empty
        assertEquals(0, listOfAllPseudoIdentifierWithNoUpdateByIdentity.size)
    }

    @Test
    fun checkComplianceToArticle16_validation() {
        val result =
            executePPGAndQuery(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/GDPRComplianceChecks/RightToRectification/Python_validation"
                ),
                listOf(Path(".")),
                "MATCH path1=(ps1:PseudoIdentifier)--()-[:DFG*]->(hr1:HttpRequest {name: 'POST'})-[:TO]->(he1:HttpEndpoint)--()-[:DFG*]->(d1:DatabaseQuery {type: 'CREATE'}) WHERE NOT EXISTS { MATCH path2=(ps1)--()-[:DFG*]->(hr3:HttpRequest {name: 'PUT'})-[:TO]->(he3:HttpEndpoint {method: 'PUT'})--()-[:DFG*]->(d2:DatabaseQuery) WHERE (d2.type='UPDATE') AND (d1)-[:STORAGE]->(:DatabaseStorage)<-[:STORAGE]-(d2) } RETURN path1"
            )

        // create a list for all pseudoidentifiers with no update call connected to them via a data
        // flow
        val listOfAllPseudoIdentifierWithNoUpdateByIdentity = mutableListOf<Long>()
        // iterate over all paths and add to the list
        result.forEach {
            var path = it.get("path1") as Array<*>

            // the first node is the pseudoidentifier because of the query
            val firstNode = (path.first() as InternalPath.SelfContainedSegment).start()
            if (firstNode.labels().contains("PseudoIdentifier")) {
                // add the pseudoidentifier to the list if it is not already in it
                if (!listOfAllPseudoIdentifierWithNoUpdateByIdentity.contains(firstNode.id()))
                    listOfAllPseudoIdentifierWithNoUpdateByIdentity.add(firstNode.id())
            }
        }
        // if the code is compliant to article 16, the list should be empty
        assertNotEquals(0, listOfAllPseudoIdentifierWithNoUpdateByIdentity.size)
    }

    @Test
    fun checkComplianceToArticle17_paragraph_1() {
        val result =
            executePPGAndQuery(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/GDPRComplianceChecks/RightToErasure/Python"
                ),
                listOf(Path(".")),
                "MATCH path1=(ps1:PseudoIdentifier)--()-[:DFG*]->(hr1:HttpRequest {name: 'POST'})-[:TO]->(he1:HttpEndpoint)--()-[:DFG*]->(d1:DatabaseQuery {type: 'CREATE'}) WHERE NOT EXISTS { MATCH path2=(ps1)--()-[:DFG*]->(hr3:HttpRequest {name: 'DELETE'})-[:TO]->(he3:HttpEndpoint {method: 'DELETE'})--()-[:DFG*]->(d2:DatabaseQuery) WHERE (d2.type='DELETE') AND (d1)-[:STORAGE]->(:DatabaseStorage)<-[:STORAGE]-(d2) } RETURN path1"
            )
        // create a list for all pseudoidentifiers with no delete call connected to them via a data flow
        val listOfAllPseudoIdentifierWithNoDeleteByIdentity = mutableListOf<Long>()
        // iterate over all paths and add to the list
        result.forEach {
            var path = it.get("path1") as Array<*>

            // the first node is the pseudoidentifier because of the query
            val firstNode = (path.first() as InternalPath.SelfContainedSegment).start()
            if (firstNode.labels().contains("PseudoIdentifier")) {
                // add the pseudoidentifier to the list if it is not already in it
                if (!listOfAllPseudoIdentifierWithNoDeleteByIdentity.contains(firstNode.id()))
                    listOfAllPseudoIdentifierWithNoDeleteByIdentity.add(firstNode.id())
            }
        }
        // if the code is compliant to article 17(1), the list should be empty
        assertEquals(0, listOfAllPseudoIdentifierWithNoDeleteByIdentity.size)
    }

    @Test
    fun checkComplianceToArticle17_paragraph_1_validation() {
        val result =
            executePPGAndQuery(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/GDPRComplianceChecks/RightToErasure/Python_validation"
                ),
                listOf(Path(".")),
                "MATCH path1=(ps1:PseudoIdentifier)--()-[:DFG*]->(hr1:HttpRequest {name: 'POST'})-[:TO]->(he1:HttpEndpoint)--()-[:DFG*]->(d1:DatabaseQuery {type: 'CREATE'}) WHERE NOT EXISTS { MATCH path2=(ps1)--()-[:DFG*]->(hr3:HttpRequest {name: 'DELETE'})-[:TO]->(he3:HttpEndpoint {method: 'DELETE'})--()-[:DFG*]->(d2:DatabaseQuery) WHERE (d2.type='DELETE') AND (d1)-[:STORAGE]->(:DatabaseStorage)<-[:STORAGE]-(d2) } RETURN path1"
            )
        // create a list for all pseudoidentifiers with no delete call connected to them via a data flow
        val listOfAllPseudoIdentifierWithNoDeleteByIdentity = mutableListOf<Long>()
        // iterate over all paths and add to the list
        result.forEach {
            var path = it.get("path1") as Array<*>

            // the first node is the pseudoidentifier because of the query
            val firstNode = (path.first() as InternalPath.SelfContainedSegment).start()
            if (firstNode.labels().contains("PseudoIdentifier")) {
                // add the pseudoidentifier to the list if it is not already in it
                if (!listOfAllPseudoIdentifierWithNoDeleteByIdentity.contains(firstNode.id()))
                    listOfAllPseudoIdentifierWithNoDeleteByIdentity.add(firstNode.id())
            }
        }
        // if the code is compliant to article 17(1), the list should be empty
        assertNotEquals(0, listOfAllPseudoIdentifierWithNoDeleteByIdentity.size)
    }

    @Test
    fun checkComplianceToArticle17_paragraph_2() {
        val result =
            executePPGAndQuery(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/GDPRComplianceChecks/RightToErasure/Python"
                ),
                listOf(Path(".")),
                "MATCH (hr1:HttpRequest), path1=(ps1:PseudoIdentifier)--()-[:DFG*]->(hr1) WHERE NOT (hr1)-[:TO]-(:HttpEndpoint) AND NOT EXISTS { MATCH path2=(ps1)--()-[:DFG*]->(hr2:HttpRequest {name: 'DELETE'})-[:TO]-(he2:HttpEndpoint {method: 'DELETE'})--()-[:DFG*]->(hr3:HttpRequest) WHERE (hr3.name='DELETE') AND (hr3.url = hr1.url) AND NOT (hr3)-[:TO]-(:HttpEndpoint) } RETURN path1"
            )
        // create a list for all pseudoidentifiers, which are communicated to extern with no delete call to extern connected to them via a data flow
        val listOfAllPseudoIdentifierWithNoDeleteToExternByIdentity = mutableListOf<Long>()
        // iterate over all paths and add to the list
        result.forEach {
            var path = it.get("path1") as Array<*>

            // the first node is the pseudoidentifier because of the query
            val firstNode = (path.first() as InternalPath.SelfContainedSegment).start()
            if (firstNode.labels().contains("PseudoIdentifier")) {
                // add the pseudoidentifier to the list if it is not already in it
                if (!listOfAllPseudoIdentifierWithNoDeleteToExternByIdentity.contains(
                        firstNode.id()
                    )
                )
                    listOfAllPseudoIdentifierWithNoDeleteToExternByIdentity.add(firstNode.id())
            }
        }
        // if the code is compliant to article 17(2), the list should be empty
        assertEquals(0, listOfAllPseudoIdentifierWithNoDeleteToExternByIdentity.size)
    }

    @Test
    fun checkComplianceToArticle17_paragraph_2_validation() {
        val result =
            executePPGAndQuery(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/GDPRComplianceChecks/RightToErasure/Python_validation"
                ),
                listOf(Path(".")),
                "MATCH (hr1:HttpRequest), path1=(ps1:PseudoIdentifier)--()-[:DFG*]->(hr1) WHERE NOT (hr1)-[:TO]-(:HttpEndpoint) AND NOT EXISTS { MATCH path2=(ps1)--()-[:DFG*]->(hr2:HttpRequest {name: 'DELETE'})-[:TO]-(he2:HttpEndpoint {method: 'DELETE'})--()-[:DFG*]->(hr3:HttpRequest) WHERE (hr3.name='DELETE') AND (hr3.url = hr1.url) AND NOT (hr3)-[:TO]-(:HttpEndpoint) } RETURN path1"
            )
        // create a list for all pseudoidentifiers, which are communicated to extern with no delete call to extern connected to them via a data flow
        val listOfAllPseudoIdentifierWithNoDeleteToExternByIdentity = mutableListOf<Long>()
        // iterate over all paths and add to the list
        result.forEach {
            var path = it.get("path1") as Array<*>

            // the first node is the pseudoidentifier because of the query
            val firstNode = (path.first() as InternalPath.SelfContainedSegment).start()
            if (firstNode.labels().contains("PseudoIdentifier")) {
                // add the pseudoidentifier to the list if it is not already in it
                if (!listOfAllPseudoIdentifierWithNoDeleteToExternByIdentity.contains(
                        firstNode.id()
                    )
                )
                    listOfAllPseudoIdentifierWithNoDeleteToExternByIdentity.add(firstNode.id())
            }
        }
        // if the code is compliant to article 17(2), the list should be empty
        assertNotEquals(0, listOfAllPseudoIdentifierWithNoDeleteToExternByIdentity.size)
    }

    @Test
    fun checkComplianceToArticle19() {
        val result_data_flows =
            executePPGAndQuery(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/GDPRComplianceChecks/NotificationObligation/Python"
                ),
                listOf(Path(".")),
                "MATCH path1=(ps1:PseudoIdentifier)--()-[:DFG*]->(hr1:HttpRequest)-[:TO]->(he1:HttpEndpoint)--()-[:DFG*]->(hr2:HttpRequest) WHERE NOT (hr2)-[:TO]->(:HttpEndpoint) AND NOT EXISTS { MATCH path2=(ps1)--()-[:DFG*]->(hr3:HttpRequest)-[:TO]->(he3:HttpEndpoint)--()-[:DFG*]->(hr4:HttpRequest) WHERE NOT (hr4)-[:TO]->(:HttpEndpoint) AND ((hr4.name='DELETE') OR (hr4.name='PUT')) AND (hr4.url = hr2.url) } RETURN path1"
            )
        // create a list for all pseudoidentifiers, which are communicated to extern with no delete or update call to extern connected to them via a data flow
        val listOfAllPseudoIdentifierWithNoDeleteOrUpdateToExternByIdentity = mutableListOf<Long>()
        // iterate over all paths and add to the list
        result_data_flows.forEach {
            var path = it.get("path1") as Array<*>

            // the first node is the pseudoidentifier because of the query
            val firstNode = (path.first() as InternalPath.SelfContainedSegment).start()
            if (firstNode.labels().contains("PseudoIdentifier")) {
                // add the pseudoidentifier to the list if it is not already in it
                if (!listOfAllPseudoIdentifierWithNoDeleteOrUpdateToExternByIdentity.contains(
                        firstNode.id()
                    )
                )
                    listOfAllPseudoIdentifierWithNoDeleteOrUpdateToExternByIdentity.add(
                        firstNode.id()
                    )
            }
        }
        // if the code is compliant to article 19, the list should be empty
        assertEquals(0, listOfAllPseudoIdentifierWithNoDeleteOrUpdateToExternByIdentity.size)

        val result_data_storage =
            executePPGAndQuery(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/GDPRComplianceChecks/NotificationObligation/Python"
                ),
                listOf(Path(".")),
                "MATCH path1=(ps1:PseudoIdentifier)--()-[:DFG*]->(hr1:HttpRequest)-[:TO]->(he1:HttpEndpoint)--()-[:DFG*]-(hr2:HttpRequest) WHERE NOT (hr2)-[:TO]-(:HttpEndpoint) WITH COLLECT(DISTINCT hr2.url) as externalDataRecipients MATCH path2=(:FileWrite)-[:CALLS]->(m:MemberCallExpression)-[:ARGUMENTS]->()<-[:DFG*]-(l2:Literal) WHERE ALL(recipient IN externalDataRecipients WHERE l2.value CONTAINS recipient) RETURN path2"
            )
        // iterate over all found paths and check if the first node is a FileWrite
        result_data_storage.forEach {
            val path = it.get("path2") as Array<*>
            // the first node is the literal, which contains the name of the personal data recipient
            val firstNode = (path.first() as InternalPath.SelfContainedSegment).start()
            // if the first node is a FileWrite => A call expression that writes a literal containing information of the data recipients could be found => the code is compliant to article 19
            assertTrue(firstNode.labels().contains("FileWrite"))
        }
    }

    @Test
    fun checkComplianceToArticle19_validation() {
        val result_data_flows =
            executePPGAndQuery(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/GDPRComplianceChecks/NotificationObligation/Python_validation"
                ),
                listOf(Path(".")),
                "MATCH path1=(ps1:PseudoIdentifier)--()-[:DFG*]->(hr1:HttpRequest)-[:TO]->(he1:HttpEndpoint)--()-[:DFG*]->(hr2:HttpRequest) WHERE NOT (hr2)-[:TO]->(:HttpEndpoint) AND NOT EXISTS { MATCH path2=(ps1)--()-[:DFG*]->(hr3:HttpRequest)-[:TO]->(he3:HttpEndpoint)--()-[:DFG*]->(hr4:HttpRequest) WHERE NOT (hr4)-[:TO]->(:HttpEndpoint) AND ((hr4.name='DELETE') OR (hr4.name='PUT')) AND (hr4.url = hr2.url) } RETURN path1"
            )
        // create a list for all pseudoidentifiers, which are communicated to extern with no delete
        // or update call to extern connected to them via a data flow
        val listOfAllPseudoIdentifierWithNoDeleteOrUpdateToExternByIdentity = mutableListOf<Long>()
        // iterate over all paths and add to the list
        result_data_flows.forEach {
            var path = it.get("path1") as Array<*>

            // the first node is the pseudoidentifier because of the query
            val firstNode = (path.first() as InternalPath.SelfContainedSegment).start()
            if (firstNode.labels().contains("PseudoIdentifier")) {
                // add the pseudoidentifier to the list if it is not already in it
                if (!listOfAllPseudoIdentifierWithNoDeleteOrUpdateToExternByIdentity.contains(
                        firstNode.id()
                    )
                )
                    listOfAllPseudoIdentifierWithNoDeleteOrUpdateToExternByIdentity.add(
                        firstNode.id()
                    )
            }
        }
        // check if the code is not compliant to article 19
        assertNotEquals(0, listOfAllPseudoIdentifierWithNoDeleteOrUpdateToExternByIdentity.size)

        val result_data_storage =
            executePPGAndQuery(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/GDPRComplianceChecks/NotificationObligation/Python_validation"
                ),
                listOf(Path(".")),
                "MATCH path1=(ps1:PseudoIdentifier)--()-[:DFG*]->(hr1:HttpRequest)-[:TO]->(he1:HttpEndpoint)--()-[:DFG*]-(hr2:HttpRequest) WHERE NOT (hr2)-[:TO]-(:HttpEndpoint) WITH COLLECT(DISTINCT hr2.url) as externalDataRecipients MATCH path2=(:FileWrite)-[:CALLS]->(m:MemberCallExpression)-[:ARGUMENTS]->()<-[:DFG*]-(l2:Literal) WHERE ALL(recipient IN externalDataRecipients WHERE l2.value CONTAINS recipient) RETURN path2"
            )
        // iterate over all found paths and check if the first node is a FileWrite
        result_data_storage.forEach {
            val path = it.get("path2") as Array<*>
            // the first node is the literal, which contains the name of the personal data recipient
            val firstNode = (path.first() as InternalPath.SelfContainedSegment).start()
            // if the first node is a FileWrite => A call expression that writes a literal containing information of the data recipients could be found => the code is compliant to article 19
            assertFalse(firstNode.labels().contains("FileWrite"))
        }
    }

    @Test
    fun checkComplianceToArticle20_paragraph_1() {
        val result =
            executePPGAndQuery(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/GDPRComplianceChecks/RightToDataPortability/Python"
                ),
                listOf(Path(".")),
                "MATCH path1=(psi:PseudoIdentifier)--()-[:DFG*]->(hr1:HttpRequest {name: \"POST\"})-[:DFG*]->(he1:HttpEndpoint)-[:DFG*]->(d1:DatabaseQuery {type:\"CREATE\"}) WHERE NOT EXISTS {  MATCH path2=(psi)--()-[:DFG*]->(hr2:HttpRequest {name: \"GET\"})-[:DFG*]->(he2:HttpEndpoint {method: \"GET\"})-[:DFG*]->(d2:DatabaseQuery {type:\"READ\"})-[:DFG*]->({name: \"HttpStatus.OK\"}), path3=(:FileWrite)-[:CALLS]->(m:MemberCallExpression)-[:ARGUMENTS]->(:Node)<-[:DFG*]-(hr2) WHERE (d1)-[:STORAGE]->(:DatabaseStorage)<-[:STORAGE]-(d2) } RETURN path1"
            )
        // create a list for all pseudoidentifiers with no compliant data portability
        val listOfAllPseudoIdentifierWithNoCompliantDataPortabilityByIdentity =
            mutableListOf<Long>()
        // iterate over all paths and add to the list
        result.forEach {
            val path = it.get("path1") as Array<*>

            // the first node is the pseudoidentifier because of the query
            val firstNode = (path.first() as InternalPath.SelfContainedSegment).start()
            if (firstNode.labels().contains("PseudoIdentifier")) {
                // add the pseudoidentifier to the list if it is not already in it
                if (!listOfAllPseudoIdentifierWithNoCompliantDataPortabilityByIdentity.contains(
                        firstNode.id()
                    )
                )
                    listOfAllPseudoIdentifierWithNoCompliantDataPortabilityByIdentity.add(
                        firstNode.id()
                    )
            }
        }
        // if the code is compliant to article 20(1), the list should be empty
        assertEquals(1, listOfAllPseudoIdentifierWithNoCompliantDataPortabilityByIdentity.size)
    }

    @Test
    fun checkComplianceToArticle20_paragraph_1_validation() {
        val result =
            executePPGAndQuery(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/GDPRComplianceChecks/RightToDataPortability/Python_validation"
                ),
                listOf(Path(".")),
                "MATCH path1=(psi:PseudoIdentifier)--()-[:DFG*]->(hr1:HttpRequest {name: \"POST\"})-[:DFG*]->(he1:HttpEndpoint)-[:DFG*]->(d1:DatabaseQuery {type:\"CREATE\"}) WHERE NOT EXISTS {  MATCH path2=(psi)--()-[:DFG*]->(hr2:HttpRequest {name: \"GET\"})-[:DFG*]->(he2:HttpEndpoint {method: \"GET\"})-[:DFG*]->(d2:DatabaseQuery {type:\"READ\"})-[:DFG*]->({name: \"HttpStatus.OK\"}), path3=(:FileWrite)-[:CALLS]->(m:MemberCallExpression)-[:ARGUMENTS]->(:Node)<-[:DFG*]-(hr2) WHERE (d1)-[:STORAGE]->(:DatabaseStorage)<-[:STORAGE]-(d2) } RETURN path1"
            )
        // create a list for all pseudoidentifiers with no compliant data portability
        val listOfAllPseudoIdentifierWithNoCompliantDataPortabilityByIdentity =
            mutableListOf<Long>()
        // iterate over all paths and add to the list
        result.forEach {
            val path = it.get("path1") as Array<*>

            // the first node is the pseudoidentifier because of the query
            val firstNode = (path.first() as InternalPath.SelfContainedSegment).start()
            if (firstNode.labels().contains("PseudoIdentifier")) {
                // add the pseudoidentifier to the list if it is not already in it
                if (!listOfAllPseudoIdentifierWithNoCompliantDataPortabilityByIdentity.contains(
                        firstNode.id()
                    )
                )
                    listOfAllPseudoIdentifierWithNoCompliantDataPortabilityByIdentity.add(
                        firstNode.id()
                    )
            }
        }
        // if the code is compliant to article 20(1), the list should be empty
        assertNotEquals(0, listOfAllPseudoIdentifierWithNoCompliantDataPortabilityByIdentity.size)
    }

    @Test
    fun checkComplianceToArticle20_paragraph_2() {
        val result =
            executePPGAndQuery(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/GDPRComplianceChecks/RightToDataPortability/Python"
                ),
                listOf(Path(".")),
                "MATCH path1=(psi:PseudoIdentifier)--()-[:DFG*]->(hr1:HttpRequest {name: \"POST\"})--()-[:DFG*]->(he1:HttpEndpoint)--()-[:DFG*]->(d1:DatabaseQuery {type: \"CREATE\"}) WHERE NOT EXISTS { MATCH path2=(psi)--()-[:DFG*]->(hr2:HttpRequest)--()-[:DFG*]->(he2:HttpEndpoint)--()-[:DFG*]->(d2:DatabaseQuery {type:\"READ\"})-[:DFG*]->(hr3:HttpRequest {name: \"PUT\"}) WHERE NOT (hr3)-[:TO]-(:HttpEndpoint) AND (d1)-[:STORAGE]->(:DatabaseStorage)<-[:STORAGE]-(d2) } RETURN path1"
            )
        // create a list for all pseudoidentifiers with no compliant data portability (to external
        // service)
        val listOfAllPseudoIdentifierWithNoCompliantDataPortabilityToExternalServiceByIdentity =
            mutableListOf<Long>()
        // iterate over all paths and add to the list
        result.forEach {
            var path = it.get("path1") as Array<*>

            // the first node is the pseudoidentifier because of the query
            val firstNode = (path.first() as InternalPath.SelfContainedSegment).start()
            if (firstNode.labels().contains("PseudoIdentifier")) {
                // add the pseudoidentifier to the list if it is not already in it
                if (!listOfAllPseudoIdentifierWithNoCompliantDataPortabilityToExternalServiceByIdentity
                        .contains(firstNode.id())
                )
                    listOfAllPseudoIdentifierWithNoCompliantDataPortabilityToExternalServiceByIdentity
                        .add(firstNode.id())
            }
        }
        // if the code is compliant to article 20(2), the list should be empty
        assertEquals(
            0,
            listOfAllPseudoIdentifierWithNoCompliantDataPortabilityToExternalServiceByIdentity.size
        )
    }

    @Test
    fun checkComplianceToArticle20_paragraph_2_validation() {
        val result =
            executePPGAndQuery(
                Path(
                    System.getProperty("user.dir") +
                        "/../ppg-testing-library/GDPRComplianceChecks/RightToDataPortability/Python_validation"
                ),
                listOf(Path(".")),
                "MATCH path1=(psi:PseudoIdentifier)--()-[:DFG*]->(hr1:HttpRequest {name: \"POST\"})--()-[:DFG*]->(he1:HttpEndpoint)--()-[:DFG*]->(d1:DatabaseQuery {type: \"CREATE\"}) WHERE NOT EXISTS { MATCH path2=(psi)--()-[:DFG*]->(hr2:HttpRequest)--()-[:DFG*]->(he2:HttpEndpoint)--()-[:DFG*]->(d2:DatabaseQuery {type:\"READ\"})-[:DFG*]->(hr3:HttpRequest {name: \"PUT\"}) WHERE NOT (hr3)-[:TO]-(:HttpEndpoint) AND (d1)-[:STORAGE]->(:DatabaseStorage)<-[:STORAGE]-(d2) } RETURN path1"
            )
        // create a list for all pseudoidentifiers with no compliant data portability (to external
        // service)
        val listOfAllPseudoIdentifierWithNoCompliantDataPortabilityToExternalServiceByIdentity =
            mutableListOf<Long>()
        // iterate over all paths and add to the list
        result.forEach {
            var path = it.get("path1") as Array<*>

            // the first node is the pseudoidentifier because of the query
            val firstNode = (path.first() as InternalPath.SelfContainedSegment).start()
            if (firstNode.labels().contains("PseudoIdentifier")) {
                // add the pseudoidentifier to the list if it is not already in it
                if (!listOfAllPseudoIdentifierWithNoCompliantDataPortabilityToExternalServiceByIdentity
                        .contains(firstNode.id())
                )
                    listOfAllPseudoIdentifierWithNoCompliantDataPortabilityToExternalServiceByIdentity
                        .add(firstNode.id())
            }
        }
        // if the code is compliant to article 20(2), the list should be empty
        assertNotEquals(
            0,
            listOfAllPseudoIdentifierWithNoCompliantDataPortabilityToExternalServiceByIdentity.size
        )
    }
}
