package io.clouditor.graph

import io.clouditor.graph.utils.DatabaseQueryType
import kotlin.io.path.Path
import kotlin.test.assertEquals
import org.junit.Test
import org.junit.jupiter.api.Tag
import org.neo4j.driver.internal.InternalPath

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
                "MATCH p=(n:PseudoIdentifier)--()-[:DFG*]->(hr:HttpRequest)--()-[:DFG*]->(he:HttpEndpoint)--()-[:DFG*]->(d:DatabaseQuery) WHERE (d.type = 'UPDATE') AND (hr.name = 'PUT') AND (he.method = 'PUT') WITH COLLECT(n) as pseudosWithUpdate MATCH path=(psi:PseudoIdentifier)--() WHERE NOT psi IN pseudosWithUpdate RETURN path"
            )

        // create a list for all pseudoidentifiers with no update call connected to them via a data flow
        val listOfAllPseudoIdentifierWithNoUpdateByIdentity = mutableListOf<Long>()
        // iterate over all paths and add to the list
        result.forEach {
            var path = it.get("path") as Array<*>

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
    fun checkComplianceToArticle17_paragraph_1() {
        val result =
            executePPGAndQuery(
                Path(
                    System.getProperty("user.dir") +
                            "/../ppg-testing-library/GDPRComplianceChecks/RightToErasure/Python"

                ),
                listOf(Path(".")),
                "MATCH p=(n:PseudoIdentifier)--()-[:DFG*]->(hr:HttpRequest)--()-[:DFG*]->(he:HttpEndpoint)--()-[:DFG*]->(d:DatabaseQuery) WHERE (d.type = 'DELETE') AND (hr.name = 'DELETE') AND (he.method = 'DELETE') WITH COLLECT(n) as pseudosWithDelete MATCH path=(psi:PseudoIdentifier)--() WHERE NOT psi IN pseudosWithDelete RETURN path"
            )
        // create a list for all pseudoidentifiers with no delete call connected to them via a data flow
        val listOfAllPseudoIdentifierWithNoDeleteByIdentity = mutableListOf<Long>()
        // iterate over all paths and add to the list
        result.forEach {
            var path = it.get("path") as Array<*>

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
    fun checkComplianceToArticle17_paragraph_2() {
        val result =
            executePPGAndQuery(
                Path(
                    System.getProperty("user.dir") +
                            "/../ppg-testing-library/GDPRComplianceChecks/RightToErasure/Python"

                ),
                listOf(Path(".")),
                "MATCH p1=(psi1:PseudoIdentifier)--()-[:DFG*]->(hr1:HttpRequest)--()-[:DFG*]->(hr2:HttpRequest) WHERE NOT EXISTS { MATCH (psi1)--()-[:DFG*]->(hr1:HttpRequest)-[:TO]->(h:HttpEndpoint)--()-[:DFG*]->(hr2:HttpRequest)-[:TO]->(:HttpEndpoint)} AND (hr1.name='DELETE') AND NOT (hr2.name = hr1.name) RETURN p1"
            )
        // TODO: DELETE NOTE: Alle pseudo die extern kommunziert werden bekommt man mit folgender query: MATCH p1=(psi1:PseudoIdentifier)--()-[:DFG*]->(:HttpRequest)--()-[:DFG*]->(:HttpRequest) WHERE NOT EXISTS { MATCH (psi1)--()-[:DFG*]->(hr1:HttpRequest)-[:TO]->(h:HttpEndpoint)--()-[:DFG*]->(hr2:HttpRequest)-[:TO]->(:HttpEndpoint)} RETURN p1
        // create a list for all pseudoidentifiers, which are communicated to extern with no delete call to extern connected to them via a data flow
        val listOfAllPseudoIdentifierWithNoDeleteToExternByIdentity = mutableListOf<Long>()
        // iterate over all paths and add to the list
        result.forEach {
            var path = it.get("p1") as Array<*>

            // the first node is the pseudoidentifier because of the query
            val firstNode = (path.first() as InternalPath.SelfContainedSegment).start()
            if (firstNode.labels().contains("PseudoIdentifier")) {
                // add the pseudoidentifier to the list if it is not already in it
                if (!listOfAllPseudoIdentifierWithNoDeleteToExternByIdentity.contains(firstNode.id()))
                    listOfAllPseudoIdentifierWithNoDeleteToExternByIdentity.add(firstNode.id())
            }
        }
        // if the code is compliant to article 17(2), the list should be empty
        assertEquals(0, listOfAllPseudoIdentifierWithNoDeleteToExternByIdentity.size)
    }

    @Test
    fun checkComplianceToArticle19() {

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
                "MATCH p1=(psi:PseudoIdentifier)--()-[:DFG*]->(hr1:HttpRequest)--()-[:DFG*]->(he1:HttpEndpoint)--()-[:DFG*]->(d:DatabaseQuery {type:\"READ\"})--()-[:DFG*]->({name: \"HttpStatus.OK\"}), p2=(m:MemberCallExpression {name:\"write\"})-[:ARGUMENTS]-(:Node)--()-[:DFG*]-(hr1), p3=(m2:MemberCallExpression)--()-[:DFG*]-(:Node)-[:REFERS_TO]-(:DeclaredReferenceExpression)-[:BASE]-(m) WITH COLLECT(psi) as correctPseudos MATCH p4=(psi2:PseudoIdentifier)--(:Node) WHERE NOT psi2 IN correctPseudos RETURN p4"
            )
        // create a list for all pseudoidentifiers with no compliant data portability
        val listOfAllPseudoIdentifierWithNoCompliantDataPortabilityByIdentity = mutableListOf<Long>()
        // iterate over all paths and add to the list
        result.forEach {
            var path = it.get("p4") as Array<*>

            // the first node is the pseudoidentifier because of the query
            val firstNode = (path.first() as InternalPath.SelfContainedSegment).start()
            if (firstNode.labels().contains("PseudoIdentifier")) {
                // add the pseudoidentifier to the list if it is not already in it
                if (!listOfAllPseudoIdentifierWithNoCompliantDataPortabilityByIdentity.contains(firstNode.id()))
                    listOfAllPseudoIdentifierWithNoCompliantDataPortabilityByIdentity.add(firstNode.id())
            }
        }
        // if the code is compliant to article 20(1), the list should be empty
        assertEquals(0, listOfAllPseudoIdentifierWithNoCompliantDataPortabilityByIdentity.size)


        // MATCH p1=(psi:PseudoIdentifier)--()-[:DFG*]->(hr1:HttpRequest)--()-[:DFG*]->(he1:HttpEndpoint)--()-[:DFG*]->(d:DatabaseQuery {type:"READ"})--()-[:DFG*]->({name: "HttpStatus.OK"}) return p1
        // Alle nodes die das erfüllen sind compliant: MATCH p1=(psi:PseudoIdentifier)--()-[:DFG*]->(hr1:HttpRequest)--()-[:DFG*]->(he1:HttpEndpoint)--()-[:DFG*]->(d:DatabaseQuery {type:"READ"})--()-[:DFG*]->({name: "HttpStatus.OK"}), p2=(m:MemberCallExpression {name:"write"})-[:ARGUMENTS]-(:Node)--()-[:DFG*]-(hr1), p3=(m2:MemberCallExpression)--()-[:DFG*]-(:Node)-[:REFERS_TO]-(:DeclaredReferenceExpression)-[:BASE]-(m) return p2
        // Finale Abfrage: MATCH p1=(psi:PseudoIdentifier)--()-[:DFG*]->(hr1:HttpRequest)--()-[:DFG*]->(he1:HttpEndpoint)--()-[:DFG*]->(d:DatabaseQuery {type:"READ"})--()-[:DFG*]->({name: "HttpStatus.OK"}), p2=(m:MemberCallExpression {name:"write"})-[:ARGUMENTS]-(:Node)--()-[:DFG*]-(hr1), p3=(m2:MemberCallExpression)--()-[:DFG*]-(:Node)-[:REFERS_TO]-(:DeclaredReferenceExpression)-[:BASE]-(m) WITH COLLECT(psi) as correctPseudos MATCH p4=(psi2:PseudoIdentifier)--(:Node) WHERE NOT psi2 IN correctPseudos RETURN p4
    }

    @Test
    fun checkComplianceToArticle20_paragraph_2() {
        // query: MATCH p1=(psi:PseudoIdentifier)--()-[:DFG*]->(hr1:HttpRequest)--()-[:DFG*]->(he1:HttpEndpoint)--()-[:DFG*]->(d:DatabaseQuery {type:"READ"})--()-[:DFG*]->(hr2:HttpRequest {name: "PUT"}) WHERE NOT EXISTS {MATCH p2=(hr2)--()-[:DFG*]->(he2:HttpEndpoint)} WITH COLLECT(psi) as correctPseudos MATCH p3=(psi2:PseudoIdentifier)--(:Node) WHERE NOT psi2 IN correctPseudos RETURN p3
        val result =
            executePPGAndQuery(
                Path(
                    System.getProperty("user.dir") +
                            "/../ppg-testing-library/GDPRComplianceChecks/RightToDataPortability/Python"

                ),
                listOf(Path(".")),
                "MATCH p1=(psi:PseudoIdentifier)--()-[:DFG*]->(hr1:HttpRequest)--()-[:DFG*]->(he1:HttpEndpoint)--()-[:DFG*]->(d:DatabaseQuery {type:\"READ\"})--()-[:DFG*]->(hr2:HttpRequest {name: \"PUT\"}) WHERE NOT EXISTS {MATCH p2=(hr2)--()-[:DFG*]->(he2:HttpEndpoint)} WITH COLLECT(psi) as correctPseudos MATCH p3=(psi2:PseudoIdentifier)--(:Node) WHERE NOT psi2 IN correctPseudos RETURN p3"
            )
        // create a list for all pseudoidentifiers with no compliant data portability (to external service)
        val listOfAllPseudoIdentifierWithNoCompliantDataPortabilityToExternalServiceByIdentity = mutableListOf<Long>()
        // iterate over all paths and add to the list
        result.forEach {
            var path = it.get("p3") as Array<*>

            // the first node is the pseudoidentifier because of the query
            val firstNode = (path.first() as InternalPath.SelfContainedSegment).start()
            if (firstNode.labels().contains("PseudoIdentifier")) {
                // add the pseudoidentifier to the list if it is not already in it
                if (!listOfAllPseudoIdentifierWithNoCompliantDataPortabilityToExternalServiceByIdentity.contains(firstNode.id()))
                    listOfAllPseudoIdentifierWithNoCompliantDataPortabilityToExternalServiceByIdentity.add(firstNode.id())
            }
        }
        // if the code is compliant to article 20(2), the list should be empty
        assertEquals(0, listOfAllPseudoIdentifierWithNoCompliantDataPortabilityToExternalServiceByIdentity.size)
    }

    // For testing purposes
    @Test
    fun checkComplianceOfArticle17() {
        val result =
            executePPGAndQuery(
                Path(
                    System.getProperty("user.dir") +
                            "/../ppg-testing-library/GDPRComplianceChecks/RightToErasure/Python"

                ),
                listOf(Path(".")),
                "MATCH p=(n:PseudoIdentifier)--()-[:DFG*]->(d:DatabaseQuery) WHERE (d.type = 'DELETE') RETURN p"
            )
        // First we have to check collect all pseudoidentifiers in code.
        // Second we have to check whether all pseduoidentifiers can be deleted.
        //   => This means we have to check every path (Pseudoidentifer => Delete database operation)

        // Second part done
        result.forEach {
            var path = it.get("p") as Array<*>
            // the first node should be the label
            val firstNode = (path.first() as InternalPath.SelfContainedSegment).start()
            // Ein Path besteht immer aus Segmenten. 1. Segment => verbunden mit 2. Segment usw...
            // Folgendes print statement kann man verwenden, um die endLine rauszubekommen, bei welcher das Data label versehen wurde
            //print((path.first() as InternalPath.SelfContainedSegment).end().get("endLine"))
            assert(firstNode.labels().contains("PseudoIdentifier"))
            // the last node should be a DatabaseQuery
            val lastNode = (path.last() as InternalPath.SelfContainedSegment).end()
            assert(lastNode.labels().contains("DatabaseQuery"))
            // lastNode should be a deleting operation
            if (lastNode.get("type").equals("DELETE")) {
                // Evtl query für labelNode schreiben und dann diese ausgeben (relationship labeled node...)
                // Wie bekomme ich speziellen Knoten aus der List?
            }

            // Remove from method get() generated character '"' and generate enum type
            val typeOfDatabaseQuery = DatabaseQueryType.valueOf(lastNode.get("type").toString().replace("\"", ""))
            assertEquals(DatabaseQueryType.DELETE, typeOfDatabaseQuery, "No deleting operation can be found")
        }

//        result.first().apply {
//            var path = this.get("p") as Array<*>
//            // the first node should be the label
//            val firstNode = (path.first() as InternalPath.SelfContainedSegment).start()
//            assert(firstNode.labels().contains("PseudoIdentifier"))
//            // the last node should be a DatabaseQuery
//            val lastNode = (path.last() as InternalPath.SelfContainedSegment).end()
//            assert(lastNode.labels().contains("DatabaseQuery"))
//            // lastNode should be a deleting operation
//            assertTrue(lastNode.get("isDeleting").isTrue, "No deleting operation can be found")
//        }
    }
}
