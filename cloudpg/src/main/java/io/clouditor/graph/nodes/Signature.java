package io.clouditor.graph.nodes;

import de.fraunhofer.aisec.cpg.graph.declarations.VariableDeclaration;
import io.clouditor.graph.Integrity;

public class Signature extends Integrity {

	public VariableDeclaration getMessage() {
		return message;
	}

	public void setMessage(VariableDeclaration message) {
		this.message = message;
	}

	protected VariableDeclaration message;

	public Signature(VariableDeclaration message) {
		setMessage(message);
	}
}