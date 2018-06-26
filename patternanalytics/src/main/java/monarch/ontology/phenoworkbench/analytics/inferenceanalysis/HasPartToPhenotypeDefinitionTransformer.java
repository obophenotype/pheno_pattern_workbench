package monarch.ontology.phenoworkbench.analytics.inferenceanalysis;

import monarch.ontology.phenoworkbench.util.RenderManager;
import org.semanticweb.owlapi.model.*;

import java.util.HashSet;
import java.util.Set;

public class HasPartToPhenotypeDefinitionTransformer extends PhenotypeDefinitionTransformer {

    public HasPartToPhenotypeDefinitionTransformer(RenderManager renderManager, Set<OWLClass> pato,Set<OWLClass> phenotypes) {
        super(renderManager, pato,phenotypes);
    }

    @Override
    protected OWLClassExpression transformClassExpression(OWLClassExpression definiton) {
        return hasPartToPhenotype(definiton);
    }


    private OWLClassExpression hasPartToPhenotype(OWLClassExpression cein) {
        if (cein instanceof OWLObjectSomeValuesFrom) {
            OWLObjectSomeValuesFrom ce = (OWLObjectSomeValuesFrom) cein;
            if (!ce.getProperty().isAnonymous()) {
                if (ce.getProperty().asOWLObjectProperty().getIRI().toString().equals("http://purl.obolibrary.org/obo/BFO_0000051")) {
                    Set<OWLClassExpression> expressions = new HashSet<>();
                    if(ce.getFiller() instanceof OWLObjectIntersectionOf) {
                        OWLObjectIntersectionOf ois = (OWLObjectIntersectionOf) ce.getFiller();
                        expressions.addAll(ois.getOperands());
                    } else {
                        expressions.add(ce.getFiller());
                    }
                    expressions.add(df().getOWLClass(IRI.create("http://purl.obolibrary.org/obo/UPHENO_0001001")));
                    return df().getOWLObjectIntersectionOf(expressions);
                }
            }
        }
        return cein;

    }


}
