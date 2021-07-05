package io.clouditor.graph;

import java.util.List;
import de.fraunhofer.aisec.cpg.graph.statements.expressions.CallExpression;

public class HttpRequest extends Functionality {

	protected List<HttpEndpoint> to;
	protected CallExpression call;

	public HttpRequest(List<HttpEndpoint> to, CallExpression call) {
		setTo(to);
		setCall(call);
	}

	public List<HttpEndpoint> getTo() {
		return to;
	}

	public void setTo(List<HttpEndpoint> to) {
		this.to = to;
	}

	public CallExpression getCall() {
		return call;
	}

	public void setCall(CallExpression call) {
		this.call = call;
	}
}