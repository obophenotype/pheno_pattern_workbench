package monarch.ontology.phenoworkbench.browser.reconciliation;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Window;
import com.vaadin.ui.renderers.HtmlRenderer;
import com.vaadin.ui.renderers.NumberRenderer;

import monarch.ontology.phenoworkbench.analytics.pattern.reconciliation.PatternReconciler;
import monarch.ontology.phenoworkbench.analytics.pattern.reconciliation.PatternReconciliationCandidate;
import monarch.ontology.phenoworkbench.util.StringUtils;

public class ReconcilerGrid extends Grid<PatternReconciliationCandidate> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1503748580420486580L;
	List<PatternReconciliationCandidate> reconciliations = new ArrayList<>();
	public ReconcilerGrid(PatternReconciler p) {
		setWidth("100%");
		setHeight("100%");
		System.out.println("Get Reconciliations");
		reconciliations.addAll(p.getAllPatternReconciliations());
		System.out.println("Set Reconciliations: "+reconciliations.size());
		setItems(reconciliations);
		Column p1 = addColumn(rec -> "<div>" + StringUtils.insertPeriodically(rec.getP1().toString(),"<br>",50)+ "</div>", new HtmlRenderer()).setCaption("DefinedClass 1");
		Column p2 = addColumn(rec -> "<div>" + StringUtils.insertPeriodically(rec.getP2().toString(),"<br>",50)+ "</div>", new HtmlRenderer()).setCaption("DefinedClass 2");
		Column c_compl = addColumn(PatternReconciliationCandidate::getReconciliationComplexity,new NumberRenderer(new DecimalFormat("#.##"))).setCaption("Complexity");
		Column c_imp = addColumn(PatternReconciliationCandidate::getReconciliationEffect,new NumberRenderer(new DecimalFormat("#.####"))).setCaption("OntologyClassImpact");
		Column c_jack = addColumn(PatternReconciliationCandidate::getJaccardSimiliarity,new NumberRenderer(new DecimalFormat("#.##"))).setCaption("Jackard");
		Column c_subcl = addColumn(PatternReconciliationCandidate::getSubclassSimilarity,new NumberRenderer(new DecimalFormat("#.##"))).setCaption("SBSim");

		Column c_bt = addComponentColumn(recon -> {
		      Button button = new Button("Reconcile");
		      button.addClickListener(click -> reconcileClick(recon,p));
		      return button;
		      });
		p1.setExpandRatio(4);
		p2.setExpandRatio(4);
		c_compl.setExpandRatio(1);
		c_imp.setExpandRatio(1);
		c_bt.setExpandRatio(1);
		c_jack.setExpandRatio(1);
		c_subcl.setExpandRatio(1);
		System.out.println("Change row height");
		setRowHeight(100);
		System.out.println("Done Creating Grid");
		
	}
	private void reconcileClick(PatternReconciliationCandidate recon, PatternReconciler r) {
		Window sub = new ReconciliationWindow(recon, r);
		  this.getUI().addWindow(sub);
		  this.getUI().push(); 
		  
	}
	
	public void setExcludeReconciled(boolean exclude) {
		
	}
}
