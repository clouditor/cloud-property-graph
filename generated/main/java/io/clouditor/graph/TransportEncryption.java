package io.clouditor.graph;

import de.fraunhofer.aisec.cpg.graph.Node;

public class TransportEncryption extends Confidentiality {

	protected String algorithm;
	protected String tlsVersion;
	protected boolean enforced;
	protected boolean enabled;

	public TransportEncryption(String algorithm, String tlsVersion, boolean enforced, boolean enabled) {
		super();
		setAlgorithm(algorithm);
		setTlsVersion(tlsVersion);
		setEnforced(enforced);
		setEnabled(enabled);
	}

	public String getAlgorithm() {
		return algorithm;
	}

	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}

	public String getTlsVersion() {
		return tlsVersion;
	}

	public void setTlsVersion(String tlsVersion) {
		this.tlsVersion = tlsVersion;
	}

	public boolean isEnforced() {
		return enforced;
	}

	public void setEnforced(boolean enforced) {
		this.enforced = enforced;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}