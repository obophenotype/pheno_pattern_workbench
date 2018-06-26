package monarch.ontology.phenoworkbench.analytics.inferenceanalysis;

import monarch.ontology.phenoworkbench.util.RenderManager;
import org.semanticweb.owlapi.model.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TowardsDefinitionTransformation extends PhenotypeDefinitionTransformer {
    private Map<OWLClass, OWLObjectProperty> relationscreated = new HashMap<>();
    private Set<OWLClass> qualityBlacklist = new HashSet<>();

    public TowardsDefinitionTransformation(RenderManager renderManager, Set<OWLClass> pato,Set<OWLClass> phenotypes) {
        super(renderManager, pato,phenotypes);
        qualityBlacklist.add(df().getOWLClass(IRI.create("http://purl.obolibrary.org/obo/PATO_0000460")));
        qualityBlacklist.add(df().getOWLClass(IRI.create("http://purl.obolibrary.org/obo/PATO_0000389")));
        qualityBlacklist.add(df().getOWLClass(IRI.create("http://purl.obolibrary.org/obo/PATO_0000618")));
        qualityBlacklist.add(df().getOWLClass(IRI.create("http://purl.obolibrary.org/obo/PATO_0001793")));
        qualityBlacklist.add(df().getOWLClass(IRI.create("http://purl.obolibrary.org/obo/PATO_0002118")));
        qualityBlacklist.add(df().getOWLClass(IRI.create("http://purl.obolibrary.org/obo/PATO_0000634")));
        qualityBlacklist.add(df().getOWLClass(IRI.create("http://purl.obolibrary.org/obo/PATO_0000639")));
        qualityBlacklist.add(df().getOWLClass(IRI.create("http://purl.obolibrary.org/obo/PATO_0001191")));
        qualityBlacklist.add(df().getOWLClass(IRI.create("http://purl.obolibrary.org/obo/PATO_0001632")));
        qualityBlacklist.add(df().getOWLClass(IRI.create("http://purl.obolibrary.org/obo/PATO_0001863")));

    }

    @Override
    protected OWLClassExpression transformClassExpression(OWLClassExpression phenotype) {
        OWLObjectProperty towards = df().getOWLObjectProperty(IRI.create("http://purl.obolibrary.org/obo/RO_0002503"));
        if (!phenotype.getSignature().contains(towards)) {
            return phenotype;
        }
        if (phenotype instanceof OWLObjectIntersectionOf) {
            /*
            QinE Pattern
             */
            return createTowardsExpression(((OWLObjectIntersectionOf) phenotype));
        } else if (phenotype instanceof OWLObjectSomeValuesFrom) {
            /*
            Checking for has-part patterns
             */
            OWLObjectSomeValuesFrom cephen = ((OWLObjectSomeValuesFrom) phenotype);
            if (!cephen.getProperty().isAnonymous()) {
                if (cephen.getProperty().asOWLObjectProperty().getIRI().toString().equals("http://purl.obolibrary.org/obo/BFO_0000051")) {
                    if (cephen.getFiller() instanceof OWLObjectIntersectionOf)
                        return df().getOWLObjectSomeValuesFrom(cephen.getProperty(), createTowardsExpression((OWLObjectIntersectionOf) cephen.getFiller()));
                }
            }
        }
        return phenotype;
    }

    private OWLClassExpression createTowardsExpression(OWLObjectIntersectionOf cephen) {

        Set<OWLClassExpression> newIntersection = new HashSet<>();
        OWLClass quality_class = null;
        OWLClassExpression inheres_class = null;
        OWLClass towards_class = null;
        OWLObjectProperty inheres = null;

        for (OWLClassExpression ce : cephen.getOperands()) {
            if (ce instanceof OWLObjectSomeValuesFrom) {
                OWLObjectSomeValuesFrom ceop = (OWLObjectSomeValuesFrom) ce;
                if (!ceop.getProperty().isAnonymous()) {
                    OWLObjectProperty p = ceop.getProperty().asOWLObjectProperty();
                    String iri = p.getIRI().toString();
                    if (iri.equals("http://purl.obolibrary.org/obo/RO_0000052") || iri.equals("http://purl.obolibrary.org/obo/RO_0002314")) {
                        //inheres in or inheres in part of
                        if (inheres_class != null) {
                            System.out.println("Too many inheres in relations, aborting...");
                            return cephen;

                        } else if (ceop.getFiller().isClassExpressionLiteral()) {
                            inheres_class = ceop.getFiller();
                            inheres = p;
                        } else if(ceop.getFiller() instanceof OWLObjectIntersectionOf){
                            inheres_class = ceop.getFiller();
                            inheres = p;
                        } else {
                            System.out.println("Unknown pattern for the Q inheres in expression "+ getRenderManager().renderForMarkdown(ceop.getFiller()));
                        }
                    } else if (iri.equals("http://purl.obolibrary.org/obo/RO_0002503")) {
//towards
                        if (towards_class != null) {
                            System.out.println("Too many towards relations, aborting...");
                            return cephen;

                        } else if (ceop.getFiller().isClassExpressionLiteral()) {
                            towards_class = ceop.getFiller().asOWLClass();
                        }
                    } else {
                        newIntersection.add(ce);
                    }
                }

            } else if (ce instanceof OWLClass) {
                if (qualityBlacklist.contains(ce)) {
                    newIntersection.add(ce);
                    continue;
                } else if (isPhenotype((OWLClass) ce)) {
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
        if (quality_class != null && towards_class != null) {
            OWLClass cl_pheno = df().getOWLClass(IRI.create("http://purl.obolibrary.org/obo/UPHENO_0001001"));
            OWLObjectProperty op_inherepartof = df().getOWLObjectProperty(IRI.create("http://purl.obolibrary.org/obo/RO_0002314"));
            OWLObjectProperty qualityRelation = createQualityRelation(quality_class);
            if (inheres_class != null) {
                if(inheres_class instanceof OWLClass) {
                    newIntersection.add(df().getOWLObjectSomeValuesFrom(inheres, df().getOWLObjectIntersectionOf(inheres_class, df().getOWLObjectSomeValuesFrom(qualityRelation, towards_class))));
                } else if( inheres_class instanceof OWLObjectIntersectionOf) {
                    OWLObjectIntersectionOf inheresclass = (OWLObjectIntersectionOf)inheres_class;
                    Set<OWLClassExpression> exps = new HashSet<>(inheresclass.getOperands());
                    exps.add(df().getOWLObjectSomeValuesFrom(qualityRelation, towards_class));
                    newIntersection.add(df().getOWLObjectSomeValuesFrom(inheres, df().getOWLObjectIntersectionOf(exps)));
                } else {
                    System.out.println("TOWARDS PATTERN WARNING: NEITHER CLASS NOR INTERSECTION IN INHERES");
                }
            } else {
                newIntersection.add(df().getOWLObjectSomeValuesFrom(op_inherepartof, df().getOWLObjectIntersectionOf(cl_pheno, df().getOWLObjectSomeValuesFrom(qualityRelation, towards_class))));
            }
        } else {
            System.out.println("Not a valid expression (either now quality class or towards relation)");
            return cephen;
        }
        return df().getOWLObjectIntersectionOf(newIntersection);
    }

    private OWLObjectProperty createQualityRelation(OWLClass quality_class) {
        String name = "qrel_" + getRenderManager().getLabel(quality_class).toLowerCase().replaceAll("\\W", "_").replaceAll("[^a-z_]", "");
        OWLObjectProperty rel = df().getOWLObjectProperty(IRI.create("http://purl.obolibrary.org/obo/" + name));
        relationscreated.put(quality_class, rel);
        return rel;
    }

    public Map<OWLClass, OWLObjectProperty> getNewEntitiesCreated() {
        return relationscreated;
    }
}
