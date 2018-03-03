package monarch.ontology.phenoworkbench.browser;

import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;

import monarch.ontology.phenoworkbench.browser.analytics.Pattern;
import monarch.ontology.phenoworkbench.browser.analytics.PatternClass;
import monarch.ontology.phenoworkbench.browser.analytics.PatternGrammar;
import monarch.ontology.phenoworkbench.browser.analytics.PatternImpact;
import monarch.ontology.phenoworkbench.browser.analytics.QuickImpact;

public class PatternInfoBox extends HorizontalLayout {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2254864769616615100L;
	Label label = LabelManager.htmlLabel("<h3>Nothing Selected</h3>");

	public PatternInfoBox() {
		setWidth("100%");
		setHeightUndefined();
		setMargin(true);
		setSpacing(true);
		label.setSizeFull();
		addComponent(label);
	}

	public void setValue(PatternClass p, QuickImpact quickImpact) {
		label.setValue(renderImpact(p, quickImpact));
	}

	private String renderImpact(PatternClass p, QuickImpact quickImpact) {
		StringBuilder sb = new StringBuilder();
		sb.append("<div style='background-color: white;'>");
		sb.append("<h3>" + p.getLabel() + "</h3>");
		if (p instanceof Pattern) {
			Pattern pattern = (Pattern) p;
			PatternImpact.Impact impact = quickImpact.getImpact(pattern);
			sb.append("<strong>" + pattern.getPatternString() + "</strong>");
			sb.append("<ol>");
			sb.append("<li>Direct: " + impact.getDirectImpact() + "</li>");
			sb.append("<li>Indirect: " + impact.getIndirectImpact() + "</li>");
			sb.append("</ol>");
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

}
