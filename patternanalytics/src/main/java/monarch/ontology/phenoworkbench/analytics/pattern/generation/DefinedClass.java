package monarch.ontology.phenoworkbench.analytics.pattern.generation;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;

import java.util.Objects;

public class DefinedClass extends OntologyClass {
    private final OWLClassExpression definiton;
    private PatternGrammar grammar = new PatternGrammar("none");

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DefinedClass)) return false;
        if (!super.equals(o)) return false;
        DefinedClass that = (DefinedClass) o;
        return Objects.equals(definiton, that.definiton) &&
                Objects.equals(grammar, that.grammar) &&
                Objects.equals(patternstring, that.patternstring);
    }

    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), definiton, grammar, patternstring);
    }

    private String patternstring= "Definition not loaded";


    DefinedClass(OWLClass c, OWLClassExpression definition) {
        super(c);
        this.definiton = definition;
    }

    public final String getPatternString() {
        return patternstring;
    }

    public final void setPatternString(String patternstring) {
        this.patternstring = patternstring;
    }

    public final OWLClassExpression getDefiniton() {
        return definiton;
    }

    public final PatternGrammar getGrammar() {
        return grammar;
    }

    public final void setGrammar(PatternGrammar grammar) {
        this.grammar = grammar;
    }

    @Override
    public String toString() {
        return getLabel()+": "+getPatternString();
    }


}
