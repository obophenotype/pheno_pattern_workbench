package monarch.ontology.phenoworkbench.util;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ExplanationAnalyser {
    List<String> getReport(int indendationlevel);
    List<String> getRenderedAxiomList();
}
