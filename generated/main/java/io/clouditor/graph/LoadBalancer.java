package io.clouditor.graph;

import java.util.List;
import de.fraunhofer.aisec.cpg.graph.Node;
import java.util.Map;
import org.neo4j.ogm.annotation.Transient;

public class LoadBalancer extends Compute {

	protected HttpEndpoint httpEndpoint;
	protected List<NetworkService> backend;
	protected AccessRestriction accessRestriction;
	protected String url;

	public LoadBalancer(HttpEndpoint httpEndpoint, List<NetworkService> backend, AccessRestriction accessRestriction,
			String url, GeoLocation geoLocation, Map<String, String> labels) {
		super(geoLocation, labels);
		setHttpEndpoint(httpEndpoint);
		setBackend(backend);
		setAccessRestriction(accessRestriction);
		setUrl(url);
	}

	public HttpEndpoint getHttpEndpoint() {
		return httpEndpoint;
	}

	public void setHttpEndpoint(HttpEndpoint httpEndpoint) {
		this.httpEndpoint = httpEndpoint;
	}

	public List<NetworkService> getBackend() {
		return backend;
	}

	public void setBackend(List<NetworkService> backend) {
		this.backend = backend;
	}

	public AccessRestriction getAccessRestriction() {
		return accessRestriction;
	}

	public void setAccessRestriction(AccessRestriction accessRestriction) {
		this.accessRestriction = accessRestriction;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}