package monarch.ontology.phenoworkbench.browser.analytics;

import org.semanticweb.owlapi.model.OWLAxiom;

import java.util.HashSet;
import java.util.Set;

public class Explanation {
    private final Set<OWLAxiom> axioms;

    Explanation(Set<OWLAxiom> axioms) {
        this.axioms = axioms;
    }

    @Override
    public String toString() {
        return "Explanation{!}";
    }

    public Set<OWLAxiom> getAxioms() {
        return new HashSet<>(axioms);
    }
}
