package io.clouditor.graph;

import de.fraunhofer.aisec.cpg.graph.Node;
import java.util.Map;
import org.neo4j.ogm.annotation.Transient;

public class Container extends Compute {

	protected Image image;

	public Container(Image image, GeoLocation geoLocation, Map<String, String> labels) {
		super(geoLocation, labels);
		setImage(image);
	}

	public Image getImage() {
		return image;
	}

	public void setImage(Image image) {
		this.image = image;
	}
}