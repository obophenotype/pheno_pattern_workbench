package monarch.ontology.phenoworkbench.browser.reconciliation;

import java.util.List;

import com.vaadin.data.HasValue;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.shared.ui.slider.SliderOrientation;
import com.vaadin.ui.*;

import monarch.ontology.phenoworkbench.analytics.pattern.generation.DefinedClass;
import monarch.ontology.phenoworkbench.analytics.pattern.reconciliation.PatternReconciler;
import monarch.ontology.phenoworkbench.analytics.pattern.reconciliation.PatternReconciliationCandidate;
import monarch.ontology.phenoworkbench.browser.basic.LabelManager;

class ReconcilerLayoutPanel extends VerticalLayout {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3354993059350574732L;
	private final ReconcilerGrid grid;
	private final Slider sl_jaccard = new Slider("Jackard Similarity", 0, 1);
	private final Slider sl_sbcl = new Slider("Subclass Similarity", 0, 1);
	private final Slider sl_complexity = new Slider("Complexity", 0, 1);
	private final Slider sl_impact = new Slider("sImpact", 0, 1);
	private final TextField tf_filter_patterns = new TextField("Filter");
	private final CheckBox cb_exclude_reconciled = new CheckBox("Exclude Reconciled");
	private final CheckBox cb_exclude_equal = new CheckBox("Exclude Equal");
	private final ProgressBar pb_grammar = new ProgressBar();
	private final ProgressBar pb_equal = new ProgressBar();
	private final Label l_grammar = new Label("0 %");
	private final Label l_equal = new Label("0 %");
	private final VerticalLayout vl_reconciliation = new VerticalLayout();


	ReconcilerLayoutPanel(PatternReconciler p) {
		grid = new ReconcilerGrid(p,vl_reconciliation);
		setSizeFull();
		setMargin(false);
		prepareFilterControls();
		HorizontalLayout hl_preferences = layoutPreferencePanel();
		List<PatternReconciliationCandidate> stream = p.getAllPatternReconciliations();
		long ct_all  = stream.size();
		updateProgress(pb_grammar,l_grammar,stream.stream().filter(PatternReconciliationCandidate::isGrammarEquivalent).count(),ct_all);
		updateProgress(pb_equal,l_equal,stream.stream().filter(PatternReconciliationCandidate::isSyntacticallyEquivalent).count(),ct_all);
		addComponent(hl_preferences);
		addComponent(grid);
		addComponent(vl_reconciliation);
	}

	private void updateProgress(ProgressBar pb, Label label,
			long progress, long l) {
		float percent = ((float)progress/(float)l);
		pb.setValue(percent);
		label.setValue(100*percent+" %");
	}

	private void prepareFilterControls() {
		sl_jaccard.setOrientation(SliderOrientation.HORIZONTAL);
		sl_jaccard.setResolution(2);
		sl_jaccard.addValueChangeListener(this::filter);
		sl_sbcl.setOrientation(SliderOrientation.HORIZONTAL);
		sl_sbcl.setResolution(2);
		sl_sbcl.addValueChangeListener(this::filter);
		sl_complexity.setOrientation(SliderOrientation.HORIZONTAL);
		sl_complexity.setValue(1.0);
		sl_complexity.setResolution(2);
		sl_complexity.addValueChangeListener(this::filter);
		sl_impact.setResolution(4);
		sl_impact.addValueChangeListener(this::filter);

		tf_filter_patterns.addValueChangeListener(this::filter);
		cb_exclude_reconciled.addValueChangeListener(this::filter);
		cb_exclude_equal.addValueChangeListener(this::filter);
	}

	private HorizontalLayout layoutPreferencePanel() {
		HorizontalLayout hl_preferences = new HorizontalLayout();
		hl_preferences.setMargin(false);
		hl_preferences.addComponent(layoutOptionsPanel());
		hl_preferences.addComponent(layoutStatusPanel());
		return hl_preferences;
	}

	private VerticalLayout layoutOptionsPanel() {
		VerticalLayout vl_options = new VerticalLayout();
		vl_options.setMargin(false);
		vl_options.setCaption("Options");
		vl_options.addComponent(layoutControlPanel());
		vl_options.addComponent(layoutSlidersPanel());
		return vl_options;
	}

