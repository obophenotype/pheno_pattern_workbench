package monarch.ontology.phenoworkbench.analytics.inferenceanalysis;

import monarch.ontology.phenoworkbench.util.Export;
import monarch.ontology.phenoworkbench.util.RenderManager;
import monarch.ontology.phenoworkbench.util.Subsumption;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.OWLObjectDuplicator;

import java.io.File;
import java.util.*;

public class OntologyImpactSurvey {

    public static OWLDataFactory df = OWLManager.getOWLDataFactory();
    public static RenderManager renderManager = RenderManager.getInstance();

    public static void main(String[] args) throws OWLOntologyCreationException {
        //args = new String[3];
        //args[0] = "/data/pato.owl";
        //args[1] = "/data/corpora/obo20190313/obi.owl";
        //args[2] = "/data/patosurvey/";
        File f_ref_ontology = new File(args[0]);
        File f_o = new File(args[1]);
        String substr_for_identification = args[2];
        File out = new File(args[3]);
        log("Loading "+f_ref_ontology.getName());
        OWLOntology o_ref = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(f_ref_ontology);
        log("Loading " + f_o.getName());
        OWLOntology o = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(f_o);
        OWLReasoner r = new ElkReasonerFactory().createReasoner(o);
        renderManager.addLabel(o_ref);
        renderManager.addLabel(o);

        Set<OWLClass> o_ref_classes = computeReferenceClasses(o_ref,substr_for_identification);
        o_ref_classes.retainAll(o.getClassesInSignature(Imports.INCLUDED));

        OWLOntology o_ref_o_classes_to_thing = createOWithoutRefOLinks(o, o_ref_classes);
        OWLReasoner r_ref_o_classes_to_thing = new ElkReasonerFactory().createReasoner(o_ref_o_classes_to_thing);

        Set<Subsumption> subs = SubsumptionUtils.getSubsumptions(r,r.getRootOntology(),false);
        Set<Subsumption> subs_wo_ref = SubsumptionUtils.getSubsumptions(r_ref_o_classes_to_thing,r_ref_o_classes_to_thing.getRootOntology(),false);




        Set<OWLAxiom> non_ref_ontology_axioms = new HashSet<>(o.getAxioms(Imports.INCLUDED));
        non_ref_ontology_axioms.removeAll(o_ref.getAxioms(Imports.INCLUDED));


        List<Map<String, String>> data_axiom = new ArrayList<>();
        List<Map<String, String>> data_expressions = new ArrayList<>();
        List<Map<String, String>> data_class = new ArrayList<>();
        List<Map<String, String>> data_subs = new ArrayList<>();

        log("Extracting data about subsumptions");
        for(Subsumption s:subs) {
            if(!subs_wo_ref.contains(s)) {
                Map<String, String> rec = new HashMap<>();
                rec.put("sub",s.getSub_c().getIRI().toString());
                rec.put("super",s.getSuper_c().getIRI().toString());
                rec.put("sub_label",s.getSub_c().getIRI().toString());
                rec.put("super_label",s.getSuper_c().getIRI().toString());
                data_subs.add(rec);
            }
        }

        log("Extracting data about classes");


        for (OWLClass p : o_ref_classes) {
            Map<String, String> rec = new HashMap<>();
            rec.put("iri", p.getIRI().toString());
            rec.put("label",renderManager.getLabel(p));
            rec.put("o_path", f_o.getAbsolutePath());
            rec.put("o", f_o.getName());
            rec.put("impact",getImpact(p,non_ref_ontology_axioms,r,o_ref_classes)+"");
            data_class.add(rec);
        }
        log("Extracting data about axioms ("+o.getAxiomCount(Imports.INCLUDED)+" axioms, "+o_ref_classes.size()+" classes)");
        int total = o.getAxiomCount(Imports.INCLUDED);
        int progress = 0;
        int breaks = total/10;
        for (OWLAxiom ax : o.getAxioms(Imports.INCLUDED)) {
            progress++;
            if(total>100) {
                if (progress % breaks == 0) {
                    log(progress + " out of " + total);
                }
            }
            for (OWLClass p : ax.getClassesInSignature()) {
                if (o_ref_classes.contains(p)) {
                    String axiomcode = ax.hashCode() + "";
                    String axiomtype = ax.getAxiomType().getName();
                    Map<String, String> rec = new HashMap<>();
                    rec.put("axiomid", axiomcode);
                    rec.put("axiom_incl","unknown");
                    rec.put("axiomtype", axiomtype);
                    rec.put("o_path", f_o.getAbsolutePath());
                    rec.put("o", f_o.getName());
                    rec.put("iri", p.getIRI().toString());
                    boolean pat_only = referenceOntologyOnly(ax.getClassesInSignature(),o_ref_classes);
                    rec.put("ref_ontology_only", pat_only+"");
                    data_axiom.add(rec);
                    for (OWLClassExpression ce : ax.getNestedClassExpressions()) {
                        if (ce.containsEntityInSignature(p)) {
                            String expressioncode = ax.hashCode() + "";
                            Map<String, String> rec_exp = new HashMap<>();
                            rec_exp.put("axiomid", axiomcode);
                            rec_exp.put("axiom_incl","unknown");
                            rec_exp.put("expressionid", expressioncode);
                            rec_exp.put("expressiontype", ce.getClassExpressionType().getName());
                            rec_exp.put("o_path", f_o.getAbsolutePath());
                            rec_exp.put("o", f_o.getName());
                            rec_exp.put("iri", p.getIRI().toString());
                            data_expressions.add(rec_exp);
                            if(ce instanceof OWLQuantifiedObjectRestriction) {
                                OWLQuantifiedObjectRestriction cer = (OWLQuantifiedObjectRestriction)ce;
                                if(cer.getFiller().asConjunctSet().contains(p)) {
                                    String property = cer.getProperty().isAnonymous() ? cer.getProperty().toString() : cer.getProperty().asOWLObjectProperty().getIRI().toString();
                                    rec_exp.put("p", property);
                                }
                            }
                        }
                    }
                }
            }
        }
        Export.writeCSV(data_axiom, new File(out, f_ref_ontology.getName()+"_ontologysurvey_axiomdata_" + f_o.getName() + ".csv"));
        Export.writeCSV(data_expressions, new File(out, f_ref_ontology.getName()+"_ontologysurvey_expressiondata_" + f_o.getName() + ".csv"));
        Export.writeCSV(data_class, new File(out, f_ref_ontology.getName()+"_ontologysurvey_classdata_" + f_o.getName() + ".csv"));
        Export.writeCSV(data_subs, new File(out, f_ref_ontology.getName()+"_ontologysurvey_subsumptiondata_" + f_o.getName() + ".csv"));
    }

