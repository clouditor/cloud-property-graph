package io.clouditor.graph;

import de.fraunhofer.aisec.cpg.graph.Node;

public class PasswordBasedAuthentication extends Authenticity {

	protected boolean activated;

	public PasswordBasedAuthentication(boolean activated) {
		super();
		setActivated(activated);
	}

	public boolean isActivated() {
		return activated;
	}

	public void setActivated(boolean activated) {
		this.activated = activated;
	}
}