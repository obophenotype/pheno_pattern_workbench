package monarch.ontology.phenoworkbench.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Node {
    Set<OntologyClass> equivalentClasses = new HashSet<>();
    private Set<Node> parents = new HashSet<>();
    private Set<Node> children = new HashSet<>();
    OntologyClass representativeElement;
    public Node(Set<OntologyClass> eq, OntologyClass representative) {
        equivalentClasses.addAll(eq);
        representativeElement = representative;
    }

    public OntologyClass getRepresentativeElement() {
        return representativeElement;
    }

    public final void addChild(Node c) {
        children.add(c);
    }

    public final void addParent(Node c) {
        parents.add(c);
    }

    public final Set<Node> directChildren() {
        return children;
    }


    public final Set<Node> directParents() {
        return parents;
    }

    public final Set<Node> indirectParents() {
        Set<Node> indirect = new HashSet<>();
        allParentsRecursive(this, indirect);
        return indirect;
    }

    public final Set<Node> indirectChildren() {
        Set<Node> indirect = new HashSet<>();
        allChildrenRecursive(this, indirect);
        return indirect;
    }

    private void allParentsRecursive(Node c, Set<Node> indirect) {
        Timer.start("OntologyClass::allParentsRecursive");
        for(Node p:c.directParents()) {
            if(!indirect.contains(p)) {
                indirect.add(p);
                allParentsRecursive(p, indirect);
            }
        }
        Timer.end("OntologyClass::allParentsRecursive");
    }

    private void allChildrenRecursive(Node c, Set<Node> indirect) {
        Timer.start("OntologyClass::allChildrenRecursive");
        for(Node p:c.directChildren())
            if(!indirect.contains(p)) {
                indirect.add(p);
                allChildrenRecursive(p, indirect);
            }
        Timer.end("OntologyClass::allChildrenRecursive");
    }

    public Set<OntologyClass> getEquivalenceGroup() {
        return equivalentClasses;
    }

    public Set<OntologyClass> indirectParentsFlat() {
        return flatten(indirectParents());
    }

    public Set<OntologyClass> indirectChildrenFlat() {
        return flatten(indirectChildren());
    }

    public Set<OntologyClass> directParentsFlat() {
        return flatten(directParents());
    }

    public Set<OntologyClass> directChildrenFlat() {
        return flatten(directChildren());
    }

    public Set<OntologyClass> flatten(Set<Node> nodes) {
        Set<OntologyClass> ip = new HashSet<>();
        nodes.forEach(n->ip.addAll(n.getEquivalenceGroup()));
        return ip;
    }

    @Override
    public String toString() {
        String out = "";
        for(OntologyClass c: getEquivalenceGroup()) {
            String s=c.getLabel();
            if (c instanceof DefinedClass) {
                if (c instanceof PatternClass) {
                    s = "<i>" + s + "</i>";
                } else {
                    s = "<b>" + s + "</b>";
                }
            }
            out+=s+" | ";
        }
        return out.replaceAll("[|][ ]$","");
    }
}