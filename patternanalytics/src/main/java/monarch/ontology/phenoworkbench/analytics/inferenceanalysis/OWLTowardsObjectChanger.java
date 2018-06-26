package monarch.ontology.phenoworkbench.analytics.inferenceanalysis;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;

import java.util.Set;

public class OWLTowardsObjectChanger extends OWLObjectChanger {
    /**
     * Creates an object duplicator that duplicates objects using the specified
     * data factory.
     *
     * @param dataFactory The data factory to be used for the duplication.
     */
    public OWLTowardsObjectChanger(OWLDataFactory dataFactory) {
        super(dataFactory);
    }

    @Override
    public OWLClassExpression visit(OWLObjectSomeValuesFrom ce) {
        return df.getOWLObjectSomeValuesFrom(duplicate(ce.getProperty()),
                duplicate(ce.getFiller()));
    }

    @Override
    public OWLClassExpression visit(OWLObjectIntersectionOf ce) {
        Set<OWLClassExpression> ops = duplicateSet(ce.getOperands());
        return df.getOWLObjectIntersectionOf(ops);
    }
}
