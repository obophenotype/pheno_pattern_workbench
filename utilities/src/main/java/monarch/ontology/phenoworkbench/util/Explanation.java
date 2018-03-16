package monarch.ontology.phenoworkbench.util;

import org.semanticweb.owlapi.model.OWLAxiom;

import java.util.HashSet;
import java.util.Set;

public class Explanation {
    private final org.semanticweb.owl.explanation.api.Explanation<OWLAxiom> e;

    public Explanation(org.semanticweb.owl.explanation.api.Explanation<OWLAxiom> e) {
        this.e = e;
    }

    @Override
    public String toString() {
        return "Explanation{!}";
    }

    public Set<OWLAxiom> getAxioms() {
        return new HashSet<>(e.getAxioms());
    }
}
