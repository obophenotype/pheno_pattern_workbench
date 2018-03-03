package monarch.ontology.phenoworkbench.browser;

import monarch.ontology.phenoworkbench.browser.analytics.Pattern;
import monarch.ontology.phenoworkbench.browser.analytics.PatternClass;

public class PatternTreeItem {
	private final PatternClass c;

	public PatternTreeItem(PatternClass c) {
		this.c = c;
	}

	@Override
	public String toString() {
		String s = getPatternClass().toString();
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
