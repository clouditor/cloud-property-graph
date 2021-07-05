package io.clouditor.graph;

import de.fraunhofer.aisec.cpg.graph.Node;
import java.util.Map;
import org.neo4j.ogm.annotation.Transient;

public class RoleAssignment extends IdentityManagement {

	public RoleAssignment(Authorization authorization, Authenticity authenticity, GeoLocation geoLocation,
			Map<String, String> labels) {
		super(authorization, authenticity, geoLocation, labels);
	}
}