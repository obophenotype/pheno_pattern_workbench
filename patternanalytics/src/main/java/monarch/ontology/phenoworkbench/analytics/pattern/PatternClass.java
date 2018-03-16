package monarch.ontology.phenoworkbench.analytics.pattern;

import org.semanticweb.owlapi.model.OWLClass;

import java.util.Objects;

public class PatternClass {
    private final OWLClass c;
    private String label;
    private PatternGrammar grammar = new PatternGrammar("none");

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PatternClass)) return false;
        PatternClass that = (PatternClass) o;
        return Objects.equals(c, that.c) &&
                Objects.equals(label, that.label) &&
                Objects.equals(grammar, that.grammar);
    }

    @Override
    public int hashCode() {

        return Objects.hash(c, label, grammar);
    }

    public PatternClass(OWLClass c) {
        this.c = c;
        this.label = c.toString();
    }

    public OWLClass getOWLClass() {
        return c;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
