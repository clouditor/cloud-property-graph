package io.clouditor.graph;

import java.util.List;
import de.fraunhofer.aisec.cpg.graph.Node;

public class ObjectStorageRequest extends Functionality {

	protected List<ObjectStorage> to;
	protected Node source;
	protected String type;

	public ObjectStorageRequest(List<ObjectStorage> to, Node source, String type) {
		super();
		setTo(to);
		setSource(source);
		setType(type);
	}

	public List<ObjectStorage> getTo() {
		return to;
	}

	public void setTo(List<ObjectStorage> to) {
		this.to = to;
	}

	public Node getSource() {
		return source;
	}

	public void setSource(Node source) {
		this.source = source;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}