package monarch.ontology.phenoworkbench.util;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLEntity;

import java.util.HashSet;
import java.util.Set;

public class TermCluster {
    Set<IRI> terms = new HashSet<>();
    public void addTerm(IRI iri) {
        terms.add(iri);
    }
}
