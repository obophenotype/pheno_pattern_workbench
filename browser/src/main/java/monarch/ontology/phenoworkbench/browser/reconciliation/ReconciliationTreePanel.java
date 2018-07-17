package monarch.ontology.phenoworkbench.browser.reconciliation;

import com.vaadin.ui.*;

import monarch.ontology.phenoworkbench.util.OntologyClass;
import monarch.ontology.phenoworkbench.analytics.pattern.generation.PatternProvider;
import monarch.ontology.phenoworkbench.analytics.pattern.reconciliation.PatternReconciler;
import monarch.ontology.phenoworkbench.util.ReconciliationCandidateSet;
import monarch.ontology.phenoworkbench.browser.basic.LayoutUtils;
import monarch.ontology.phenoworkbench.browser.basic.PatternTree;
import monarch.ontology.phenoworkbench.browser.basic.PatternTreeItem;

public class ReconciliationTreePanel extends HorizontalLayout {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5640247323480000539L;
	ReconciliationTreeSummaryInfoBox ib = new ReconciliationTreeSummaryInfoBox();
	MappingGrid grid;
	PatternReconciler p;

	ReconciliationTreePanel(PatternProvider patternProvider, PatternReconciler p, VerticalLayout vl_reconcile) {
		setWidth("100%");
		this.p = p;
		PatternTree tree = new PatternTree(patternProvider.getTopOntologyClasses());
		grid = new MappingGrid(p.getAllPatternReconciliations(),true);
		Panel panel_tree = LayoutUtils.preparePanel(tree, "Browser");
		Layout l_reconciliation = prepareReconciliationInfoPanel(p,vl_reconcile);
		tree.addItemClickListener(e->update(e));

		addComponent(LayoutUtils.prepareSplitPanel(l_reconciliation, panel_tree, 800));
	}

	private Layout prepareReconciliationInfoPanel(PatternReconciler p, VerticalLayout vl_reconcile) {
		VerticalLayout vl = new VerticalLayout();
		vl.setMargin(false);
		vl.setWidth("100%");
		vl.addComponent(LayoutUtils.preparePanel(ib, "Reconciliation Status"));
		vl.addComponent(grid);
		return vl;
	}

	private void update(Tree.ItemClick<PatternTreeItem> e) {
		OntologyClass c  = e.getItem().getPatternClass();
		ReconciliationCandidateSet candidateSet = p.getReconciliationsRelatedToClassOrChildren(c);
		ib.update(candidateSet);
		grid.restrict(candidateSet);
	}
}
