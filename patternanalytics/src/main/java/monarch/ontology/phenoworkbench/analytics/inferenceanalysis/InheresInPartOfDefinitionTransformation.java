package monarch.ontology.phenoworkbench.analytics.inferenceanalysis;

import monarch.ontology.phenoworkbench.util.RenderManager;
import org.semanticweb.owlapi.model.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class InheresInPartOfDefinitionTransformation extends PhenotypeDefinitionTransformer {

    public InheresInPartOfDefinitionTransformation(RenderManager renderManager, Set<OWLClass> pato, Set<OWLClass> phenotypes) {
        super(renderManager, pato, phenotypes);
    }

    @Override
    protected OWLClassExpression transformClassExpression(OWLClassExpression phenotype) {
        if (phenotype instanceof OWLObjectIntersectionOf) {
            /*
            QinE Pattern
             */
            return createExpression(((OWLObjectIntersectionOf) phenotype));
        } else if (phenotype instanceof OWLObjectSomeValuesFrom) {
            /*
            Checking for has-part patterns
             */
            OWLObjectSomeValuesFrom cephen = ((OWLObjectSomeValuesFrom) phenotype);
            if (!cephen.getProperty().isAnonymous()) {
                if (cephen.getProperty().asOWLObjectProperty().getIRI().toString().equals("http://purl.obolibrary.org/obo/BFO_0000051")) {
                    if (cephen.getFiller() instanceof OWLObjectIntersectionOf)
                        return df().getOWLObjectSomeValuesFrom(cephen.getProperty(), createExpression((OWLObjectIntersectionOf) cephen.getFiller()));
                }
            }
        }
        return phenotype;
    }

    private OWLClassExpression createExpression(OWLObjectIntersectionOf cephen) {

        Set<OWLClassExpression> newIntersection = new HashSet<>();

        for (OWLClassExpression ce : cephen.getOperands()) {
            OWLObjectSomeValuesFrom ois = (OWLObjectSomeValuesFrom) ce;
            if (ce instanceof OWLObjectSomeValuesFrom) {
                if (!ois.getProperty().isAnonymous()) {
                    System.out.println("A");
                    OWLObjectProperty op_in = ois.getProperty().asOWLObjectProperty();
                    if (op_in.equals(PhenoEntities.op_inheresin)) {
                        System.out.println("B");
                        OWLClassExpression filler = ois.getFiller();
                        if (filler instanceof OWLObjectSomeValuesFrom) {
                            System.out.println("C");
                            OWLObjectSomeValuesFrom poexp = (OWLObjectSomeValuesFrom) filler;
                            if (!poexp.getProperty().isAnonymous()) {
                                System.out.println("D");
                                OWLObjectProperty po = poexp.getProperty().asOWLObjectProperty();
                                if (po.equals(PhenoEntities.op_partof)) {
                                    System.out.println("E");
                                    newIntersection.add(df().getOWLObjectSomeValuesFrom(PhenoEntities.op_inheresinpartof, poexp.getFiller()));
                                }
                            }

                        } else if (filler instanceof OWLObjectIntersectionOf) {
                            OWLObjectIntersectionOf oio = (OWLObjectIntersectionOf)filler;
                            Set<OWLClassExpression> inheresIntersection = new HashSet<>();
                            for(OWLClassExpression ceoio:oio.getOperands()) {
                                if(ceoio instanceof OWLObjectSomeValuesFrom) {
                                    System.out.println("C");
                                    OWLObjectSomeValuesFrom poexp = (OWLObjectSomeValuesFrom) filler;
                                    if (!poexp.getProperty().isAnonymous()) {
                                        System.out.println("D");
                                        OWLObjectProperty po = poexp.getProperty().asOWLObjectProperty();
                                        if (po.equals(PhenoEntities.op_partof)) {
                                            System.out.println("E");
                                            newIntersection.add(df().getOWLObjectSomeValuesFrom(PhenoEntities.op_inheresinpartof, poexp.getFiller()));
                                        } else {
                                            inheresIntersection.add(ceoio);
                                        }
                                    } else {
                                        inheresIntersection.add(ceoio);
                                    }
                                } else {
                                    inheresIntersection.add(ceoio);
                                }
                            }
                            newIntersection.add(df().getOWLObjectSomeValuesFrom(PhenoEntities.op_inheresinpartof, df().getOWLObjectIntersectionOf(inheresIntersection)));
                        }
                    }
                }
            } else {
                newIntersection.add(ce);
            }
        }
        return df().getOWLObjectIntersectionOf(newIntersection);
    }

    private OWLClassExpression rewriteInheresInPartOf(OWLClassExpression filler) {

        return filler;
    }


}
