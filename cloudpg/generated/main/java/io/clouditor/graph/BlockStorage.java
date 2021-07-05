package io.clouditor.graph;

import de.fraunhofer.aisec.cpg.graph.Node;
import java.util.Map;
import org.neo4j.ogm.annotation.Transient;

public class BlockStorage extends Storage {

	public BlockStorage(AtRestEncryption atRestEncryption, GeoLocation geoLocation, Map<String, String> labels) {
		super(atRestEncryption, geoLocation, labels);
	}
}