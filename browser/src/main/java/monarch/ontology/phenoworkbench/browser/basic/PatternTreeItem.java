package monarch.ontology.phenoworkbench.browser.basic;

import monarch.ontology.phenoworkbench.util.DefinedClass;
import monarch.ontology.phenoworkbench.util.OntologyClass;
import monarch.ontology.phenoworkbench.analytics.pattern.generation.PatternClass;

public class PatternTreeItem {
	private final OntologyClass c;

	PatternTreeItem(OntologyClass c) {
		this.c = c;
	}

	@Override
	public String toString() {
		String s = getPatternClass().getLabel();
		if (getPatternClass() instanceof DefinedClass) {
			if(getPatternClass() instanceof PatternClass) {
				s = "<i>" + s + "</i>";
			} else {
				s = "<b>" + s + "</b>";
			}
		}
		return s;
	}

	public OntologyClass getPatternClass() {
		return c;
	}
}
