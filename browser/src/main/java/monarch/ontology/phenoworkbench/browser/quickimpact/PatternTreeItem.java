package monarch.ontology.phenoworkbench.browser.quickimpact;

import monarch.ontology.phenoworkbench.analytics.pattern.Pattern;
import monarch.ontology.phenoworkbench.analytics.pattern.PatternClass;

public class PatternTreeItem {
	private final PatternClass c;

	public PatternTreeItem(PatternClass c) {
		this.c = c;
	}

	@Override
	public String toString() {
		String s = getPatternClass().getLabel();
		if (getPatternClass() instanceof Pattern) {
			if(((Pattern)c).isDefinedclass()) {
				s = "<i>" + s + "</i>";
			} else {
				s = "<b>" + s + "</b>";
			}
		}
		return s;
	}

	public PatternClass getPatternClass() {
		return c;
	}
}
