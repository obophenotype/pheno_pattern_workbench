package monarch.ontology.phenoworkbench.browser;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Window;
import com.vaadin.ui.renderers.HtmlRenderer;

import monarch.ontology.phenoworkbench.browser.analytics.PatternReconciler;
import monarch.ontology.phenoworkbench.browser.analytics.PatternReconciliation;
import monarch.ontology.phenoworkbench.browser.util.StringUtils;

public class ReconcilerGrid extends Grid<PatternReconciliation> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1503748580420486580L;
	List<PatternReconciliation> reconciliations = new ArrayList<>();
	List<PatternReconciliation> all_reconciliations = new ArrayList<>();
	public ReconcilerGrid(PatternReconciler p) {
		setWidth("100%");
		setHeightMode(HeightMode.UNDEFINED);
		reconciliations.addAll(p.getAllPatternReconciliations());
		setItems(reconciliations);
		Column p1 = addColumn(rec -> "<div>" + StringUtils.insertPeriodically(rec.getP1().toString(),"<br>",50)+ "</div>", new HtmlRenderer()).setCaption("Pattern 1");
		Column p2 = addColumn(rec -> "<div>" + StringUtils.insertPeriodically(rec.getP2().toString(),"<br>",50)+ "</div>", new HtmlRenderer()).setCaption("Pattern 2");
		Column c_compl = addColumn(PatternReconciliation::getReconciliationComplexity).setCaption("Complexity");
		Column c_imp = addColumn(PatternReconciliation::getReconciliationEffect).setCaption("Impact");
		Column c_bt = addComponentColumn(recon -> {
		      Button button = new Button("Reconcile");
		      button.addClickListener(click -> reconcileClick(recon,p));
		      return button;
		      });
		p1.setExpandRatio(3);
		p2.setExpandRatio(3);
		c_compl.setExpandRatio(1);
		c_imp.setExpandRatio(1);
		c_bt.setExpandRatio(1);
		setRowHeight(100);
	}
	private void reconcileClick(PatternReconciliation recon, PatternReconciler r) {
		Window sub = new ReconciliationWindow(recon, r);
		  this.getUI().addWindow(sub);
		  this.getUI().push(); 
		  
	}
	
	public void setExcludeReconciled(boolean exclude) {
		
	}
}
