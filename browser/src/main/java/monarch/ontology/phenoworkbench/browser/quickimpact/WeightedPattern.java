package monarch.ontology.phenoworkbench.browser.quickimpact;

import monarch.ontology.phenoworkbench.analytics.pattern.generation.DefinedClass;

public class WeightedPattern {
	
	private final DefinedClass p;
	private final int weight;
	
	public WeightedPattern(DefinedClass p, int weight) {
		this.p = p;
		this.weight = weight;
	}
	
	public DefinedClass getPattern() {
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
