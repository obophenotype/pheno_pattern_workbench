package monarch.ontology.phenoworkbench.browser.candident;

import com.vaadin.ui.Component;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import monarch.ontology.phenoworkbench.uiutils.basic.LabelManager;
import monarch.ontology.phenoworkbench.util.CandidateKB;
import monarch.ontology.phenoworkbench.util.OntologyClass;
import monarch.ontology.phenoworkbench.analytics.pattern.reconciliation.CandidateIdentifierApp;

//Define a sub-window by inheritance
public class SuggestSimilarWindow extends Window {
	private static final long serialVersionUID = 7544515480410218589L;

	public SuggestSimilarWindow(OntologyClass p,CandidateIdentifierApp app,CandidateKB kb) {
		super(null);
		center();
		setWidth("600px");
		setHeight("500px");
		setModal(true);

		VerticalLayout l = new VerticalLayout();
		l.setWidth("100%");
		l.setHeightUndefined();
		l.addComponent(LabelManager.labelH2("Suggested Related Candidates"));
		SuggestedCandidateGrid grid = new SuggestedCandidateGrid(kb);
		grid.setItems(app.getSuggestions(p,kb.getBuckets()));
		l.addComponent(grid);
		Panel c = preparePanel(l, "Term Info");
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