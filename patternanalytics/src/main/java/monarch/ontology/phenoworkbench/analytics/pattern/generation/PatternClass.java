package monarch.ontology.phenoworkbench.analytics.pattern.generation;

import monarch.ontology.phenoworkbench.util.DefinedClass;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;

public class PatternClass extends DefinedClass {

    PatternClass(OWLClass c, OWLClassExpression definition) {
        super(c,definition);
    }

    @Override
    public String toString() {
        return getLabel()+": "+getPatternString();
    }
}
