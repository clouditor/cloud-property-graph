package io.clouditor.graph;

import de.fraunhofer.aisec.cpg.graph.Node;
import java.util.Map;
import org.neo4j.ogm.annotation.Transient;

public class IdentityManagement extends CloudResource {

	protected Authorization authorization;
	protected Authenticity authenticity;

	public IdentityManagement(Authorization authorization, Authenticity authenticity, GeoLocation geoLocation,
			Map<String, String> labels) {
		super(geoLocation, labels);
		setAuthorization(authorization);
		setAuthenticity(authenticity);
	}

	public Authorization getAuthorization() {
		return authorization;
	}

	public void setAuthorization(Authorization authorization) {
		this.authorization = authorization;
	}

	public Authenticity getAuthenticity() {
		return authenticity;
	}

	public void setAuthenticity(Authenticity authenticity) {
		this.authenticity = authenticity;
	}
}