package monarch.ontology.phenoworkbench.browser.basic;

import java.util.Collections;

import com.vaadin.ui.Component;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import monarch.ontology.phenoworkbench.uiutils.basic.LabelManager;
import monarch.ontology.phenoworkbench.util.OntologyClass;
import monarch.ontology.phenoworkbench.analytics.pattern.reconciliation.OntologyTermSet;
import monarch.ontology.phenoworkbench.browser.quickimpact.PatternInfoBox;

//Define a sub-window by inheritance
public class TermInfoWindow extends Window {
	private static final long serialVersionUID = 7544515480410218589L;

	public TermInfoWindow(OntologyClass p,OntologyTermSet ts) {
		super(null);
		center();
		setWidth("600px");
		setHeight("500px");
		setModal(true);

		VerticalLayout l = new VerticalLayout();
		l.setWidth("100%");
		l.setHeightUndefined();
		
		PatternTree pt = new PatternTree(Collections.singleton(p.getNode()));
		pt.expandAll();
		PatternInfoBox sc = new PatternInfoBox();
		l.addComponent(sc);
		l.addComponent(LabelManager.labelH2("Class Hierarchy"));
		l.addComponent(pt);
		Panel c = preparePanel(l, "Term Info");
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