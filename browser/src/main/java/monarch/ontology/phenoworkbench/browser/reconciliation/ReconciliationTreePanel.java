package monarch.ontology.phenoworkbench.browser.reconciliation;

import com.vaadin.ui.*;

import monarch.ontology.phenoworkbench.util.Node;
import monarch.ontology.phenoworkbench.analytics.pattern.reconciliation.PatternReconciler;
import monarch.ontology.phenoworkbench.util.ReconciliationCandidateSet;
import monarch.ontology.phenoworkbench.browser.basic.LayoutUtils;
import monarch.ontology.phenoworkbench.browser.basic.PatternTree;

import java.util.HashSet;
import java.util.Set;

public class ReconciliationTreePanel extends HorizontalLayout {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5640247323480000539L;
	ReconciliationTreeSummaryInfoBox ib = new ReconciliationTreeSummaryInfoBox();
	MappingGrid grid;
	PatternReconciler p;

	ReconciliationTreePanel(PatternReconciler p, ReconciliationCandidateSet cset, VerticalLayout vl_reconcile) {
		setWidth("100%");
		this.p = p;
		Set<Node> nodes = new HashSet<>();
        p.getPatternProvider().getTopOntologyClasses(true).forEach(n->nodes.add(n.getNode()));
		PatternTree tree = new PatternTree(nodes);
		grid = new MappingGrid(cset,true);
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

	private void update(Tree.ItemClick<Node> e) {
		Node c  = e.getItem();
		ReconciliationCandidateSet candidateSet = p.getReconciliationsRelatedToClassOrChildren(c.getRepresentativeElement());
		ib.update(candidateSet);
		grid.restrict(candidateSet);
	}
}
