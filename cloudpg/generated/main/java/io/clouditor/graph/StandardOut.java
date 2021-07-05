package io.clouditor.graph;

import de.fraunhofer.aisec.cpg.graph.statements.expressions.CallExpression;
import de.fraunhofer.aisec.cpg.graph.Node;

public class StandardOut extends Functionality {

	protected CallExpression call;

	public StandardOut(CallExpression call) {
		super();
		setCall(call);
	}

	public CallExpression getCall() {
		return call;
	}

	public void setCall(CallExpression call) {
		this.call = call;
	}
}