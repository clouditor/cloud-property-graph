package io.clouditor.graph;

import de.fraunhofer.aisec.cpg.graph.Node;

public class CustomerKeyEncryption extends AtRestEncryption {

	protected String keyUrl;

	public CustomerKeyEncryption(String keyUrl, String algorithm, String keymanager) {
		super(algorithm, keymanager);
		setKeyUrl(keyUrl);
	}

	public String getKeyUrl() {
		return keyUrl;
	}

	public void setKeyUrl(String keyUrl) {
		this.keyUrl = keyUrl;
	}
}