package monarch.ontology.phenoworkbench.browser.reconciliation;

import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.server.Page;
import com.vaadin.server.ResourceReference;
import com.vaadin.server.StreamResource;
import com.vaadin.shared.ui.slider.SliderOrientation;
import com.vaadin.ui.*;

import monarch.ontology.phenoworkbench.analytics.pattern.reconciliation.PatternReconciler;
import monarch.ontology.phenoworkbench.browser.basic.IOUtils;
import monarch.ontology.phenoworkbench.util.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MappingGridPanel extends VerticalLayout {
    /**
	 * 
	 */
	private static final long serialVersionUID = 3225339060486944225L;
	
	private final MappingGrid grid;
	private final Set<PatternReconciliationCandidate> currentCandidates = new HashSet<>(); // TODO keeping track of those should not be necessary
    private final Slider sl_jaccard = new Slider("Similarity", 0, 1);
    private final Slider sl_complexity = new Slider("Complexity", 0, 1);
    private final Slider sl_impact = new Slider("Impact", 0, 1);
    private final TextField tf_filter_patterns = new TextField("Filter");

    private final CheckBox cb_exclude_reconciled = new CheckBox("Same grammar");
    private final CheckBox cb_exclude_equal = new CheckBox("Equal");
    private final CheckBox cb_exclude_equivalent = new CheckBox("Equivalent");
    private final CheckBox cb_both_def = new CheckBox("Both defined");
    private final CheckBox cb_exclude_p1_sub_p2 = new CheckBox("P1 SC: P2");
    private final CheckBox cb_exclude_p2_sub_p1 = new CheckBox("P2 SC: P1");

    private final ReconcilerStatusPanel reconcilerStatusPanel = new ReconcilerStatusPanel();
    private final boolean filterKB;
    private final KB kb = KB.getInstance();
    private final Button bt_downloadValidated = new Button("Export");
    private Upload upload;


    public MappingGridPanel(PatternReconciler app,ReconciliationCandidateSet p, ReconciliationCandidateFunction infoListener, ReconciliationCandidateFunction multiFunctionListener, ReconciliationCandidateFunction removeListener, boolean excludekb) {
        grid = new MappingGrid(p, false,infoListener,multiFunctionListener,removeListener);
        TSVFileUploader receiver = new TSVFileUploader(p) {

            @Override
            protected void loadMappings(List<IRIMapping> list) {
                p.addCandidates(app.preparePatternMap(list));
            }
        };
        upload = new Upload(null, receiver);
        upload.addSucceededListener(receiver);
        this.filterKB = excludekb;
        setMargin(false);
        prepareFilterControls(p.getMaxReconciliationImpact());
        addComponent(layoutGrid(layoutPreferencePanel()));
        HorizontalLayout hl_bt = new HorizontalLayout();
        hl_bt.addComponent(bt_downloadValidated);
        hl_bt.addComponent(upload);
        addComponent(hl_bt);
        reconcilerStatusPanel.updateProgress(p.items());
        p.addCandidateChangeListener(()->{grid.restrict(p); filter(null);});
        kb.getMappingBlacklist().addCandidateChangeListener(()->{filter(null);});
        kb.getMappings().addCandidateChangeListener(()->{filter(null);});
        bt_downloadValidated.addClickListener(click->downloadValidatedMappings(p.items()));
        filter(null);
    }

    private void downloadValidatedMappings(Collection<PatternReconciliationCandidate> items) {
        String tsv = "";
        for(PatternReconciliationCandidate c:items) {
            //HP:0100300	Desmin bodies	MP:0003084	abnormal skeletal muscle fiber morphology	0.8205128205	1
            //I add similarity here twice because the original file has two different similarity measures (see upheno/mappings)
            tsv+=""+c.getP1().getIri().toString()+"	"+c.getP1().getLabel()+"	"+c.getP2().getIri().toString()+"	"+c.getP2().getLabel()+"	"+c.getOtherMetrics().get("jaccard")+"	"+c.getOtherMetrics().get("sbcl")+"\n";
        }
        downloadExportFile(tsv.getBytes(),"validated_mapping_"+ StringUtils.getCurrentDateString()+".tsv");
    }

    private void downloadExportFile(byte[] toDownload, String filename) {
        StreamResource resource = IOUtils.getStreamResource(toDownload,filename);
        ResourceReference ref = new ResourceReference(resource, this, "download");
        this.setResource("download", resource);
        Page.getCurrent().open(ref.getURL(), null);
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
        
        sl_complexity.setOrientation(SliderOrientation.HORIZONTAL);
        sl_complexity.setValue(1.0);
        sl_complexity.setResolution(2);
    
        sl_impact.setMin(0);
        sl_impact.setMax(MAX_IMPACT);
        
        sl_jaccard.addValueChangeListener(this::filter);
        sl_complexity.addValueChangeListener(this::filter);
        sl_impact.addValueChangeListener(this::filter);

        //cb_both_def.setValue(true);
        tf_filter_patterns.addValueChangeListener(this::filter);
        cb_exclude_reconciled.addValueChangeListener(this::filter);
        cb_exclude_equal.addValueChangeListener(this::filter);
        cb_exclude_equivalent.addValueChangeListener(this::filter);
        cb_both_def.addValueChangeListener(this::filter);
        cb_exclude_p1_sub_p2.addValueChangeListener(this::filter);
        cb_exclude_p2_sub_p1.addValueChangeListener(this::filter);
    }
	
	
	private void filter(Object o) {
        currentCandidates.clear();
        String value = tf_filter_patterns.getValue();
        boolean excludeReconciled = !cb_exclude_reconciled.getValue();
        boolean excludeEqual = !cb_exclude_equal.getValue();
        boolean excludeEquivalentValue = !cb_exclude_equivalent.getValue();
        boolean excludeIfNotBothDef = cb_both_def.getValue();
        boolean excludep1p2 = !cb_exclude_p1_sub_p2.getValue();
        boolean excludep2p1 = !cb_exclude_p2_sub_p1.getValue();
        float jaccard = sl_jaccard.getValue().floatValue();
        float complexity = sl_complexity.getValue().floatValue();
        float effect = sl_impact.getValue().floatValue();
        ListDataProvider<PatternReconciliationCandidate> dataProvider = (ListDataProvider<PatternReconciliationCandidate>) grid
                .getDataProvider();
        dataProvider.getItems().forEach(s -> filterOut(s, value, excludeReconciled, excludeEqual, jaccard, complexity, effect,excludep1p2,excludep2p1,excludeEquivalentValue,excludeIfNotBothDef));
        dataProvider.setFilter(PatternReconciliationCandidate::getItself, currentCandidates::contains);
        reconcilerStatusPanel.updateProgress(currentCandidates); //todo Should not be necessary. Why cant I get a handle on the currently filtered items?
    }

    private boolean filterOut(PatternReconciliationCandidate s, String value, boolean excludeReconciled,
                              boolean excludeEqual, float jaccard, float complexity, float effect, boolean excludep1p2, boolean excludep2p1, boolean excludeEquivalentValue, boolean excludeIfNotBothDef) {
        boolean filter = caseInsensitiveContains(s.stringForSearch(), value)
                && minThreshold(s.getSimiliarity(), jaccard)
                && and(s.isGrammarEquivalent(), excludeReconciled)
                && and(s.isSyntacticallyEquivalent(), excludeEqual)
                && and(s.isP1SubclassOfP2(), excludep1p2)
                && and(s.isP2SubclassOfP1(), excludep2p1)
                && and(s.isLogicallyEquivalent(), excludeEquivalentValue)
                && and(!s.isBothDefinitionSet(), excludeIfNotBothDef)
                && maxThreshold(s.getReconciliationComplexity(), complexity)
                && notInKB(s)
                && notBlacklisted(s)
                && minThreshold(s.getReconciliationEffect(), effect);
        if(filter) {
            currentCandidates.add(s);
        }
        return filter;
    }

    private boolean notBlacklisted(PatternReconciliationCandidate s) {
        if(filterKB) {
            return !kb.isBlacklistedMapping(s);
        }
        return true;
    }

    private boolean notInKB(PatternReconciliationCandidate s) {
		if(filterKB) {
			return !kb.isValidatedMapping(s);
		}
		return true;
	}

	private boolean maxThreshold(double actual, float threshold) {
        return actual <= threshold;
    }

    private boolean minThreshold(double actual, float threshold) {
        return threshold > 0 ? actual >= threshold : true;
    }

    private boolean caseInsensitiveContains(String s, String value) {
        if (value.length() > 2) {
            return s.toLowerCase().contains(value.toLowerCase());

        }
        return true;
    }

    private boolean and(boolean b1, boolean b2) {
        return !b1 || !b2;
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
        vl_excludecbs.addComponent(cb_exclude_p1_sub_p2);
        vl_excludecbs.addComponent(cb_exclude_p2_sub_p1);
        vl_excludecbs.addComponent(cb_exclude_equivalent);
        vl_excludecbs.addComponent(cb_both_def);
        return vl_excludecbs;
    }

    private HorizontalLayout layoutSlidersPanel() {
        HorizontalLayout hl_sliders = new HorizontalLayout();
        hl_sliders.setWidth("100%");
        hl_sliders.addComponent(sl_jaccard);
        hl_sliders.addComponent(sl_complexity);
        hl_sliders.addComponent(sl_impact);
        return hl_sliders;
    }



}
