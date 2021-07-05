package io.clouditor.graph;

import de.fraunhofer.aisec.cpg.graph.Node;

public class Backup extends Availability {

	protected String policy;
	protected boolean activated;

	public Backup(String policy, boolean activated) {
		super();
		setPolicy(policy);
		setActivated(activated);
	}

	public String getPolicy() {
		return policy;
	}

	public void setPolicy(String policy) {
		this.policy = policy;
	}

	public boolean isActivated() {
		return activated;
	}

	public void setActivated(boolean activated) {
		this.activated = activated;
	}
}