package monarch.ontology.phenoworkbench.browser.reconciliation;

import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;

import monarch.ontology.phenoworkbench.browser.LabelManager;
import monarch.ontology.phenoworkbench.browser.analytics.PatternReconciler;
import monarch.ontology.phenoworkbench.browser.analytics.PatternReconciliation;

//Define a sub-window by inheritance
public class ReconciliationWindow extends Window {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 983881322993478390L;
	ProgressBar bar = new ProgressBar();
	
 public ReconciliationWindow(PatternReconciliation recon, PatternReconciler r) {
     super(null);
     center();
     bar.setIndeterminate(true);
     setWidth("500px");
     setHeight("300px");
     bar.setWidth("100%");
     bar.setHeight("100%");
     setModal(true);

     VerticalLayout l = new VerticalLayout();
     l.setWidth("100%");
     l.setHeightUndefined();
     //l.setMargin(true);
     StringBuilder sb = new StringBuilder();
     sb.append("<div>");
     sb.append("<div style='background-color:white;'>");
     sb.append("<table style='width:100%'><tr><th>Pattern 1</th><th>Pattern 2</th></tr>");
     sb.append("<tr><td>"+recon.getP1().toString().replaceAll(":",":<br/>")+"</td><td>"+recon.getP2().toString().replaceAll(":",":<br/>")+"</td></tr></table>");
     sb.append("</div>");
     sb.append("<ul>");
     sb.append("<li>Complexity of reconciliation: "+recon.getReconciliationComplexity()+"</li>");
     sb.append("<li>Logical equivalence: "+recon.isLogicallyEquivalent()+"</li>");
     sb.append("<li>Syntactic equivalence: "+recon.isSyntacticallyEquivalent()+"</li>");
     sb.append("<li>Diff --&gt;: <br />"+recon.getRightDiff().toString().replaceAll("\n","<br />")+"</li>");
     sb.append("<li>Diff &lt;--: <br />"+recon.getLeftDiff().toString().replaceAll("\n","<br />")+"</li>");
     sb.append("</ul>");
     sb.append("</div>");
     Label label = LabelManager.htmlLabel(sb.toString());
     label.setWidth("100%");
     l.addComponent(label);
     //l.addComponent(bar);
     Panel c = preparePanel(l,"Reconciliation");
     setContent(c);

 }

    private Panel preparePanel(Component c, String label) {
        Panel panel = new Panel(label);
        panel.setWidth("100%");
        panel.setHeight("100%");
        //c.setSizeUndefined();
        c.setWidth("100%");
        panel.setContent(c);
        return panel;
    }
}