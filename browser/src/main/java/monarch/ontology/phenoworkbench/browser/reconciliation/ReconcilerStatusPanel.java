package monarch.ontology.phenoworkbench.browser.reconciliation;

import com.vaadin.ui.*;
import monarch.ontology.phenoworkbench.util.PatternReconciliationCandidate;
import monarch.ontology.phenoworkbench.util.ReconciliationCandidateSet;
import monarch.ontology.phenoworkbench.browser.basic.LabelManager;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class ReconcilerStatusPanel extends VerticalLayout {
    /**
	 * 
	 */
	private static final long serialVersionUID = 567953063492999216L;
	private final ProgressBar pb_grammar = new ProgressBar();
    private final ProgressBar pb_equal = new ProgressBar();
    private final ProgressBar pb_logical = new ProgressBar();
    private final Label l_all = LabelManager.htmlLabel("None");
    private final Label l_grammar = LabelManager.htmlLabel(getColoredPercentString(0.0f));
    private final Label l_equal = LabelManager.htmlLabel(getColoredPercentString(0.0f));
    private final Label l_logical = LabelManager.htmlLabel(getColoredPercentString(0.0f));

    ReconcilerStatusPanel() {
        setMargin(true);
        addComponent(layoutStatusPanel());
    }

    void updateProgress(Collection<PatternReconciliationCandidate> candidates) {
    		l_all.setValue("Reconciliations: "+candidates.size());
        updateProgress(pb_grammar, l_grammar, div(candidates.stream().filter(PatternReconciliationCandidate::isGrammarEquivalent).count(),candidates.size()));
        updateProgress(pb_equal, l_equal, div(candidates.stream().filter(PatternReconciliationCandidate::isSyntacticallyEquivalent).count(),candidates.size()));
        updateProgress(pb_logical, l_logical, div(candidates.stream().filter(PatternReconciliationCandidate::isLogicallyEquivalent).count(),candidates.size()));
    }

    private float div(long count, long all) {
        if(all==0) {
            return 0.0f;
        }
        return (float)count/(float)all;
    }

    private void updateProgress(ProgressBar pb, Label label,float percent ) {
        if(percent>=0&&percent<=1) {
            pb.setValue(percent);
            label.setValue(getColoredPercentString(percent));
        } else {
        		pb.setValue(0.0f);
        		label.setValue(getColoredString("Illegal: "+percent,"red"));
        }
    }

    private VerticalLayout layoutStatusPanel() {
        VerticalLayout vl_status = new VerticalLayout();
        vl_status.setMargin(false);
        vl_status.setSpacing(false);
        vl_status.addComponent(l_all);
        vl_status.addComponent(createProgressWidget("Grammar: ", pb_grammar, l_grammar));
        vl_status.addComponent(createProgressWidget("Syntax: ", pb_equal, l_equal));
        vl_status.addComponent(createProgressWidget("Logical: ", pb_logical, l_logical));
        return vl_status;
    }

    private Component createProgressWidget(String name, ProgressBar bar, Label percent) {
        HorizontalLayout hl_pb = new HorizontalLayout();
        hl_pb.setMargin(false);
        hl_pb.setSpacing(true);
        hl_pb.addComponent(LabelManager.htmlLabel(name));
        hl_pb.addComponent(LabelManager.htmlLabel("<hr />"));
        hl_pb.addComponent(percent);
        VerticalLayout vl_pb = new VerticalLayout();
        vl_pb.addComponent(hl_pb);
        vl_pb.setMargin(false);
        vl_pb.setSpacing(false);
        vl_pb.addComponent(bar);
        bar.setWidth("100%");
        return vl_pb;
    }

    private String getColoredPercentString(float recon) {
        return getColoredString(String.format("%.2f", recon*100), color(recon));
    }
    
    private String getColoredString(String s, String color) {
        return "<div style='color:"+color+";'>"+s+" %</div>";
    }

    private String color(float recon) {
        if(recon<0.1) {
            return "red";
        } else if(recon<0.5) {
            return "orange";
        } else {
            return "green";
        }
    }
}
