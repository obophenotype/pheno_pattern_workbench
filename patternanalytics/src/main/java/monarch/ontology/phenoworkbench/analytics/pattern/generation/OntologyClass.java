package monarch.ontology.phenoworkbench.analytics.pattern.generation;

import monarch.ontology.phenoworkbench.util.Timer;
import org.semanticweb.owlapi.model.OWLClass;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class OntologyClass {
    private final OWLClass c;
    private String label;
    private Set<OntologyClass> parents = new HashSet<>();
    private Set<OntologyClass> children = new HashSet<>();

    OntologyClass(OWLClass c) {
        this.c = c;
        this.label = c.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OntologyClass)) return false;
        OntologyClass that = (OntologyClass) o;
        return Objects.equals(c, that.c) &&
                Objects.equals(label, that.label);
    }

    @Override
    public int hashCode() {

        return Objects.hash(c, label);
    }

    public final void addChild(OntologyClass c) {
        children.add(c);
    }

    public final void addParent(OntologyClass c) {
        parents.add(c);
    }

    public final Set<OntologyClass> directChildren() {
        return children;
    }


    public final Set<OntologyClass> directParents() {
       return parents;
    }


    public final OWLClass getOWLClass() {
        return c;
    }

    public final void setLabel(String label) {
        this.label = label;
    }

    public final String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }

    public final Set<OntologyClass> indirectParents() {
      Set<OntologyClass> indirect = new HashSet<>();
      allParentsRecursive(this, indirect);
      return indirect;
    }

    public final Set<OntologyClass> indirectChildren() {
        Set<OntologyClass> indirect = new HashSet<>();
        allChildrenRecursive(this, indirect);
        return indirect;
    }

    private void allParentsRecursive(OntologyClass c, Set<OntologyClass> indirect) {
        Timer.start("OntologyClass::allParentsRecursive");
        for(OntologyClass p:c.directParents()) {
            if(!indirect.contains(p)) {
                indirect.add(p);
                allParentsRecursive(p, indirect);
            }
        }
        Timer.end("OntologyClass::allParentsRecursive");
    }

    private void allChildrenRecursive(OntologyClass c, Set<OntologyClass> indirect) {
        Timer.start("OntologyClass::allChildrenRecursive");
        for(OntologyClass p:c.directChildren()) {
            if(!indirect.contains(p)) {
                indirect.add(p);
                allChildrenRecursive(p, indirect);
            }
        }
        Timer.end("OntologyClass::allChildrenRecursive");
    }
}