    /*
    This method creates a version of the ontology that replaces all occurrences of the reference ontology terms with OWL thing. This accurately measures their impact on reasoning
     */
    private static OWLOntology createOWithoutRefOLinks(OWLOntology o, Set<OWLClass> o_ref_signature) throws OWLOntologyCreationException {
        Set<OWLAxiom> axioms = new HashSet<>();
        for(OWLAxiom ax:o.getAxioms(Imports.INCLUDED)) {
            boolean changed = false;
            if(ax instanceof OWLEquivalentClassesAxiom) {
                Set<OWLClassExpression> expressions = new HashSet<>();
                for(OWLClassExpression ce:((OWLEquivalentClassesAxiom) ax).getClassExpressions()) {
                    if(ce.isClassExpressionLiteral()) {
                        expressions.add(ce);
                    } else if (containsReferenceClass(ce, o_ref_signature)) {

                        changed = true;
                    } else {
                        expressions.add(ce);
                    }
                }
                if(changed) {
                    axioms.add(df.getOWLEquivalentClassesAxiom(expressions));
                } else {
                    axioms.add(ax);
                }
            } else if(ax instanceof OWLSubClassOfAxiom) {
                OWLClassExpression ce_new = null;
                OWLClassExpression ce = ((OWLSubClassOfAxiom) ax).getSuperClass();
                if (!ce.isClassExpressionLiteral() && containsReferenceClass(ce, o_ref_signature)) {
                    changed = true;
                    ce_new = replaceAllRefClassesWithOWLThing(ce,o_ref_signature);
                }
                if(changed) {
                    axioms.add(df.getOWLSubClassOfAxiom(((OWLSubClassOfAxiom) ax).getSubClass(),ce_new));
                } else {
                    axioms.add(ax);
                }
            }
        }
        return OWLManager.createOWLOntologyManager().createOntology(axioms);
    }

    private static boolean containsReferenceClass(OWLClassExpression ce, Set<OWLClass> o_ref_signature) {
        return !Collections.disjoint(ce.getClassesInSignature(), o_ref_signature);
    }

    private static OWLClassExpression replaceAllRefClassesWithOWLThing(OWLClassExpression ce, Set<OWLClass> o_ref_signature) {
        Map<IRI, IRI> replace = new HashMap<>();
        for (OWLClass c : ce.getClassesInSignature()) {
            if(o_ref_signature.contains(c)) {
                replace.put(c.getIRI(), df.getOWLThing().getIRI());
            }
        }
        if(replace.isEmpty()) {
            return ce;
        } else {
            OWLObjectDuplicator replacer = new OWLObjectDuplicator(df, replace);
            return replacer.duplicateObject(ce);
        }
    }

    private static int getImpact(OWLClass ref_ontology_class, Set<OWLAxiom> axioms, OWLReasoner r, Set<OWLClass> ref_ontology_sig) {
        Set<OWLAxiom> used_in = new HashSet<>();
        Set<OWLClass> branch = new HashSet<>();
        branch.add(ref_ontology_class);
        branch.addAll(r.getSubClasses(ref_ontology_class,false).getFlattened());
        branch.remove(df.getOWLNothing());
        branch.remove(df.getOWLThing());
        for(OWLAxiom ax:axioms) {
            if(!referenceOntologyOnly(ax.getClassesInSignature(),ref_ontology_sig)){
                for (OWLClass c : branch) {
                    if (ax.getClassesInSignature().contains(c)) {
                        used_in.add(ax);
                        break;
                    }
                }
            }
        }
        return used_in.size();
    }

    private static boolean referenceOntologyOnly(Set<OWLClass> sigAx, Set<OWLClass> ref_o_signature) {
        return ref_o_signature.containsAll(sigAx);
    }

    private static Set<OWLClass> computeReferenceClasses(OWLOntology ref_ontology, String subst) {
        Set<OWLClass> properties = new HashSet<>();
        for (OWLClass p : ref_ontology.getClassesInSignature(Imports.INCLUDED)) {
            if (p.getIRI().toString().contains(subst)) {
                properties.add(p);
            } else {
                log("Excluded: "+p);
            }
        }
        return properties;
    }

    private static void log(Object o) {
        System.out.println(o);
    }
}
