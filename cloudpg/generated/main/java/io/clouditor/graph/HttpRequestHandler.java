package io.clouditor.graph;

import java.util.List;
import de.fraunhofer.aisec.cpg.graph.Node;

public class HttpRequestHandler extends Functionality {

	protected List<HttpEndpoint> httpEndpoints;
	protected Application application;
	protected String path;

	public HttpRequestHandler(List<HttpEndpoint> httpEndpoints, Application application, String path) {
		super();
		setHttpEndpoints(httpEndpoints);
		setApplication(application);
		setPath(path);
	}

	public List<HttpEndpoint> getHttpEndpoints() {
		return httpEndpoints;
	}

	public void setHttpEndpoints(List<HttpEndpoint> httpEndpoints) {
		this.httpEndpoints = httpEndpoints;
	}

	public Application getApplication() {
		return application;
	}

	public void setApplication(Application application) {
		this.application = application;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
}