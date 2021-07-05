package io.clouditor.graph;

import de.fraunhofer.aisec.cpg.graph.Node;

public class GeoLocation extends Availability {

	protected String region;

	public GeoLocation(String region) {
		super();
		setRegion(region);
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}
}