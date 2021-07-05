package io.clouditor.graph;

import de.fraunhofer.aisec.cpg.graph.Node;
import java.util.Map;
import org.neo4j.ogm.annotation.Transient;

public class VirtualMachine extends Compute {

	protected Log log;
	protected BlockStorage blockStorage;
	protected NetworkInterface networkInterface;

	public VirtualMachine(Log log, BlockStorage blockStorage, NetworkInterface networkInterface,
			GeoLocation geoLocation, Map<String, String> labels) {
		super(geoLocation, labels);
		setLog(log);
		setBlockStorage(blockStorage);
		setNetworkInterface(networkInterface);
	}

	public Log getLog() {
		return log;
	}

	public void setLog(Log log) {
		this.log = log;
	}

	public BlockStorage getBlockStorage() {
		return blockStorage;
	}

	public void setBlockStorage(BlockStorage blockStorage) {
		this.blockStorage = blockStorage;
	}

	public NetworkInterface getNetworkInterface() {
		return networkInterface;
	}

	public void setNetworkInterface(NetworkInterface networkInterface) {
		this.networkInterface = networkInterface;
	}
}