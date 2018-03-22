package monarch.ontology.phenoworkbench.browser.reconciliation;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Tree.ItemClick;
import com.vaadin.ui.VerticalLayout;

import monarch.ontology.phenoworkbench.analytics.pattern.generation.OntologyClass;
import monarch.ontology.phenoworkbench.analytics.pattern.reconciliation.PatternReconciler;
import monarch.ontology.phenoworkbench.analytics.pattern.reconciliation.PatternReconciliationCandidate;
import monarch.ontology.phenoworkbench.analytics.pattern.reconciliation.ReconciliationCandidateSet;
import monarch.ontology.phenoworkbench.browser.basic.LabelManager;
import monarch.ontology.phenoworkbench.browser.basic.LayoutUtils;
import monarch.ontology.phenoworkbench.browser.basic.PatternTree;
import monarch.ontology.phenoworkbench.browser.basic.PatternTreeItem;
import monarch.ontology.phenoworkbench.browser.quickimpact.PatternInfoBox;

//Define a sub-window by inheritance
class ReconciliationPanel extends VerticalLayout {

	/**
	 * 
	 */
	private static final long serialVersionUID = 983881322993478390L;
	PatternTree p1;
	PatternTree p2;
	ReconciliationTreeSummaryInfoBox ib_treeinfobox = new ReconciliationTreeSummaryInfoBox();	
	PatternReconciler r;
	PatternInfoBox ib1 = new PatternInfoBox();
	PatternInfoBox ib2 = new PatternInfoBox();

	ReconciliationPanel(PatternReconciliationCandidate recon, PatternReconciler r) {
		setWidth("100%");
		setMargin(false);
		this.r = r;
		int height = 1000;
		ReconciliationInfoBox l_reconciliationinfo = new ReconciliationInfoBox(recon);
		Panel info = LayoutUtils.preparePanel(LayoutUtils.hlNoMarginNoSpacingNoSize(l_reconciliationinfo),
				"Reconciliation Summary");

		p1 = new PatternTree(Collections.singleton(recon.getP1()));
		Panel panel_p1 = LayoutUtils.preparePanel(p1, "Hierarchy 1");
		panel_p1.setHeight(height+"px");

		
		Panel info1 = LayoutUtils.preparePanel(LayoutUtils.hlNoMarginNoSpacingNoSize(ib1), "<- Selected");

		p2 = new PatternTree(Collections.singleton(recon.getP2()));
		Panel panel_p2 = LayoutUtils.preparePanel(p2, "Hierarchy 2");
		panel_p2.setHeight(height+"px");

		Panel info2 = LayoutUtils.preparePanel(LayoutUtils.hlNoMarginNoSpacingNoSize(ib2), "Selected ->");

		Panel panel_treeinfobox = LayoutUtils.preparePanel(LayoutUtils.hlNoMarginNoSpacingNoSize(ib_treeinfobox),
				"Reconciliation Status");

		VerticalLayout vl_treeinfo = new VerticalLayout();
		vl_treeinfo.addComponent(panel_treeinfobox);
		vl_treeinfo.addComponent(info1);
		vl_treeinfo.addComponent(info2);
		vl_treeinfo.setHeight(height+"px");

		GridLayout hl_trees = new GridLayout(3, 2);
		hl_trees.setWidth("100%");
		hl_trees.addComponent(info, 0, 0, 2, 0);
		hl_trees.addComponent(panel_p1, 0, 1);
		hl_trees.addComponent(vl_treeinfo, 1, 1);
		hl_trees.addComponent(panel_p2, 2, 1);
		hl_trees.setColumnExpandRatio(0, 3);
		hl_trees.setColumnExpandRatio(1, 3);
		hl_trees.setColumnExpandRatio(2, 3);

		p1.addItemClickListener(e -> update(e));
		p2.addItemClickListener(e -> update(e));

		addComponent(LabelManager.labelH1("Reconciliation", 100.0f, Unit.PERCENTAGE));
		addComponent(hl_trees);

		p1.addItemClickListener(event -> updateInfoBox(r, ib1, event.getItem(), p1));
		p2.addItemClickListener(event -> updateInfoBox(r, ib2, event.getItem(), p2));
		updateClassInforBox(r, ib1, recon.getP1());
		updateClassInforBox(r, ib2, recon.getP2());
	}

	private void update(ItemClick<PatternTreeItem> e) {
		System.out.println(e);
		OntologyClass pc = e.getItem().getPatternClass();
		ReconciliationCandidateSet candidates = r.getReconciliationsRelatedTo(pc);
		Optional<PatternReconciliationCandidate> cl = candidates.getClosestMatchCandidate();
		if (cl.isPresent()) {
			PatternReconciliationCandidate prc = cl.get();
			System.out.println("There is a candidate!");
			OntologyClass other = prc.getP1().equals(pc) ? prc.getP2() : prc.getP1();
			if (e.getSource().equals(p1)) {
				System.out.println("Set source 2");
				p2.expandSelect(other);
				ib2.setValue(other,r,r,r);
			} else if (e.getSource().equals(p2)) {
				System.out.println("Set source 1");
				p1.expandSelect(other);
				ib1.setValue(other,r,r,r);
			}
		}
		ib_treeinfobox.update(candidates);
	}



	private void updateInfoBox(PatternReconciler r, PatternInfoBox impactbox, PatternTreeItem pi, PatternTree tree) {
		OntologyClass pc = ((PatternTreeItem) pi).getPatternClass();
		updateClassInforBox(r, impactbox, pc);
		this.getUI().push();
	}

	private void updateClassInforBox(PatternReconciler r, PatternInfoBox impactbox, OntologyClass pc) {
		impactbox.setValue(pc, r, r, r);		
	}
}