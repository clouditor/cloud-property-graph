package io.clouditor.graph;

import de.fraunhofer.aisec.cpg.graph.declarations.FunctionDeclaration;
import de.fraunhofer.aisec.cpg.graph.Node;

public class HttpEndpoint extends Functionality {

	protected Authenticity authenticity;
	protected TransportEncryption transportEncryption;
	protected String path;
	protected String url;
	protected String method;
	protected FunctionDeclaration handler;

	public HttpEndpoint(Authenticity authenticity, TransportEncryption transportEncryption, String path, String url,
			String method, FunctionDeclaration handler) {
		super();
		setAuthenticity(authenticity);
		setTransportEncryption(transportEncryption);
		setPath(path);
		setUrl(url);
		setMethod(method);
		setHandler(handler);
	}

	public Authenticity getAuthenticity() {
		return authenticity;
	}

	public void setAuthenticity(Authenticity authenticity) {
		this.authenticity = authenticity;
	}

	public TransportEncryption getTransportEncryption() {
		return transportEncryption;
	}

	public void setTransportEncryption(TransportEncryption transportEncryption) {
		this.transportEncryption = transportEncryption;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public FunctionDeclaration getHandler() {
		return handler;
	}

	public void setHandler(FunctionDeclaration handler) {
		this.handler = handler;
	}
}