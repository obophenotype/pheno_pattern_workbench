package monarch.ontology.phenoworkbench.browser.quickimpact;

import java.util.Optional;

import com.vaadin.ui.*;

import monarch.ontology.phenoworkbench.util.OntologyClass;
import monarch.ontology.phenoworkbench.browser.basic.MarkDownTools;
import monarch.ontology.phenoworkbench.analytics.pattern.generation.ExplanationRenderProvider;
import monarch.ontology.phenoworkbench.browser.basic.LabelManager;
import monarch.ontology.phenoworkbench.util.ExplanationAnalyser;
import monarch.ontology.phenoworkbench.util.StringUtils;

//Define a sub-window by inheritance
public class ExplanationWindow extends Window {

	/**
	 * 
	 */
	private static final long serialVersionUID = 983881322993478390L;
	ProgressBar bar = new ProgressBar();

	ExplanationWindow(ExplanationRenderProvider qi, OntologyClass p, OntologyClass current) {
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
		// l.setMargin(true);
		StringBuilder sb = new StringBuilder();
		sb.append("<div>");
		Optional<ExplanationAnalyser> e = qi.getSubsumptionExplanationRendered(current, p);
		if (e.isPresent()) {
			ExplanationAnalyser expa = e.get();
			StringBuilder sbmd = StringUtils.linesToStringBuilder(expa.getReport(0));
			String md = MarkDownTools.toHTML(sbmd.toString());
			
			sb.append("<h3>Axioms</h3>");
			sb.append("<ol>");
			for (String ax : expa.getRenderedAxiomList()) {
				sb.append("<li>" + ax + "</li>");
			}
			sb.append("<br />");
			sb.append("<h3>Explanation Analysis</h3>");
			sb.append("<div>");
			//System.out.println(md);
			sb.append(md);
			sb.append("</div>");
			
			
			sb.append("</ol>");
		}
		sb.append("</div>");
		Label label = LabelManager.htmlLabel(sb.toString());
		label.setWidth("100%");
		l.addComponent(label);
		// l.addComponent(bar);
		Panel c = preparePanel(l, "Explanation");
		setContent(c);
	}

	private Panel preparePanel(Component c, String label) {
		Panel panel = new Panel(label);
		panel.setWidth("100%");
		panel.setHeight("100%");
		// c.setSizeUndefined();
		c.setWidth("100%");
		panel.setContent(c);
		return panel;
	}
}