package monarch.ontology.phenoworkbench.browser.reconciliation;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.VerticalLayout;

import monarch.ontology.phenoworkbench.analytics.pattern.reconciliation.ReconciliationCandidateSet;

public class ReconciliationTreeSummaryInfoBox extends VerticalLayout {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2254864769616615100L;
	private final ReconcilerStatusPanel rsp = new ReconcilerStatusPanel();

	public ReconciliationTreeSummaryInfoBox() {
		setWidth("100%");
		setHeightUndefined();
		setMargin(false);
		setSpacing(true);
		addComponent(rsp);
		setComponentAlignment(rsp, Alignment.TOP_CENTER);
	}
	
	public void update(ReconciliationCandidateSet rcs) {
		rsp.updateProgress(rcs);
	}

}
