package monarch.ontology.phenoworkbench.browser.reconciliation;

import java.util.List;

import com.vaadin.data.HasValue;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.shared.Registration;
import com.vaadin.shared.ui.slider.SliderOrientation;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.Slider;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import monarch.ontology.phenoworkbench.analytics.pattern.generation.DefinedClass;
import monarch.ontology.phenoworkbench.analytics.pattern.reconciliation.PatternReconciler;
import monarch.ontology.phenoworkbench.analytics.pattern.reconciliation.PatternReconciliationCandidate;
import monarch.ontology.phenoworkbench.analytics.pattern.reconciliation.ReconciliationCandidateSet;
import monarch.ontology.phenoworkbench.browser.basic.LabelManager;

public class ReconciliationGridPanel extends VerticalLayout {
    /**
	 * 
	 */
	private static final long serialVersionUID = 3225339060486944225L;
	
	private final ReconcilerGrid grid;
    private final Slider sl_jaccard = new Slider("Jackard Similarity", 0, 1);
    private final Slider sl_sbcl = new Slider("Subclass Similarity", 0, 1);
    private final Slider sl_complexity = new Slider("Complexity", 0, 1);
    private final Slider sl_impact = new Slider("sImpact", 0, 1);
    private final TextField tf_filter_patterns = new TextField("Filter");
    private final CheckBox cb_exclude_reconciled = new CheckBox("Exclude Reconciled");
    private final CheckBox cb_exclude_equal = new CheckBox("Exclude Equal");
    private final ReconcilerStatusPanel reconcilerStatusPanel = new ReconcilerStatusPanel();


    ReconciliationGridPanel(PatternReconciler p,VerticalLayout vl_reconciliation) {
        grid = new ReconcilerGrid(p, vl_reconciliation);
        setMargin(false);
        prepareFilterControls(p.getMaxReconciliationImpact());
        addComponent(layoutGrid(layoutPreferencePanel()));
        reconcilerStatusPanel.updateProgress(p.getAllPatternReconciliations());
    }
	
	private VerticalLayout layoutGrid(HorizontalLayout hl_preferences) {
        VerticalLayout vl_grid = new VerticalLayout();
        vl_grid.setWidth("100%");
        vl_grid.setMargin(false);
        vl_grid.addComponent(hl_preferences);
        vl_grid.addComponent(grid);
        return vl_grid;
    }
	
	private void prepareFilterControls(long MAX_IMPACT) {
        sl_jaccard.setOrientation(SliderOrientation.HORIZONTAL);
        sl_jaccard.setResolution(2);
       
        sl_sbcl.setOrientation(SliderOrientation.HORIZONTAL);
        sl_sbcl.setResolution(2);
        
        sl_complexity.setOrientation(SliderOrientation.HORIZONTAL);
        sl_complexity.setValue(1.0);
        sl_complexity.setResolution(2);
    
        sl_impact.setMin(0);
        sl_impact.setMax(MAX_IMPACT);
        
        sl_jaccard.addValueChangeListener(this::filter);
        sl_sbcl.addValueChangeListener(this::filter);
        sl_complexity.addValueChangeListener(this::filter);
        sl_impact.addValueChangeListener(this::filter);
        
        tf_filter_patterns.addValueChangeListener(this::filter);
        cb_exclude_reconciled.addValueChangeListener(this::filter);
        cb_exclude_equal.addValueChangeListener(this::filter);
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
    
    private HorizontalLayout layoutPreferencePanel() {
        HorizontalLayout hl_preferences = new HorizontalLayout();
        hl_preferences.setMargin(false);
        hl_preferences.addComponent(layoutOptionsPanel());
        hl_preferences.addComponent(reconcilerStatusPanel);
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



}
