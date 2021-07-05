package io.clouditor.graph;

import de.fraunhofer.aisec.cpg.graph.Node;

public class AccessRestriction extends Authorization {

	protected boolean inbound;
	protected String restrictedPorts;

	public AccessRestriction(boolean inbound, String restrictedPorts) {
		super();
		setInbound(inbound);
		setRestrictedPorts(restrictedPorts);
	}

	public boolean isInbound() {
		return inbound;
	}

	public void setInbound(boolean inbound) {
		this.inbound = inbound;
	}

	public String getRestrictedPorts() {
		return restrictedPorts;
	}

	public void setRestrictedPorts(String restrictedPorts) {
		this.restrictedPorts = restrictedPorts;
	}
}