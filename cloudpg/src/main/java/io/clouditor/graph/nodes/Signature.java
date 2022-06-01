package io.clouditor.graph.nodes;

import de.fraunhofer.aisec.cpg.graph.declarations.VariableDeclaration;
import io.clouditor.graph.Integrity;

public class Signature extends Integrity {

	protected VariableDeclaration signature;
	protected VariableDeclaration message;

	public Signature(VariableDeclaration message, VariableDeclaration signature) {
		setMessage(message);
		setSignature(signature);
	}

	public VariableDeclaration getMessage() {
		return message;
	}

	public void setMessage(VariableDeclaration message) {
		this.message = message;
	}

	public VariableDeclaration getSignature() {
		return signature;
	}

	public void setSignature(VariableDeclaration signature) {
		this.signature = signature;
	}
}