package io.clouditor.graph;

import de.fraunhofer.aisec.cpg.graph.Node;

public class OTPBasedAuthentication extends Authenticity {

	protected boolean activated;

	public OTPBasedAuthentication(boolean activated) {
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