package io.clouditor.graph;

import de.fraunhofer.aisec.cpg.graph.Node;
import java.util.Map;
import org.neo4j.ogm.annotation.Transient;

public class Function extends Compute {

	public Function(GeoLocation geoLocation, Map<String, String> labels) {
		super(geoLocation, labels);
	}
}