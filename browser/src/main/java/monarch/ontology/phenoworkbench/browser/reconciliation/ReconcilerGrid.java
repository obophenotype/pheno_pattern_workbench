package monarch.ontology.phenoworkbench.browser.reconciliation;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.renderers.HtmlRenderer;
import com.vaadin.ui.renderers.NumberRenderer;

import monarch.ontology.phenoworkbench.analytics.pattern.reconciliation.PatternReconciler;
import monarch.ontology.phenoworkbench.analytics.pattern.reconciliation.PatternReconciliationCandidate;
import monarch.ontology.phenoworkbench.analytics.pattern.reconciliation.ReconciliationCandidateSet;
import monarch.ontology.phenoworkbench.browser.basic.HTMLRenderUtils;

class ReconcilerGrid extends Grid<PatternReconciliationCandidate> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1503748580420486580L;
	private final List<PatternReconciliationCandidate> reconciliations = new ArrayList<>();

	public ReconcilerGrid(PatternReconciler p, VerticalLayout vl_reconcile, boolean small) {
		setWidth("100%");
		setHeight("100%");
		Column<PatternReconciliationCandidate, String> p1 = addColumn(
				rec -> HTMLRenderUtils.renderDefinedClass(rec.getP1(), 50), new HtmlRenderer()).setCaption("Class 1");
		Column<PatternReconciliationCandidate, String> p2 = addColumn(
				rec -> HTMLRenderUtils.renderDefinedClass(rec.getP2(), 50), new HtmlRenderer()).setCaption("Class 2");
		if (!small) {
			Column<PatternReconciliationCandidate, Double> c_compl = addColumn(
					PatternReconciliationCandidate::getReconciliationComplexity,
					new NumberRenderer(new DecimalFormat("#.##"))).setCaption("Complexity");
			Column<PatternReconciliationCandidate, Long> c_imp = addColumn(
					PatternReconciliationCandidate::getReconciliationEffect).setCaption("Impact");
			Column<PatternReconciliationCandidate, Double> c_jack = addColumn(
					PatternReconciliationCandidate::getJaccardSimiliarity,
					new NumberRenderer(new DecimalFormat("#.##"))).setCaption("Jackard");
			Column<PatternReconciliationCandidate, Double> c_subcl = addColumn(
					PatternReconciliationCandidate::getSubclassSimilarity,
					new NumberRenderer(new DecimalFormat("#.##"))).setCaption("SBSim");
			c_compl.setExpandRatio(1);
			c_imp.setExpandRatio(1);
			c_jack.setExpandRatio(1);
			c_subcl.setExpandRatio(1);
			setItems(p.getAllPatternReconciliations().items());
		}
		Column<PatternReconciliationCandidate, Button> c_bt = addComponentColumn(recon -> {
			Button button = new Button("?");
			button.addClickListener(click -> reconcileClick(recon, p, vl_reconcile));
			return button;
		});
		p1.setExpandRatio(4);
		p2.setExpandRatio(4);

		c_bt.setExpandRatio(1);

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

	ReconcilerGrid(PatternReconciler p, VerticalLayout vl_reconcile) {
		this(p, vl_reconcile, false);
	}

	private void reconcileClick(PatternReconciliationCandidate recon, PatternReconciler r, VerticalLayout vl) {
		vl.removeAllComponents();
		vl.addComponent(new ReconciliationPanel(recon, r));
	}

	public void restrict(ReconciliationCandidateSet candidateSet) {
		setItems(candidateSet.items());
	}
}
