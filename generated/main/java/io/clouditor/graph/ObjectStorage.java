package io.clouditor.graph;

import de.fraunhofer.aisec.cpg.graph.Node;
import java.util.Map;
import org.neo4j.ogm.annotation.Transient;

public class ObjectStorage extends Storage {

	protected HttpEndpoint httpEndpoint;

	public ObjectStorage(HttpEndpoint httpEndpoint, AtRestEncryption atRestEncryption, GeoLocation geoLocation,
			Map<String, String> labels) {
		super(atRestEncryption, geoLocation, labels);
		setHttpEndpoint(httpEndpoint);
	}

	public HttpEndpoint getHttpEndpoint() {
		return httpEndpoint;
	}

	public void setHttpEndpoint(HttpEndpoint httpEndpoint) {
		this.httpEndpoint = httpEndpoint;
	}
}