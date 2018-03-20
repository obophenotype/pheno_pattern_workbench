package monarch.ontology.phenoworkbench.analytics.pattern.generation;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;

public class DefinedClass extends OntologyClass {
    private final OWLClassExpression definiton;
    private PatternGrammar grammar = new PatternGrammar("none");
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
