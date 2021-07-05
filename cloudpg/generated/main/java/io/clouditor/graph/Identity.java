package io.clouditor.graph;

import de.fraunhofer.aisec.cpg.graph.Node;
import java.util.Map;
import org.neo4j.ogm.annotation.Transient;

public class Identity extends IdentityManagement {

	protected PasswordBasedAuthentication passwordBasedAuthentication;
	protected OTPBasedAuthentication oTPBasedAuthentication;

	public Identity(PasswordBasedAuthentication passwordBasedAuthentication,
			OTPBasedAuthentication oTPBasedAuthentication, Authorization authorization, Authenticity authenticity,
			GeoLocation geoLocation, Map<String, String> labels) {
		super(authorization, authenticity, geoLocation, labels);
		setPasswordBasedAuthentication(passwordBasedAuthentication);
		setOTPBasedAuthentication(oTPBasedAuthentication);
	}

	public PasswordBasedAuthentication getPasswordBasedAuthentication() {
		return passwordBasedAuthentication;
	}

	public void setPasswordBasedAuthentication(PasswordBasedAuthentication passwordBasedAuthentication) {
		this.passwordBasedAuthentication = passwordBasedAuthentication;
	}

	public OTPBasedAuthentication getOTPBasedAuthentication() {
		return oTPBasedAuthentication;
	}

	public void setOTPBasedAuthentication(OTPBasedAuthentication oTPBasedAuthentication) {
		this.oTPBasedAuthentication = oTPBasedAuthentication;
	}
}