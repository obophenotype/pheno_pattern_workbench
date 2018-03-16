package monarch.ontology.phenoworkbench.browser.quickimpact;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vaadin.ui.Grid;

import monarch.ontology.phenoworkbench.analytics.quickimpact.QuickImpact;
import monarch.ontology.phenoworkbench.analytics.pattern.PatternClass;
import monarch.ontology.phenoworkbench.analytics.pattern.Pattern;

public class WeightedPatternGrid extends Grid<WeightedPattern>{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3085573574133131822L;
	Map<PatternClass, WeightedPattern> mapPatternGrid = new HashMap<>();
	List<WeightedPattern> weightedPatterns = new ArrayList<>();
	
	public WeightedPatternGrid(QuickImpact p) {
		for (Pattern pattern : p.getAllPatterns()) {
			if (!pattern.isDefinedclass()) {
				WeightedPattern wp = new WeightedPattern(pattern, p.getImpact(pattern).getIndirectImpact());
				mapPatternGrid.put(pattern, wp);
				weightedPatterns.add(wp);
			}
		}

		setItems(weightedPatterns);
		addColumn(WeightedPattern::getLabel).setCaption("Name");
		addColumn(WeightedPattern::getWeight).setCaption("Impact");
	}

	public boolean containsPattern(PatternClass pc) {
		return mapPatternGrid.containsKey(pc);
	}

	public WeightedPattern getWeightedPattern(PatternClass pc) {
		return mapPatternGrid.get(pc);
	}

	public int indexOf(WeightedPattern wp) {
		return weightedPatterns.indexOf(wp);
	}
}
