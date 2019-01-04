package monarch.ontology.phenoworkbench.inferencereview.basic;

import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import monarch.ontology.phenoworkbench.analytics.inferenceanalysis.DefinitionImpactRunner;
import monarch.ontology.phenoworkbench.analytics.inferenceanalysis.InferenceAnalyser;
import monarch.ontology.phenoworkbench.uiutils.basic.LabelManager;
import monarch.ontology.phenoworkbench.util.OntologyDebugReport;
import monarch.ontology.phenoworkbench.util.OntologyEntry;
import monarch.ontology.phenoworkbench.util.OntologyRegistry;
import monarch.ontology.phenoworkbench.util.Subsumption;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MainView extends VerticalLayout {

	public MainView() {

		File dir = new File("/data");
		File ont = new File(dir,System.getenv("ONTOLOGY"));
		addComponent(new Label("FILE "+ont+" exists: "+ont.isFile()));
		InferenceAnalyser ia = new InferenceAnalyser(ont,false);
		ia.prepare();
		OntologyDebugReport report = ia.getMarkdownReport();

		OntologyRegistry phenotypeontologies = new OntologyRegistry();

		Set<OntologyEntry> entries = new HashSet<>(); //phenotypeontologies.getOntologies()
		entries.add(new OntologyEntry("test", ont.toURI().toString()));
		Set<OWLClass> phenoclasses = new HashSet<>();
		phenoclasses.add(OWLManager.getOWLDataFactory().getOWLClass(IRI.create(System.getenv("ROOT"))));
		DefinitionImpactRunner p = new DefinitionImpactRunner(entries,phenoclasses,true);
		p.setImports(Imports.EXCLUDED);
		p.runAnalysis();
		Map<Subsumption, String> bnb = p.getComparison().getSub_base_not_bare();
		Label l = LabelManager.htmlLabelFromMarkdown(report.getLines());
		l.setWidth("100%");
		addComponent(l);

		int i = 0;
		for(Subsumption s: bnb.keySet()) {
			addComponent(new InferenceReviewView(s,ia.getExplanations(s,2)));
			i++;
			if(i > 10) {
				break;
			}
		}

	}
	
}
