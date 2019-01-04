package monarch.ontology.phenoworkbench.browser.basic;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBoxGroup;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;
import monarch.ontology.phenoworkbench.analytics.subclassredundancy.SubClassRedundancy;
import monarch.ontology.phenoworkbench.uiutils.basic.LabelManager;
import monarch.ontology.phenoworkbench.util.OntologyEntry;
import monarch.ontology.phenoworkbench.util.OntologyRegistry;

public class RunAnalysisPanel extends VerticalLayout {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5663467371143185799L;
	OntologyRegistry registry;
	CheckBoxGroup<OntologyEntry> cb_selectontologies = new CheckBoxGroup<>("Select Ontologies");
	private final VerticalLayout vl_addtional_options = new VerticalLayout();
	RunAnalysisComponent rac = new RunAnalysisComponent();
	VerticalLayout results = new VerticalLayout();
	OptionPanel runoptions;
	
public RunAnalysisPanel(Map<String, String> runoptions) {
	ClassLoader classLoader = RunAnalysisPanel.class.getClassLoader();
	File os = new File(classLoader.getResource("ontologies").getFile());
	File roots = new File(classLoader.getResource("phenotypeclasses").getFile());
	registry =  new OntologyRegistry(os,roots);
	setMargin(false);
	setSpacing(true);
	setWidth("100%");
	results.setWidth("100%");
	results.setMargin(false);
	results.setSpacing(false);
	this.runoptions = new OptionPanel(runoptions);
	List<OntologyEntry> cb = new ArrayList<>(registry.getOntologies());
	Collections.sort(cb, (o1,o2) -> o1.toString().compareTo(o2.toString()));
	cb_selectontologies.setItems(cb);
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


public Set<OntologyEntry> getSelectedItems() {
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


public boolean reDownload() {
	return rac.isRefreshDownload();
}
}
