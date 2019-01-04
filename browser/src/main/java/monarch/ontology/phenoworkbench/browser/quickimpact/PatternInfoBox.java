package monarch.ontology.phenoworkbench.browser.quickimpact;

import com.vaadin.shared.Registration;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.components.grid.ItemClickListener;

import monarch.ontology.phenoworkbench.uiutils.basic.LabelManager;
import monarch.ontology.phenoworkbench.util.DefinedClass;
import monarch.ontology.phenoworkbench.util.OntologyClass;
import monarch.ontology.phenoworkbench.analytics.pattern.impact.OntologyClassImpact;
import monarch.ontology.phenoworkbench.util.PatternGrammar;
import monarch.ontology.phenoworkbench.analytics.pattern.generation.ExplanationRenderProvider;
import monarch.ontology.phenoworkbench.analytics.pattern.generation.GrammarProvider;
import monarch.ontology.phenoworkbench.analytics.pattern.generation.ImpactProvider;
import monarch.ontology.phenoworkbench.browser.basic.HTMLRenderUtils;

import java.util.Optional;

public class PatternInfoBox extends VerticalLayout {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2254864769616615100L;
	Label label = LabelManager.htmlLabel("<h3>Nothing Selected</h3>");
	SuperClassGrid grid = new SuperClassGrid();

	public PatternInfoBox() {
		setWidth("100%");
		setHeightUndefined();
		setMargin(false);
		setSpacing(true);
		label.setSizeFull();
		addComponent(label);
		addComponent(grid);
	}

	public void setValue(OntologyClass p, ExplanationRenderProvider explanation,ImpactProvider impact, GrammarProvider grammar) {
		label.setValue(renderImpact(p, impact,grammar));
		grid.update(explanation, p);
	}

	private String renderImpact(OntologyClass p, ImpactProvider quickImpact, GrammarProvider grammar) {
		StringBuilder sb = new StringBuilder();
		sb.append("<div style='background-color: white;'>");
		sb.append("<h3>" + patternName(p) + "</h3>");
		if (p instanceof DefinedClass) {
			DefinedClass definedClass = (DefinedClass) p;
			sb.append(HTMLRenderUtils.renderOntologyClass(definedClass));
			Optional<OntologyClassImpact> impactOptional = quickImpact.getImpact(definedClass);
			if(impactOptional.isPresent()) {
				OntologyClassImpact impact = impactOptional.get();
				sb.append("<ol>");
				sb.append("<li>Direct: " + impact.getDirectImpact() + "</li>");
				sb.append("<li>Indirect: " + impact.getIndirectImpact() + "</li>");
				sb.append("</ol>");
				sb.append("<li>OntologyClassImpact by ontology<ol>");
				for (String oid : impact.getDirectImpactByO().keySet()) {
					sb.append("<li>" + oid + "<ol>");
					sb.append("<li>Direct: " + impact.getDirectImpactByO().get(oid) + "</li>");
					sb.append("<li>Indirect: " + impact.getIndirectImpactByO().get(oid) + "</li>");
					sb.append("</ol></li>");
				}
				sb.append("</ol></li>");
			}
			sb.append("<h3>Grammars subsumed under this definedClass:</h3>");
			sb.append("<div style='border:1px solid black; padding: 10px;'>");
			sb.append("<strong>Self: " + definedClass.getGrammar().getOriginal() + "</strong>");
			sb.append("</div>");
			sb.append("<ol>");
			System.out.println(definedClass);
			System.out.println(quickImpact);
			
			for (PatternGrammar g : grammar.getSubsumedGrammars(definedClass)) {
				sb.append("<li>" + g.getOriginal() + "</li>");
			}
			sb.append("</ol>");
		} else {
			sb.append("Regular class");
		}

		sb.append("</div>");
		return sb.toString();
	}

	private String patternName(OntologyClass p) {
		return "<a href=\""+OLSLinkout.linkout(p.getOWLClass().getIRI().toString(),p.getLabel())+"\" target=\"_blank\">"+p.getLabel()+"</a>";
	}
	
	public Registration addItemClickListener(ItemClickListener<? super OntologyClass> listener) {
		return grid.addItemClickListener(listener);
	}

}
