package monarch.ontology.phenoworkbench.analytics.inferenceanalysis;

import monarch.ontology.phenoworkbench.util.RenderManager;
import org.semanticweb.owlapi.model.*;

import java.util.HashSet;
import java.util.Set;

public class HasQualityDefinitionTransformation extends PhenotypeDefinitionTransformer {

    public HasQualityDefinitionTransformation(RenderManager renderManager, Set<OWLClass> pato, Set<OWLClass> phenotypes) {
        super(renderManager, pato, phenotypes);
    }

    @Override
    protected OWLClassExpression transformClassExpression(OWLClassExpression phenotype) {
        if (phenotype instanceof OWLObjectIntersectionOf) {
            /*
            QinE Pattern
             */
            return createQualityExpression(((OWLObjectIntersectionOf) phenotype));
        } else if (phenotype instanceof OWLObjectSomeValuesFrom) {
            /*
            Checking for has-part patterns
             */
            OWLObjectSomeValuesFrom cephen = ((OWLObjectSomeValuesFrom) phenotype);
            if (!cephen.getProperty().isAnonymous()) {
                if (cephen.getProperty().asOWLObjectProperty().getIRI().toString().equals("http://purl.obolibrary.org/obo/BFO_0000051")) {
                    if (cephen.getFiller() instanceof OWLObjectIntersectionOf)
                        return df().getOWLObjectSomeValuesFrom(cephen.getProperty(), createQualityExpression((OWLObjectIntersectionOf) cephen.getFiller()));
                }
            }
        }
        return phenotype;
    }

    private OWLClassExpression createQualityExpression(OWLObjectIntersectionOf cephen) {

        Set<OWLClassExpression> newIntersection = new HashSet<>();
        OWLClass quality_class = null;

        for (OWLClassExpression ce : cephen.getOperands()) {
            if (ce instanceof OWLClass) {
                if (isPhenotype((OWLClass) ce)) {
                    newIntersection.add(ce);
                    continue;
                } else if (quality_class != null) {
                    System.out.println("Too many quality classes, aborting: " + ((OWLClass) ce).getIRI() + " .... " + getRenderManager().renderForMarkdown(cephen));
                    return cephen;
                } else if (!containsAtLeastOnePatoTerm(ce)) {
                    System.out.println("Non PATO quality in place where there should be one: " + ((OWLClass) ce).getIRI() + " .... " + getRenderManager().renderForMarkdown(cephen));
                    return cephen;
                } else {
                    quality_class = ce.asOWLClass();
                }

            } else {
                newIntersection.add(ce);
            }
        }
        if (quality_class != null) {
            OWLObjectProperty qualityRelation = df().getOWLObjectProperty(IRI.create("http://purl.obolibrary.org/obo/qrel_has_quality"));
            newIntersection.add(df().getOWLObjectSomeValuesFrom(qualityRelation, quality_class));
        } else {
            System.out.println("Not a valid expression (either no quality class or towards relation)" + " .... " + getRenderManager().renderForMarkdown(cephen));
            return cephen;
        }
        return df().getOWLObjectIntersectionOf(newIntersection);
    }


}