	private HorizontalLayout layoutControlPanel() {
		HorizontalLayout hl_control = new HorizontalLayout();
		hl_control.setMargin(false);
		hl_control.addComponent(tf_filter_patterns);
		hl_control.addComponent(layoutExcludeCheckboxes());
		return hl_control;
	}

	private VerticalLayout layoutExcludeCheckboxes() {
		VerticalLayout vl_excludecbs = new VerticalLayout();
		vl_excludecbs.setMargin(false);
		vl_excludecbs.addComponent(cb_exclude_reconciled);
		vl_excludecbs.addComponent(cb_exclude_equal);
		return vl_excludecbs;
	}

	private HorizontalLayout layoutSlidersPanel() {
		HorizontalLayout hl_sliders = new HorizontalLayout();
		hl_sliders.setWidth("100%");
		hl_sliders.addComponent(sl_jaccard);
		hl_sliders.addComponent(sl_sbcl);
		hl_sliders.addComponent(sl_complexity);
		hl_sliders.addComponent(sl_impact);
		return hl_sliders;
	}

	private VerticalLayout layoutStatusPanel() {
		VerticalLayout vl_status = new VerticalLayout();
		vl_status.setCaption("Status");
		vl_status.setMargin(false);
		vl_status.addComponent(createProgressWidget("Grammar Reconciliation: ", pb_grammar, l_grammar));
		vl_status.addComponent(createProgressWidget("Equality Reconciliation: ", pb_equal, l_equal));
		return vl_status;
	}

	private Component createProgressWidget(String name, ProgressBar bar, Label percent) {
		HorizontalLayout hl_pb = new HorizontalLayout();
		hl_pb.setMargin(false);
		hl_pb.addComponent(LabelManager.htmlLabel(name));
		hl_pb.addComponent(percent);
		VerticalLayout vl_pb = new VerticalLayout();
		vl_pb.addComponent(hl_pb);
		vl_pb.setMargin(false);
		vl_pb.addComponent(bar);
		return vl_pb;
	}

	private void filter(HasValue.ValueChangeEvent o) {
		String value = tf_filter_patterns.getValue();
		boolean excludeReconciled = cb_exclude_reconciled.getValue();
		boolean excludeEqual = cb_exclude_equal.getValue();
		float jaccard = sl_jaccard.getValue().floatValue();
		float sbcl = sl_sbcl.getValue().floatValue();
		float complexity = sl_complexity.getValue().floatValue();
		float effect = sl_impact.getValue().floatValue();
		ListDataProvider<PatternReconciliationCandidate> dataProvider = (ListDataProvider<PatternReconciliationCandidate>) grid
				.getDataProvider();
		dataProvider.setFilter(PatternReconciliationCandidate::getItself,
				s -> filterOut(s, value, excludeReconciled, excludeEqual, jaccard, sbcl, complexity, effect));
	}

	private boolean filterOut(PatternReconciliationCandidate s, String value, boolean excludeReconciled,
			boolean excludeEqual, float jaccard, float sbcl, float complexity, float effect) {
		return caseInsensitiveContains(s.getP1(), value) 
				&& minThreshold(s.getJaccardSimiliarity(), jaccard)
				&& minThreshold(s.getSubclassSimilarity(), sbcl)
				&& includeReconciled(s.isGrammarEquivalent(), excludeReconciled)
				&& includeEqual(s.isSyntacticallyEquivalent(), excludeEqual)
				&& maxThreshold(s.getReconciliationComplexity(), complexity)
				&& minThreshold(s.getReconciliationEffect(), effect);
	}

	private boolean maxThreshold(double actual, float threshold) {
		return actual <= threshold;
	}

	private boolean minThreshold(double actual, float threshold) {
		return actual >= threshold;
	}

	private boolean caseInsensitiveContains(DefinedClass s, String value) {
		if (value.length() > 2) {
			return s.toString().toLowerCase().contains(value.toLowerCase());

		}
		return true;
	}

	private boolean includeReconciled(boolean s, boolean exclude) {
		return !exclude || !s;
	}

	private boolean includeEqual(boolean s, boolean exclude) {
		return !exclude || !s;
	}

}
