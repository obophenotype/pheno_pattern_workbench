package monarch.ontology.phenoworkbench.unionanalytics;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ExplanationAnalyser {
    List<String> getReport(HashMap<OWLAxiom, Integer> countaxiomsinannotations, Map<OWLAxiom, Set<IRI>> allAxiomsAcrossOntologies);
}
