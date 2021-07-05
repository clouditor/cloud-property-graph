package io.clouditor.graph;

import java.util.List;
import de.fraunhofer.aisec.cpg.graph.declarations.FunctionDeclaration;
import de.fraunhofer.aisec.cpg.graph.Node;

public class ProxiedEndpoint extends HttpEndpoint {

	protected List<HttpEndpoint> proxyTarget;

	public ProxiedEndpoint(List<HttpEndpoint> proxyTarget, Authenticity authenticity,
			TransportEncryption transportEncryption, String path, String url, String method,
			FunctionDeclaration handler) {
		super(authenticity, transportEncryption, path, url, method, handler);
		setProxyTarget(proxyTarget);
	}

	public List<HttpEndpoint> getProxyTarget() {
		return proxyTarget;
	}

	public void setProxyTarget(List<HttpEndpoint> proxyTarget) {
		this.proxyTarget = proxyTarget;
	}
}