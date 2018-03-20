package monarch.ontology.phenoworkbench.browser.quickimpact;

import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

import monarch.ontology.phenoworkbench.analytics.pattern.generation.DefinedClass;
import monarch.ontology.phenoworkbench.analytics.pattern.generation.OntologyClass;
import monarch.ontology.phenoworkbench.analytics.pattern.impact.OntologyClassImpact;
import monarch.ontology.phenoworkbench.analytics.quickimpact.QuickImpact;
import monarch.ontology.phenoworkbench.analytics.pattern.generation.PatternGrammar;
import monarch.ontology.phenoworkbench.browser.basic.LabelManager;

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
		setMargin(true);
		setSpacing(true);
		label.setSizeFull();
		addComponent(label);
		addComponent(grid);
	}

	public void setValue(OntologyClass p, QuickImpact quickImpact) {
		label.setValue(renderImpact(p, quickImpact));
		grid.update(quickImpact, p);
	}

	private String renderImpact(OntologyClass p, QuickImpact quickImpact) {
		StringBuilder sb = new StringBuilder();
		sb.append("<div style='background-color: white;'>");
		sb.append("<h3>" + patternName(p) + "</h3>");
		if (p instanceof DefinedClass) {
			DefinedClass definedClass = (DefinedClass) p;
			Optional<OntologyClassImpact> impactOptional = quickImpact.getImpact(definedClass);
			if(impactOptional.isPresent()) {
				OntologyClassImpact impact = impactOptional.get();
				sb.append("<strong>" + definedClass.getPatternString() + "</strong>");
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
			
			for (PatternGrammar g : quickImpact.getSubsumedGrammars(definedClass)) {
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

}
