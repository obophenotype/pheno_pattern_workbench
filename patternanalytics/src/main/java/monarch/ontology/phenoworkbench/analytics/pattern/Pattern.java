package monarch.ontology.phenoworkbench.analytics.pattern;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;

import java.util.Objects;

public class Pattern extends PatternClass {
    private final OWLClassExpression definiton;
    private final boolean definedclass;
    private PatternGrammar grammar = new PatternGrammar("none");
    private String patternstring= "Not given";

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pattern)) return false;
        if (!super.equals(o)) return false;
        Pattern pattern = (Pattern) o;
        return definedclass == pattern.definedclass &&
                Objects.equals(definiton, pattern.definiton) &&
                Objects.equals(grammar, pattern.grammar) &&
                Objects.equals(patternstring, pattern.patternstring);
    }

    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), definiton, definedclass, grammar, patternstring);
    }

    public Pattern(OWLClass c, OWLClassExpression definition, boolean definedclass) {
        super(c);
        this.definiton = definition;
        this.definedclass = definedclass;
    }

    public String getPatternString() {
        return patternstring;
    }

    public void setPatternString(String patternstring) {
        this.patternstring = patternstring;
    }

    public OWLClassExpression getDefiniton() {
        return definiton;
    }

    public PatternGrammar getGrammar() {
        return grammar;
    }

    public void setGrammar(PatternGrammar grammar) {
        this.grammar = grammar;
    }

    public boolean isDefinedclass() {
        return definedclass;
    }

    @Override
    public String toString() {
        return getLabel()+": "+getPatternString();
    }
}
