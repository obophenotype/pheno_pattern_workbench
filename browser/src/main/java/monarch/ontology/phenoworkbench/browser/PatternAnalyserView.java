package monarch.ontology.phenoworkbench.browser;

import java.util.Set;

import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBoxGroup;
import com.vaadin.ui.VerticalLayout;

public class PatternAnalyserView extends VerticalLayout{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8440240868260139938L;
	
	OntologyRegistry registry = new OntologyRegistry();
	CheckBoxGroup<String> cb_selectontologies = new CheckBoxGroup<>("Select Ontologies");
	Button bt_runanalysis = new Button("Run analysis");
	

	public PatternAnalyserView() {
		final VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);
		layout.setSpacing(true);
		addComponent(layout);

		cb_selectontologies.setItems(registry.getOntologies());
		bt_runanalysis.addClickListener(x->runAnalysis(cb_selectontologies.getSelectedItems()));
		
		layout.addComponent(LabelManager.labelH1("Phenotype Pattern Analysis"));
		layout.addComponent(LabelManager.labelH2("Select Ontologies for the analysis"));
		layout.addComponent(cb_selectontologies);
		layout.addComponent(bt_runanalysis);
		
	}
	
	private void runAnalysis(Set<String> selectedItems) {
		selectedItems.forEach(System.out::println);
	}
}
