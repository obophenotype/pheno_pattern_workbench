package monarch.ontology.phenoworkbench.browser.reconciliation;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Notification;
import com.vaadin.ui.renderers.HtmlRenderer;
import com.vaadin.ui.renderers.NumberRenderer;

import monarch.ontology.phenoworkbench.util.PatternReconciliationCandidate;
import monarch.ontology.phenoworkbench.util.ReconciliationCandidateSet;
import monarch.ontology.phenoworkbench.browser.basic.HTMLRenderUtils;

class MappingGrid extends Grid<PatternReconciliationCandidate> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1503748580420486580L;
	private final List<PatternReconciliationCandidate> reconciliations = new ArrayList<>();

	MappingGrid(ReconciliationCandidateSet p, boolean small) {
		this(p, small,click-> Notification.show("No function!"),c-> Notification.show("No function!"),c-> Notification.show("No function!"));
	}

	MappingGrid(ReconciliationCandidateSet cset, boolean small, ReconciliationCandidateFunction infoListener, ReconciliationCandidateFunction multiFunctionListener, ReconciliationCandidateFunction removeListener) {
		setWidth("100%");
		setHeight("100%");
		Column<PatternReconciliationCandidate, String> p1 = addColumn(
				rec -> HTMLRenderUtils.renderOntologyClass(rec.getP1(), 50), new HtmlRenderer()).setCaption("Class 1");
		Column<PatternReconciliationCandidate, String> p2 = addColumn(
				rec -> HTMLRenderUtils.renderOntologyClass(rec.getP2(), 50), new HtmlRenderer()).setCaption("Class 2");
		if (!small) {
			Column<PatternReconciliationCandidate, Double> c_compl = addColumn(
					PatternReconciliationCandidate::getReconciliationComplexity,
					new NumberRenderer(new DecimalFormat("#.##"))).setCaption("Complexity");
			Column<PatternReconciliationCandidate, Long> c_imp = addColumn(
					PatternReconciliationCandidate::getReconciliationEffect).setCaption("Impact");
			Column<PatternReconciliationCandidate, Double> c_jack = addColumn(
					PatternReconciliationCandidate::getSimiliarity,
					new NumberRenderer(new DecimalFormat("#.##"))).setCaption("Similarity");
			c_compl.setExpandRatio(1);
			c_imp.setExpandRatio(1);
			c_jack.setExpandRatio(1);
			setItems(cset.items());
		}

		Column<PatternReconciliationCandidate, Button> c_bt = addComponentColumn(recon -> {
			Button button = new Button("?");
			button.addClickListener(click -> infoListener.handle(recon));
			return button;
		});

		Column<PatternReconciliationCandidate, Button> c_bt_candidate = addComponentColumn(recon -> {
			Button button = new Button("+");
			button.addClickListener(click -> multiFunctionListener.handle(recon));
			return button;
		});

		Column<PatternReconciliationCandidate, Button> c_bt_remove = addComponentColumn(recon -> {
			Button button = new Button("X");
			button.addClickListener(click -> removeListener.handle(recon));
			return button;
		});

		p1.setExpandRatio(4);
		p2.setExpandRatio(4);

		c_bt.setExpandRatio(1);
		c_bt_candidate.setExpandRatio(1);
		c_bt_remove.setExpandRatio(1);

		setRowHeight(120);
		System.out.println("Done Creating Grid");
	}

	public void setItems(Collection<PatternReconciliationCandidate> p) {
		System.out.println("Get Reconciliations");
		reconciliations.clear();
		reconciliations.addAll(p);
		System.out.println("Set Reconciliations: " + reconciliations.size());
		super.setItems(reconciliations);
	}

	public void restrict(ReconciliationCandidateSet candidateSet) {
		setItems(candidateSet.items());
	}
}
