package monarch.ontology.phenoworkbench.browser.basic;

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
	private final VerticalLayout vl_addtional_options = new VerticalLayout();
	RunAnalysisComponent rac = new RunAnalysisComponent();
	Layout results = new VerticalLayout();
	OptionPanel runoptions;
	
public RunAnalysisPanel(Map<String, String> runoptions) {
	setMargin(false);
	setSpacing(true);
	setWidth("100%");
	results.setWidth("100%");
	this.runoptions = new OptionPanel(runoptions);
	cb_selectontologies.setItems(registry.getOntologies());
	vl_addtional_options.setMargin(false);
	vl_addtional_options.setSpacing(false);
	addComponent(LabelManager.labelH2("Select Ontologies for the analysis"));
	addComponent(cb_selectontologies);
	addComponent(this.runoptions);
	addComponent(vl_addtional_options);
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

public void clearResults() {
	results.removeAllComponents();
}

public void addResult(Component c, boolean clear) {
	if(clear) clearResults();
	results.addComponent(c);
}

public void addAdditionalSettingsComponent(Component c, boolean clear) {
	if(clear) clearResults();
	vl_addtional_options.addComponent(c);
}
}
