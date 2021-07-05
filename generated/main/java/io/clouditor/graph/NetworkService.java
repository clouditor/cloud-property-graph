package io.clouditor.graph;

import java.util.ArrayList;
import de.fraunhofer.aisec.cpg.graph.Node;
import java.util.Map;
import org.neo4j.ogm.annotation.Transient;

public class NetworkService extends Networking {

	protected Compute compute;
	protected ArrayList<String> ips;
	protected ArrayList<Short> ports;

	public NetworkService(Compute compute, ArrayList<String> ips, ArrayList<Short> ports, GeoLocation geoLocation,
			Map<String, String> labels) {
		super(geoLocation, labels);
		setCompute(compute);
		setIps(ips);
		setPorts(ports);
	}

	public Compute getCompute() {
		return compute;
	}

	public void setCompute(Compute compute) {
		this.compute = compute;
	}

	public ArrayList<String> getIps() {
		return ips;
	}

	public void setIps(ArrayList<String> ips) {
		this.ips = ips;
	}

	public ArrayList<Short> getPorts() {
		return ports;
	}

	public void setPorts(ArrayList<Short> ports) {
		this.ports = ports;
	}
}