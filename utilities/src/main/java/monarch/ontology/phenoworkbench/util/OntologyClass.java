package monarch.ontology.phenoworkbench.util;

import org.semanticweb.owlapi.model.OWLClass;

import java.util.*;

public class OntologyClass {
    private final OWLClass c;
    private String label;
    private boolean deprecated = false;
    private String description = "No description";
    private String iri;
    private Map<String,Set<String>> annotations = new HashMap<>();
    private Node node;

    public OntologyClass(OWLClass c) {
        this.c = c;
        this.setNode(new Node(new HashSet<>(Collections.singleton(this)),this));
        this.label = c.toString();
        this.iri = c.getIRI().toString();
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

    public String getIri() {
        return iri;
    }

    public void addMetadata(String key, String value) {
        if(!annotations.containsKey(key)) {
            annotations.put(key,new HashSet<>());
        }
        annotations.get(key).add(value);
    }

    public Map<String,Set<String>> getMetadata() {
        return annotations;
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

    public Node getNode() {
        return node;
    }

    public void setNode(Node n) {
        this.node = n;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public void setDeprecated(boolean deprecated) {
        this.deprecated = deprecated;
    }
}
