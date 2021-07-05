package io.clouditor.graph;

import de.fraunhofer.aisec.cpg.graph.Node;
import java.util.Map;
import org.neo4j.ogm.annotation.Transient;

public class ContainerImage extends Image {

	public ContainerImage(Application application, GeoLocation geoLocation, Map<String, String> labels) {
		super(application, geoLocation, labels);
	}
}