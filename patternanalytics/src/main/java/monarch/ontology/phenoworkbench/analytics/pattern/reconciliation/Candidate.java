package monarch.ontology.phenoworkbench.analytics.pattern.reconciliation;

import monarch.ontology.phenoworkbench.analytics.pattern.generation.OntologyClass;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Candidate {

    private final Set<OntologyClass> candidates = new HashSet<>();

    public String getLabel() {
        String label = "";
        for(OntologyClass oc: getCandidates()) {
            label+=oc.getLabel()+" ";
        }
        return label.trim();
    }

    public Set<OntologyClass> getCandidates() {
        return candidates;
    }

    public void addOntologyClass(OntologyClass c) {
        candidates.add(c);
    }

    public void removeOntologyClass(OntologyClass c) {
        candidates.remove(c);
    }

    public void addOntologyClasses(Collection<OntologyClass> c) {
        candidates.addAll(c);
    }
}
