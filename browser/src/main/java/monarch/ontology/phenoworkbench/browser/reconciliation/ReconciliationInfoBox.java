package monarch.ontology.phenoworkbench.browser.reconciliation;

import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;

import monarch.ontology.phenoworkbench.analytics.pattern.generation.OntologyClass;
import monarch.ontology.phenoworkbench.analytics.pattern.reconciliation.PatternReconciliationCandidate;
import monarch.ontology.phenoworkbench.browser.basic.HTMLRenderUtils;
import monarch.ontology.phenoworkbench.browser.basic.LabelManager;

public class ReconciliationInfoBox extends HorizontalLayout {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2254864769616615100L;	;

	public ReconciliationInfoBox(PatternReconciliationCandidate recon) {
		setWidth("100%");
		setHeightUndefined();
		setMargin(true);
		setSpacing(true);
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
		Label l_info = LabelManager.htmlLabel(sb);
		Label l_p1 = LabelManager.htmlLabel(HTMLRenderUtils.renderDefinedClass(recon.getP1()));
		Label l_p2 = LabelManager.htmlLabel(HTMLRenderUtils.renderDefinedClass(recon.getP2()));
		l_info.setWidth("100%");
		l_p1.setWidth("100%");
		l_p2.setWidth("100%");
		addComponent(l_info);
		addComponent(l_p1);
		addComponent(l_p2);
	}

}
