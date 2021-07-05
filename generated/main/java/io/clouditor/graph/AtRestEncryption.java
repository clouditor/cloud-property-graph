package io.clouditor.graph;

import de.fraunhofer.aisec.cpg.graph.Node;

public class AtRestEncryption extends Confidentiality {

	protected String algorithm;
	protected String keymanager;

	public AtRestEncryption(String algorithm, String keymanager) {
		super();
		setAlgorithm(algorithm);
		setKeymanager(keymanager);
	}

	public String getAlgorithm() {
		return algorithm;
	}

	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}

	public String getKeymanager() {
		return keymanager;
	}

	public void setKeymanager(String keymanager) {
		this.keymanager = keymanager;
	}
}