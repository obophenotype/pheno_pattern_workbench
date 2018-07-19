package monarch.ontology.phenoworkbench.browser.quickimpact;

import java.util.*;

import com.vaadin.ui.Grid;

import monarch.ontology.phenoworkbench.util.PatternClass;
import monarch.ontology.phenoworkbench.analytics.pattern.impact.OntologyClassImpact;
import monarch.ontology.phenoworkbench.analytics.quickimpact.QuickImpact;

public class WeightedPatternGrid extends Grid<WeightedPattern>{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3085573574133131822L;
	private final Map<PatternClass, WeightedPattern> mapPatternGrid = new HashMap<>();
	private final List<WeightedPattern> weightedPatterns = new ArrayList<>();
	
	WeightedPatternGrid(QuickImpact p) {
		for (PatternClass definedClass : p.getPatternsAmongDefinedClasses()) {
				WeightedPattern wp = new WeightedPattern(definedClass);
				wp.setWeight(p.getImpact(definedClass).map(OntologyClassImpact::getIndirectImpact).orElse(0));
				wp.setInstancecount(p.getInstanceCount(wp.getPattern().getGrammar()));
				mapPatternGrid.put(definedClass, wp);
				weightedPatterns.add(wp);
		}

		setItems(weightedPatterns);
		addColumn(WeightedPattern::getLabel).setCaption("Name");
		addColumn(WeightedPattern::getWeight).setCaption("Subclasses");
		addColumn(WeightedPattern::getInstancecount).setCaption("Instances");
	}

	public boolean containsPattern(PatternClass pc) {
		return mapPatternGrid.containsKey(pc);
	}

	public Optional<WeightedPattern> getWeightedPattern(PatternClass pc) {
		return Optional.ofNullable(mapPatternGrid.get(pc));
	}

	public int indexOf(WeightedPattern wp) {
		return weightedPatterns.indexOf(wp);
	}
}
