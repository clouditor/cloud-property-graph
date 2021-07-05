package io.clouditor.graph;

import java.util.List;
import de.fraunhofer.aisec.cpg.graph.Node;
import java.util.Map;
import org.neo4j.ogm.annotation.Transient;

public class ContainerOrchestration extends CloudResource {

	protected ResourceLogging resourceLogging;
	protected List<Container> containers;
	protected String managementUrl;

	public ContainerOrchestration(ResourceLogging resourceLogging, List<Container> containers, String managementUrl,
			GeoLocation geoLocation, Map<String, String> labels) {
		super(geoLocation, labels);
		setResourceLogging(resourceLogging);
		setContainers(containers);
		setManagementUrl(managementUrl);
	}

	public ResourceLogging getResourceLogging() {
		return resourceLogging;
	}

	public void setResourceLogging(ResourceLogging resourceLogging) {
		this.resourceLogging = resourceLogging;
	}

	public List<Container> getContainers() {
		return containers;
	}

	public void setContainers(List<Container> containers) {
		this.containers = containers;
	}

	public String getManagementUrl() {
		return managementUrl;
	}

	public void setManagementUrl(String managementUrl) {
		this.managementUrl = managementUrl;
	}
}