package monarch.ontology.phenoworkbench.browser.quickimpact;

import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

import monarch.ontology.phenoworkbench.browser.LabelManager;
import monarch.ontology.phenoworkbench.browser.analytics.Pattern;
import monarch.ontology.phenoworkbench.browser.analytics.PatternClass;
import monarch.ontology.phenoworkbench.browser.analytics.PatternGrammar;
import monarch.ontology.phenoworkbench.browser.analytics.PatternImpact;
import monarch.ontology.phenoworkbench.browser.analytics.QuickImpact;

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

	public void setValue(PatternClass p, QuickImpact quickImpact) {
		label.setValue(renderImpact(p, quickImpact));
		grid.update(quickImpact, p);
	}

	private String renderImpact(PatternClass p, QuickImpact quickImpact) {
		StringBuilder sb = new StringBuilder();
		sb.append("<div style='background-color: white;'>");
		sb.append("<h3>" + patternName(p) + "</h3>");
		if (p instanceof Pattern) {
			Pattern pattern = (Pattern) p;
			PatternImpact.Impact impact = quickImpact.getImpact(pattern);
			sb.append("<strong>" + pattern.getPatternString() + "</strong>");
			sb.append("<ol>");
			sb.append("<li>Direct: " + impact.getDirectImpact() + "</li>");
			sb.append("<li>Indirect: " + impact.getIndirectImpact() + "</li>");
			sb.append("</ol>");
			sb.append("<li>Impact by ontology<ol>");
			for(String oid:impact.getDirectImpactByO().keySet()) {
				sb.append("<li>"+oid+"<ol>");
				sb.append("<li>Direct: " + impact.getDirectImpactByO().get(oid) + "</li>");
				sb.append("<li>Indirect: " + impact.getIndirectImpactByO().get(oid) + "</li>");
				sb.append("</ol></li>");
			}
			sb.append("</ol></li>");
			sb.append("<h3>Grammars subsumed under this pattern:</h3>");
			sb.append("<div style='border:1px solid black; padding: 10px;'>");
			sb.append("<strong>Self: " + pattern.getGrammar().getOriginal() + "</strong>");
			sb.append("</div>");
			sb.append("<ol>");
			System.out.println(pattern);
			System.out.println(quickImpact);
			
			for (PatternGrammar g : quickImpact.getSubsumedGrammars(pattern)) {
				sb.append("<li>" + g.getOriginal() + "</li>");
			}
			sb.append("</ol>");
		} else {
			sb.append("Regular class");
		}

		sb.append("</div>");
		return sb.toString();
	}

	private String patternName(PatternClass p) {
		return "<a href='"+OLSLinkout.linkout(p.getOWLClass().getIRI().toString(),p.getLabel())+"'>"+p.getLabel()+"</a>";
	}

}
