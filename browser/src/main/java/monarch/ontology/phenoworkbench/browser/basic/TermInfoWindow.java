package monarch.ontology.phenoworkbench.browser.basic;

import java.util.Collections;

import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import monarch.ontology.phenoworkbench.analytics.pattern.generation.OntologyClass;
import monarch.ontology.phenoworkbench.analytics.pattern.reconciliation.OntologyTermSet;
import monarch.ontology.phenoworkbench.browser.quickimpact.PatternInfoBox;

//Define a sub-window by inheritance
public class TermInfoWindow extends Window {
	private static final long serialVersionUID = 7544515480410218589L;

	public TermInfoWindow(OntologyClass p,OntologyTermSet ts) {
		super(null);
		center();
		setWidth("500px");
		setHeight("300px");
		setModal(true);

		VerticalLayout l = new VerticalLayout();
		l.setWidth("100%");
		l.setHeightUndefined();
		// l.setMargin(true);
		Label label = LabelManager.htmlLabel(p.getLabel());
		label.setWidth("100%");
		l.addComponent(label);
		l.addComponent(LabelManager.htmlLabel("Class Hierarchy"));
		PatternTree pt = new PatternTree(Collections.singleton(p));
		// l.addComponent(bar);
		pt.expandAll();
		l.addComponent(pt);
		
		PatternInfoBox sc = new PatternInfoBox();
		l.addComponent(sc);
		Panel c = preparePanel(l, "Explanation");
		setContent(c);
		sc.setValue(p, ts, ts, ts);
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