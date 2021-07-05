package io.clouditor.graph;

import de.fraunhofer.aisec.cpg.graph.Node;
import java.util.Map;
import org.neo4j.ogm.annotation.Transient;

public class CloudResource extends Node {

	protected GeoLocation geoLocation;
	@Transient
	protected Map<String, String> labels;

	public CloudResource(GeoLocation geoLocation, Map<String, String> labels) {
		setGeoLocation(geoLocation);
		setLabels(labels);
	}

	public GeoLocation getGeoLocation() {
		return geoLocation;
	}

	public void setGeoLocation(GeoLocation geoLocation) {
		this.geoLocation = geoLocation;
	}

	public Map<String, String> getLabels() {
		return labels;
	}

	public void setLabels(Map<String, String> labels) {
		this.labels = labels;
	}
}