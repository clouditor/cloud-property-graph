package io.clouditor.graph;

import de.fraunhofer.aisec.cpg.graph.Node;

public class ManagedKeyEncryption extends AtRestEncryption {

	public ManagedKeyEncryption(String algorithm, String keymanager) {
		super(algorithm, keymanager);
	}
}