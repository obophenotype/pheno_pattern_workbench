package monarch.ontology.phenoworkbench.util;

import org.semanticweb.owlapi.model.OWLAxiom;

import java.util.HashSet;
import java.util.Set;

public class Explanation {
    private final org.semanticweb.owl.explanation.api.Explanation<OWLAxiom> e;
    private final String s;

    public Explanation(org.semanticweb.owl.explanation.api.Explanation<OWLAxiom> e) {
        this.e = e;
        RenderManager rm = RenderManager.getInstance();
        StringBuilder sb = new StringBuilder();
        for(OWLAxiom ax:e.getAxioms()) {
            sb.append(rm.renderManchester(ax)+"/n");
        }
        s = sb.toString();
    }

    @Override
    public String toString() {
        return s;
    }

    public Set<OWLAxiom> getAxioms() {
        return new HashSet<>(e.getAxioms());
    }
}
