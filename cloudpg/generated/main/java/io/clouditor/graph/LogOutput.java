package io.clouditor.graph;

import java.util.List;
import de.fraunhofer.aisec.cpg.graph.statements.expressions.Expression;
import de.fraunhofer.aisec.cpg.graph.statements.expressions.CallExpression;
import de.fraunhofer.aisec.cpg.graph.Node;

public class LogOutput extends Functionality {

	protected List<Logging> to;
	protected Expression value;
	protected CallExpression call;

	public LogOutput(List<Logging> to, Expression value, CallExpression call) {
		super();
		setTo(to);
		setValue(value);
		setCall(call);
	}

	public List<Logging> getTo() {
		return to;
	}

	public void setTo(List<Logging> to) {
		this.to = to;
	}

	public Expression getValue() {
		return value;
	}

	public void setValue(Expression value) {
		this.value = value;
	}

	public CallExpression getCall() {
		return call;
	}

	public void setCall(CallExpression call) {
		this.call = call;
	}
}