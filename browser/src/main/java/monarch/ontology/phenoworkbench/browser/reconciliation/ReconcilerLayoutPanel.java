package monarch.ontology.phenoworkbench.browser.reconciliation;

import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.shared.ui.slider.SliderOrientation;
import com.vaadin.ui.*;

import monarch.ontology.phenoworkbench.analytics.pattern.generation.DefinedClass;
import monarch.ontology.phenoworkbench.analytics.pattern.reconciliation.PatternReconciler;
import monarch.ontology.phenoworkbench.analytics.pattern.reconciliation.PatternReconciliationCandidate;

public class ReconcilerLayoutPanel extends VerticalLayout {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3354993059350574732L;
	private ReconcilerGrid grid;
	Slider sl_jaccard = new Slider("Jackard Similarity",0, 1);
	Slider sl_sbcl = new Slider("Subclass Similarity",0, 1);
	Slider sl_complexity = new Slider("Complexity",0, 1);
	Slider sl_impact = new Slider("OntologyClassImpact",0, 1);
	TextField tf_filter_patterns = new TextField("Filter");
	CheckBox cb_exclude_reconciled = new CheckBox("Exclude Reconciled");
	
	public ReconcilerLayoutPanel(PatternReconciler p) {
		grid = new ReconcilerGrid(p);
		setSizeFull();
		setMargin(false);
		sl_jaccard.setOrientation(SliderOrientation.HORIZONTAL);
		sl_jaccard.setResolution(2);
		sl_jaccard.addValueChangeListener(event -> {
			filter();
		});
		sl_sbcl.setOrientation(SliderOrientation.HORIZONTAL);
		sl_sbcl.setResolution(2);
		sl_sbcl.addValueChangeListener(event -> {
			filter();
		});
		sl_complexity.setOrientation(SliderOrientation.HORIZONTAL);
		sl_complexity.setValue(1.0);
		sl_complexity.setResolution(2);
		sl_complexity.addValueChangeListener(event -> {
			filter();
		});
		sl_impact.setResolution(4);
		sl_impact.addValueChangeListener(event -> {
			filter();
		});

		tf_filter_patterns.addValueChangeListener(v->filter());
		cb_exclude_reconciled.addValueChangeListener(v->filter());

		HorizontalLayout hl_sliders = new HorizontalLayout();
		hl_sliders.setWidth("100%");
		hl_sliders.addComponent(sl_jaccard);
		hl_sliders.addComponent(sl_sbcl);
		hl_sliders.addComponent(sl_complexity);
		hl_sliders.addComponent(sl_impact);
		addComponent(cb_exclude_reconciled);
		addComponent(tf_filter_patterns);
		addComponent(hl_sliders);
		addComponent(grid);
		
	}

	private void filter() {
		String value = tf_filter_patterns.getValue();
		boolean excludeReconciled = cb_exclude_reconciled.getValue();
		float jaccard = sl_jaccard.getValue().floatValue();
		float sbcl = sl_sbcl.getValue().floatValue();
		float complexity = sl_complexity.getValue().floatValue();
		float effect = sl_impact.getValue().floatValue();
		ListDataProvider<PatternReconciliationCandidate> dataProvider = (ListDataProvider<PatternReconciliationCandidate>) grid.getDataProvider();
		dataProvider.setFilter(PatternReconciliationCandidate::getItself, s -> filter(s,value,excludeReconciled,jaccard,sbcl,complexity,effect));
	}

	private boolean filter(PatternReconciliationCandidate s, String value, boolean excludeReconciled, float jaccard, float sbcl, float complexity, float effect) {
		return caseInsensitiveContains(s.getP1(),value)&&minThreshold(s.getJaccardSimiliarity(),jaccard)&&minThreshold(s.getSubclassSimilarity(),sbcl)&&includeReconciled(s.isSyntacticallyEquivalent(),excludeReconciled)&&maxThreshold(s.getReconciliationComplexity(),complexity)&&minThreshold(s.getReconciliationEffect(),effect);
	}

	private boolean maxThreshold(double actual, float threshold) {
		return actual<=threshold;
	}
	
	private boolean minThreshold(double actual, float threshold) {
		return actual>=threshold;
	}


	private boolean caseInsensitiveContains(DefinedClass s, String value) {
		if(value.length()>2) {
	        return s.toString().toLowerCase().contains(value.toLowerCase());

		}
		return true;
	}

	private boolean includeReconciled(boolean s, boolean exclude) {
		if(exclude) {
			return !s;
		}
		return true;
	}

}
