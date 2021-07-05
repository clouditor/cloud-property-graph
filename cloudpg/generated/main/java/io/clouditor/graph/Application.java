package io.clouditor.graph;

import de.fraunhofer.aisec.cpg.graph.Node;
import java.util.List;
import de.fraunhofer.aisec.cpg.graph.declarations.TranslationUnitDeclaration;

public class Application extends Node {

	protected List<Functionality> functionalitys;
	protected List<Compute> runsOn;
	protected List<TranslationUnitDeclaration> translationUnits;
	protected String programmingLanguage;

	public Application(List<Functionality> functionalitys, List<Compute> runsOn,
			List<TranslationUnitDeclaration> translationUnits, String programmingLanguage) {
		setFunctionalitys(functionalitys);
		setRunsOn(runsOn);
		setTranslationUnits(translationUnits);
		setProgrammingLanguage(programmingLanguage);
	}

	public List<Functionality> getFunctionalitys() {
		return functionalitys;
	}

	public void setFunctionalitys(List<Functionality> functionalitys) {
		this.functionalitys = functionalitys;
	}

	public List<Compute> getRunsOn() {
		return runsOn;
	}

	public void setRunsOn(List<Compute> runsOn) {
		this.runsOn = runsOn;
	}

	public List<TranslationUnitDeclaration> getTranslationUnits() {
		return translationUnits;
	}

	public void setTranslationUnits(List<TranslationUnitDeclaration> translationUnits) {
		this.translationUnits = translationUnits;
	}

	public String getProgrammingLanguage() {
		return programmingLanguage;
	}

	public void setProgrammingLanguage(String programmingLanguage) {
		this.programmingLanguage = programmingLanguage;
	}
}