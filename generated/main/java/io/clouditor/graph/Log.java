package io.clouditor.graph;

import de.fraunhofer.aisec.cpg.graph.Node;

public class Log extends Auditing {

	protected boolean activated;

	public Log(boolean activated) {
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