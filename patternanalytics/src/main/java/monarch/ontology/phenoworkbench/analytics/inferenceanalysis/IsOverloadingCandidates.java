package monarch.ontology.phenoworkbench.analytics.inferenceanalysis;

import monarch.ontology.phenoworkbench.util.Export;
import monarch.ontology.phenoworkbench.util.Reasoner;
import monarch.ontology.phenoworkbench.util.RenderManager;
import monarch.ontology.phenoworkbench.util.Subsumption;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.dlsyntax.renderer.DLSyntaxObjectRenderer;
import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class IsOverloadingCandidates {

    private static OWLDataFactory df = OWLManager.getOWLDataFactory();
    private static RenderManager renderManager = RenderManager.getInstance();
    private static OWLObjectProperty haspart = df.getOWLObjectProperty(IRI.create("http://purl.obolibrary.org/obo/BFO_0000051"));
    private static OWLObjectProperty iipo = df.getOWLObjectProperty(IRI.create("http://purl.obolibrary.org/obo/RO_0002314"));
    private static OWLObjectProperty ii = df.getOWLObjectProperty(IRI.create("http://purl.obolibrary.org/obo/RO_0000052"));
    private static OWLClass bfo_continuant = df.getOWLClass(IRI.create("http://purl.obolibrary.org/obo/BFO_0000002"));
    private static OWLClass bfo_occurrent = df.getOWLClass(IRI.create("http://purl.obolibrary.org/obo/BFO_0000003"));
    private static Set<OWLClass> continuant_classes = new HashSet<>();
    private static Set<OWLClass> occurrent_classes = new HashSet<>();
    private static Map<OWLClass,OWLClass> asserted_bearers = new HashMap<>();

    public enum Classification {
        Occurent,
        Continuant,
        Other
    }


    public static void main(String[] args) throws OWLOntologyCreationException {
        args = new String[3];
        args[0] = "/data/tmp_pato/ontologies/obo20190313_merged_wbphenotype.owl";
        args[1] = "/data/isaoverloading/";
        args[2] = "http://purl.obolibrary.org/obo/WBPhenotype_0000886";
        File f_o = new File(args[0]);
        File out = new File(args[1]);
        String root = args[2];

        OWLClass root_phenotype = df.getOWLClass(IRI.create(root));
        System.out.println("Loading O: "+f_o.getName());
        OWLOntology o = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(f_o);
        Reasoner r = new Reasoner(o);
        Set<Subsumption> subsumptions = SubsumptionUtils.getAssertedSubsumptions(o);
        System.out.println("Gathering data: "+f_o.getName());
        System.out.println("Currently incomplete! Should look at inferred bearers as well!");
        renderManager.addLabel(o);
        prepare_bearers(o.getAxioms(Imports.INCLUDED));
        List<Map<String, String>> data = new ArrayList<>();
        Set<OWLClass> phenotypeClasses = new HashSet<>(r.getSubclassesOf(root_phenotype,false,true));
        continuant_classes.add(bfo_continuant);
        occurrent_classes.add(bfo_occurrent);


        for (Subsumption ax : subsumptions) {
            OWLClass subc = ax.getSub_c();
            OWLClass superc = ax.getSuper_c();
            if(!phenotypeClasses.contains(superc)||!asserted_bearers.containsKey(superc)||!asserted_bearers.containsKey(subc)) {
                continue;
            }
            Classification cl_sub = getClassification(subc,r);
            Classification cl_super = getClassification(superc,r);
            boolean candidate = (cl_sub!=cl_super);
            Map<String, String> rec = new HashMap<>();
            rec.put("o", f_o.getName());
            rec.put("o_path", f_o.getAbsolutePath());
            rec.put("candidate", candidate+"");
            rec.put("sub", subc.getIRI().toString());
            rec.put("super", superc.getIRI().toString());
            rec.put("sub_label", renderManager.render(subc));
            rec.put("super_label", renderManager.render(superc));
            rec.put("sub_classification", cl_sub.name());
            rec.put("super_classification", cl_super.name());
            rec.put("super_bearer", getBearer(superc).getIRI().toString());
            rec.put("sub_bearer", getBearer(subc).getIRI().toString());
            rec.put("super_bearer_label", renderManager.render(getBearer(superc)));
            rec.put("sub_bearer_label", renderManager.render(getBearer(subc)));
            if(candidate) {
                data.add(rec);
            }
        }

        Export.writeCSV(data, new File(out, "isaoverloading_subsumptiondata_" + f_o.getName() + ".csv"));
        System.out.println("Isa-overloading gathering finished for "+f_o.getName());
    }

    private static Classification getClassification(OWLClass super_c, Reasoner r) {
        Set<OWLClass> named_super_class = new HashSet<>(r.getSuperClassesOf(getBearer(super_c),false,true));
        if(!Collections.disjoint(named_super_class, continuant_classes)) {
            return Classification.Continuant;
        } else if(!Collections.disjoint(named_super_class, occurrent_classes)) {
            return Classification.Occurent;
        } else {
            return Classification.Other;
        }
    }

    private static void prepare_bearers(Set<OWLAxiom> axioms) {
        for(OWLAxiom ax:axioms) {
            if(ax instanceof OWLEquivalentClassesAxiom) {
                OWLEquivalentClassesAxiom eq = (OWLEquivalentClassesAxiom) ax;
                Set<OWLClass> named_cls = eq.getNamedClasses();
                OWLClass named = null;
                for(OWLClass n:named_cls) {
                    named = n;
                }
                if (named_cls.size() == 1) {
                    for (OWLClassExpression cein : eq.getClassExpressions()) {
                        if (cein instanceof OWLObjectSomeValuesFrom) {
                            OWLObjectSomeValuesFrom ce = (OWLObjectSomeValuesFrom) cein;
                            if (!ce.getProperty().isAnonymous()) {
                                if (ce.getProperty().asOWLObjectProperty().equals(haspart)) {
                                    if (ce.getFiller() instanceof OWLObjectIntersectionOf) {
                                        OWLObjectIntersectionOf ois = (OWLObjectIntersectionOf) ce.getFiller();
                                        for (OWLClassExpression c_inheres : ois.getOperands()) {
                                            if (c_inheres instanceof OWLObjectSomeValuesFrom) {
                                                OWLObjectSomeValuesFrom ce_inheresin = (OWLObjectSomeValuesFrom) c_inheres;
                                                if (!ce_inheresin.getProperty().isAnonymous()) {
                                                    if (ce_inheresin.getProperty().asOWLObjectProperty().equals(iipo) || ce_inheresin.getProperty().asOWLObjectProperty().equals(ii)) {
                                                        OWLClassExpression cein_filler = ce_inheresin.getFiller();
                                                        if (cein_filler instanceof OWLObjectIntersectionOf) {
                                                            OWLObjectIntersectionOf cein_filler_inter = (OWLObjectIntersectionOf)cein_filler;
                                                            Set<OWLClass> named_bearers = cein_filler_inter.getOperands().stream().filter(OWLClassExpression::isClassExpressionLiteral).map(OWLClassExpression::asOWLClass).collect(Collectors.toSet());
                                                            if(named_bearers.size()==1) {
                                                                OWLClass named_bearer = null;
                                                                for(OWLClass n:named_bearers) {
                                                                    named_bearer = n;
                                                                }
                                                                asserted_bearers.put(named, named_bearer);
                                                            } else {
                                                                System.out.println(renderManager.render(cein_filler)+" has more than one bearer, not processing.");
                                                            }
                                                        } else if (cein_filler instanceof OWLClass) {
                                                            asserted_bearers.put(named, cein_filler.asOWLClass());
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                }
                            }
                        }
                    }
                } else {
                    System.out.println(renderManager.render(ax)+" has more than one named class, not processing.");
                }
            }
        }
    }

    private static OWLClass getBearer(OWLClass c) {
        if(asserted_bearers.containsKey(c)) {
            return asserted_bearers.get(c);
        } else {
            return df.getOWLThing();
        }
    }

}
