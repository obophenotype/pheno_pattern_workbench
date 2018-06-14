package monarch.ontology.phenoworkbench.analytics.inferenceanalysis;

import org.semanticweb.owlapi.model.OWLDataFactory;

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
}
