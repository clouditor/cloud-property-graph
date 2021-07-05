package io.clouditor.graph;

import de.fraunhofer.aisec.cpg.graph.Node;
import java.util.Map;
import org.neo4j.ogm.annotation.Transient;

public class RelationalDB extends RelationalDatabaseService {

	public RelationalDB(GeoLocation geoLocation, Map<String, String> labels) {
		super(geoLocation, labels);
	}
}