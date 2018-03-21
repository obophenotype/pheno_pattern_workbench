package monarch.ontology.phenoworkbench.browser.reconciliation;

import com.vaadin.ui.*;

import monarch.ontology.phenoworkbench.analytics.pattern.reconciliation.PatternReconciler;
import monarch.ontology.phenoworkbench.analytics.pattern.reconciliation.PatternReconciliationCandidate;
import monarch.ontology.phenoworkbench.browser.basic.HTMLRenderUtils;
import monarch.ontology.phenoworkbench.browser.basic.LabelManager;
import monarch.ontology.phenoworkbench.browser.basic.LayoutUtils;
import monarch.ontology.phenoworkbench.browser.basic.PatternTree;

import java.util.Collections;

//Define a sub-window by inheritance
class ReconciliationPanel extends VerticalLayout {

	/**
	 * 
	 */
	private static final long serialVersionUID = 983881322993478390L;

	ReconciliationPanel(PatternReconciliationCandidate recon, PatternReconciler r) {
		setWidth("100%");
		setMargin(false);
		ReconciliationInfoBox l_reconciliationinfo = new ReconciliationInfoBox(recon);
		VerticalLayout vl_infobox = LayoutUtils.hlNoMarginNoSpacingNoSize(l_reconciliationinfo);
		Panel info = LayoutUtils.preparePanel(vl_infobox, "DefinedClass info");
		
		PatternTree p1 = new PatternTree(Collections.singleton(recon.getP1()));
		PatternTree p2 = new PatternTree(Collections.singleton(recon.getP2()));
		
		ReconciliationTreeInfoBox treeinfo = new ReconciliationTreeInfoBox(recon);
		VerticalLayout vl_reeinfo = LayoutUtils.hlNoMarginNoSpacingNoSize(treeinfo);
		Panel p_treeinfo = LayoutUtils.preparePanel(vl_reeinfo, "Tree info");
		
		GridLayout hl_trees = new GridLayout(3,4);
		hl_trees.setWidth("100%");
		hl_trees.addComponent(LabelManager.labelH1("Reconciliation",100.0f,Unit.PERCENTAGE),0,0,1,0);
		hl_trees.addComponent(LabelManager.htmlLabel(HTMLRenderUtils.renderDefinedClass(recon.getP1()),100.0f,Unit.PERCENTAGE),0,1);
		hl_trees.addComponent(LabelManager.htmlLabel(HTMLRenderUtils.renderDefinedClass(recon.getP2()),100.0f,Unit.PERCENTAGE),1,1);
		hl_trees.addComponent(LayoutUtils.vl100(LabelManager.labelH2("Hierarchy 1",100.0f,Unit.PERCENTAGE)),0,2);
		hl_trees.addComponent(LayoutUtils.vl100(LabelManager.labelH2("Hierarchy 2",100.0f,Unit.PERCENTAGE)),1,2);
		hl_trees.addComponent(info,2,0,2,2);
		hl_trees.addComponent(p1,0,3);
		hl_trees.addComponent(p2,1,3);
		hl_trees.addComponent(p_treeinfo,2,3);
		hl_trees.setColumnExpandRatio(0, 3);
		hl_trees.setColumnExpandRatio(1, 3);
		hl_trees.setColumnExpandRatio(2, 3);
		addComponent(LayoutUtils.vl100(LabelManager.labelH1("Hierarchy 1",100.0f,Unit.PERCENTAGE)));
		addComponent(hl_trees);

	}
}