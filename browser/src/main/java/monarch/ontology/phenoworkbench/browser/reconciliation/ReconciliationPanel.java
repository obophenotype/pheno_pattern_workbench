package monarch.ontology.phenoworkbench.browser.reconciliation;

import com.vaadin.ui.*;

import monarch.ontology.phenoworkbench.analytics.pattern.generation.DefinedClass;
import monarch.ontology.phenoworkbench.analytics.pattern.generation.OntologyClass;
import monarch.ontology.phenoworkbench.analytics.pattern.reconciliation.PatternReconciler;
import monarch.ontology.phenoworkbench.analytics.pattern.reconciliation.PatternReconciliationCandidate;
import monarch.ontology.phenoworkbench.analytics.quickimpact.QuickImpact;
import monarch.ontology.phenoworkbench.browser.basic.HTMLRenderUtils;
import monarch.ontology.phenoworkbench.browser.basic.LabelManager;
import monarch.ontology.phenoworkbench.browser.basic.LayoutUtils;
import monarch.ontology.phenoworkbench.browser.basic.PatternTree;
import monarch.ontology.phenoworkbench.browser.basic.PatternTreeItem;
import monarch.ontology.phenoworkbench.browser.quickimpact.PatternInfoBox;
import monarch.ontology.phenoworkbench.browser.quickimpact.WeightedPattern;
import monarch.ontology.phenoworkbench.browser.quickimpact.WeightedPatternGrid;

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
		PatternInfoBox ib1 = new PatternInfoBox();
        VerticalLayout vl_ib1 = LayoutUtils.hlNoMarginNoSpacingNoSize(ib1);
        Panel info1 = LayoutUtils.preparePanel(vl_ib1, "Selected Class");
        p1.addItemClickListener(event -> updateInfoBox(r,ib1, event.getItem(), p1));
        
		PatternTree p2 = new PatternTree(Collections.singleton(recon.getP2()));
		PatternInfoBox ib2 = new PatternInfoBox();
        VerticalLayout vl_ib2 = LayoutUtils.hlNoMarginNoSpacingNoSize(ib2);
        Panel info2 = LayoutUtils.preparePanel(vl_ib2, "Selected Class");
        p2.addItemClickListener(event -> updateInfoBox(r,ib2, event.getItem(), p2));

		
		ReconciliationTreeInfoBox treeinfo = new ReconciliationTreeInfoBox(recon);
		VerticalLayout vl_reeinfo = LayoutUtils.hlNoMarginNoSpacingNoSize(treeinfo);
		Panel p_treeinfo = LayoutUtils.preparePanel(vl_reeinfo, "Tree info");
		
		GridLayout hl_trees = new GridLayout(3,5);
		hl_trees.setWidth("100%");
		hl_trees.addComponent(LabelManager.labelH1("Reconciliation",100.0f,Unit.PERCENTAGE),0,0,1,0);
		hl_trees.addComponent(LabelManager.htmlLabel(HTMLRenderUtils.renderDefinedClass(recon.getP1()),100.0f,Unit.PERCENTAGE),0,1);
		hl_trees.addComponent(LabelManager.htmlLabel(HTMLRenderUtils.renderDefinedClass(recon.getP2()),100.0f,Unit.PERCENTAGE),1,1);
		hl_trees.addComponent(LayoutUtils.vl100(LabelManager.labelH2("Hierarchy 1",100.0f,Unit.PERCENTAGE)),0,2);
		hl_trees.addComponent(LayoutUtils.vl100(LabelManager.labelH2("Hierarchy 2",100.0f,Unit.PERCENTAGE)),1,2);
		hl_trees.addComponent(info,2,0,2,2);
		hl_trees.addComponent(p1,0,3);
		hl_trees.addComponent(p2,1,3);
		hl_trees.addComponent(p_treeinfo,2,3,2,4);
		hl_trees.addComponent(info1,0,4);
		hl_trees.addComponent(info2,1,4);
		hl_trees.setColumnExpandRatio(0, 3);
		hl_trees.setColumnExpandRatio(1, 3);
		hl_trees.setColumnExpandRatio(2, 3);
		addComponent(hl_trees);

	}
	
	private void updateInfoBox(PatternReconciler r, PatternInfoBox impactbox, Object pi, PatternTree tree) {
        if (pi instanceof PatternTreeItem) {
            OntologyClass pc = ((PatternTreeItem) pi).getPatternClass();
            impactbox.setValue(pc, r,r,r);
        } 
        this.getUI().push();
    }
}