package io.clouditor.graph;

import de.fraunhofer.aisec.cpg.graph.Node;
import java.util.Map;
import org.neo4j.ogm.annotation.Transient;

public class NetworkInterface extends Compute {

	protected AccessRestriction accessRestriction;
	protected NetworkService networkService;

	public NetworkInterface(AccessRestriction accessRestriction, NetworkService networkService, GeoLocation geoLocation,
			Map<String, String> labels) {
		super(geoLocation, labels);
		setAccessRestriction(accessRestriction);
		setNetworkService(networkService);
	}

	public AccessRestriction getAccessRestriction() {
		return accessRestriction;
	}

	public void setAccessRestriction(AccessRestriction accessRestriction) {
		this.accessRestriction = accessRestriction;
	}

	public NetworkService getNetworkService() {
		return networkService;
	}

	public void setNetworkService(NetworkService networkService) {
		this.networkService = networkService;
	}
}