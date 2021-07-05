package io.clouditor.graph;

import de.fraunhofer.aisec.cpg.graph.Node;

public class HttpServer extends Framework {

	protected HttpRequestHandler httpRequestHandler;

	public HttpServer(HttpRequestHandler httpRequestHandler) {
		super();
		setHttpRequestHandler(httpRequestHandler);
	}

	public HttpRequestHandler getHttpRequestHandler() {
		return httpRequestHandler;
	}

	public void setHttpRequestHandler(HttpRequestHandler httpRequestHandler) {
		this.httpRequestHandler = httpRequestHandler;
	}
}