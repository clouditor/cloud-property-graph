package io.clouditor.graph;

import de.fraunhofer.aisec.cpg.graph.Node;
import java.util.List;

public class CloudResourceGroup extends Node {

	protected List<CloudResource> collectionOf;

	public CloudResourceGroup(List<CloudResource> collectionOf) {
		setCollectionOf(collectionOf);
	}

	public List<CloudResource> getCollectionOf() {
		return collectionOf;
	}

	public void setCollectionOf(List<CloudResource> collectionOf) {
		this.collectionOf = collectionOf;
	}
}