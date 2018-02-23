package monarch.ontology.phenoworkbench.browser;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBoxGroup;
import com.vaadin.ui.Component;
import com.vaadin.ui.Layout;
import com.vaadin.ui.VerticalLayout;

public class RunAnalysisPanel extends VerticalLayout {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5663467371143185799L;
	OntologyRegistry registry = new OntologyRegistry();
	CheckBoxGroup<String> cb_selectontologies = new CheckBoxGroup<>("Select Ontologies");
	RunAnalysisComponent rac = new RunAnalysisComponent();
	Layout results = new VerticalLayout();
	OptionPanel runoptions;
	
public RunAnalysisPanel(Map<String, String> runoptions) {
	this.runoptions = new OptionPanel(runoptions);
	cb_selectontologies.setItems(registry.getOntologies());
	addComponent(LabelManager.labelH2("Select Ontologies for the analysis"));
	addComponent(cb_selectontologies);
	addComponent(this.runoptions);
	addComponent(rac);
	addComponent(results);
}


public void addClickListener(Button.ClickListener l) {
	rac.addClickListener(l);
}


public Set<String> getSelectedItems() {
	return cb_selectontologies.getSelectedItems();
}


public Optional<String> getRunoption(String option) {
	return runoptions.getRunoption(option);
}


public void addResult(Component c) {
	results.removeAllComponents();
	results.addComponent(c);
}
}
