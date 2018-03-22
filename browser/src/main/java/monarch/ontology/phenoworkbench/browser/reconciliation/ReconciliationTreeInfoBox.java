package monarch.ontology.phenoworkbench.browser.reconciliation;

import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

import monarch.ontology.phenoworkbench.analytics.pattern.generation.OntologyClass;
import monarch.ontology.phenoworkbench.analytics.pattern.reconciliation.PatternReconciliationCandidate;
import monarch.ontology.phenoworkbench.browser.basic.HTMLRenderUtils;
import monarch.ontology.phenoworkbench.browser.basic.LabelManager;

public class ReconciliationTreeInfoBox extends VerticalLayout {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2254864769616615100L;
	Label label = LabelManager.htmlLabel("<h3>Nothing Selected</h3>");

	public ReconciliationTreeInfoBox() {
		setWidth("100%");
		setHeightUndefined();
		setMargin(false);
		setSpacing(true);
		label.setSizeFull();
		addComponent(label);
	}
	
	public void update(PatternReconciliationCandidate recon) {
		if(recon!=null) {
		String sb = "<div><ul>" 
				+ "<li>Complexity of reconciliation: " + recon.getReconciliationComplexity()+ "</li>" 
				+ "<li>Logical equivalence: " + recon.isLogicallyEquivalent() + "</li>"
				+ "<li>Syntactic equivalence: " + recon.isSyntacticallyEquivalent() + "</li>"
				+ "<li>Grammatical equivalence: " + recon.isSyntacticallyEquivalent() + "</li>"
				+ "<li>Common ancestors: <ol>";
				for(OntologyClass c:recon.getCommonAncestors()) {
					sb+="<li>"+HTMLRenderUtils.renderOLSLinkout(c)+"</li>";
				}
				sb = sb + "</ol></li>"
				+ "<li>Impact: " + recon.getReconciliationEffect() + "</li>" 
				+ "</ul></div>";
		label.setValue(sb);
		}
		else {
			label.setValue("No suitable reconciliation partner found!");
		}
	}

}
