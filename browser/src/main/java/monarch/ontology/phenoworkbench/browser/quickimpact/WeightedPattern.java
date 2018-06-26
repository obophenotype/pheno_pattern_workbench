package monarch.ontology.phenoworkbench.browser.quickimpact;

import monarch.ontology.phenoworkbench.analytics.pattern.generation.DefinedClass;

public class WeightedPattern {
	
	private final DefinedClass p;
	private int weight = 0;


	private int instancecount = 0;
	
	public WeightedPattern(DefinedClass p) {
		this.p = p;
	}
	
	public DefinedClass getPattern() {
		return p;
	}

	public int getWeight() {
		return weight;
	}
	
	public int getInstancecount() {
		return instancecount;
	}

	public void setInstancecount(int instancecount) {
		this.instancecount = instancecount;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}
	
	public String getLabel() {
		return p.getLabel();
	}
	
	@Override
	public String toString() {
		return p.getLabel();
	}

}
