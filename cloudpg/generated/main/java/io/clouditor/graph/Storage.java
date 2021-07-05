package io.clouditor.graph;

import de.fraunhofer.aisec.cpg.graph.Node;
import java.util.Map;
import org.neo4j.ogm.annotation.Transient;

public class Storage extends CloudResource {

	protected AtRestEncryption atRestEncryption;

	public Storage(AtRestEncryption atRestEncryption, GeoLocation geoLocation, Map<String, String> labels) {
		super(geoLocation, labels);
		setAtRestEncryption(atRestEncryption);
	}

	public AtRestEncryption getAtRestEncryption() {
		return atRestEncryption;
	}

	public void setAtRestEncryption(AtRestEncryption atRestEncryption) {
		this.atRestEncryption = atRestEncryption;
	}
}