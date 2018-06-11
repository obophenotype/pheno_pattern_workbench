package monarch.ontology.phenoworkbench.util;

import java.util.HashSet;
import java.util.Set;

public class OntologyEntry {
    private final String oid;
    private final String iri;
    private final Set<String> roots = new HashSet<>();
    public OntologyEntry(String oid, String iri) {
        this.oid = oid;
        this.iri = iri;
    }

    public void addRootClassesOfInterest(Set<String> roots) {
        this.getRoots().addAll(roots);
    }

    public String getOid() {
        return oid;
    }

    public String getIri() {
        return iri;
    }

    public Set<String> getRoots() {
        return roots;
    }

    public String toString() {
        return oid+" ("+iri+")";
    }
}
