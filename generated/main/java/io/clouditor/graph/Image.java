package io.clouditor.graph;

import de.fraunhofer.aisec.cpg.graph.Node;
import java.util.Map;
import org.neo4j.ogm.annotation.Transient;

public class Image extends CloudResource {

	protected Application application;

	public Image(Application application, GeoLocation geoLocation, Map<String, String> labels) {
		super(geoLocation, labels);
		setApplication(application);
	}

	public Application getApplication() {
		return application;
	}

	public void setApplication(Application application) {
		this.application = application;
	}
}