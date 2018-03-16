package monarch.ontology.phenoworkbench.browser.quickimpact;

import monarch.ontology.phenoworkbench.analytics.pattern.Pattern;

public class WeightedPattern {
	
	private final Pattern p;
	private final int weight;
	
	public WeightedPattern(Pattern p, int weight) {
		this.p = p;
		this.weight = weight;
	}
	
	public Pattern getPattern() {
		return p;
	}

	public int getWeight() {
		return weight;
	}
	
	public String getLabel() {
		return p.getLabel();
	}
	
	@Override
	public String toString() {
		return p.getLabel();
	}

}
