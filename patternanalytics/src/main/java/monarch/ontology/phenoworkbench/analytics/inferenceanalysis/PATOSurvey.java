package monarch.ontology.phenoworkbench.analytics.inferenceanalysis;

import monarch.ontology.phenoworkbench.util.Export;
import monarch.ontology.phenoworkbench.util.RenderManager;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import java.io.File;
import java.util.*;

public class PATOSurvey {

    public static OWLDataFactory df = OWLManager.getOWLDataFactory();
    public static RenderManager renderManager = RenderManager.getInstance();

    public static void main(String[] args) throws OWLOntologyCreationException {
        //args = new String[3];
        //args[0] = "/data/pato.owl";
        //args[1] = "/data/corpora/obo20190313/obi.owl";
        //args[2] = "/data/patosurvey/";
        File f_pato = new File(args[0]);
        File f_o = new File(args[1]);
        File out = new File(args[2]);
        log("Loading PATO");
        OWLOntology pato = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(f_pato);
        log("Loading " + f_o.getName());
        OWLOntology o = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(f_o);
        OWLReasoner r = new ElkReasonerFactory().createReasoner(o);
        renderManager.addLabel(pato);
        renderManager.addLabel(o);

        Set<OWLClass> pato_classes = computePatoClasses(pato);
        pato_classes.retainAll(o.getClassesInSignature(Imports.INCLUDED));

        Set<OWLAxiom> non_pato_axioms = new HashSet<>(o.getAxioms(Imports.INCLUDED));
        non_pato_axioms.removeAll(pato.getAxioms(Imports.INCLUDED));


        List<Map<String, String>> data_axiom = new ArrayList<>();
        List<Map<String, String>> data_expressions = new ArrayList<>();
        List<Map<String, String>> data_class = new ArrayList<>();
        log("Extracting data about classes");
        for (OWLClass p : pato_classes) {
            Map<String, String> rec = new HashMap<>();
            rec.put("iri", p.getIRI().toString());
            rec.put("label",renderManager.getLabel(p));
            rec.put("o_path", f_o.getAbsolutePath());
            rec.put("o", f_o.getName());
            rec.put("impact",getImpact(p,non_pato_axioms,r,pato_classes)+"");
            data_class.add(rec);
        }
        log("Extracting data about axioms ("+o.getAxiomCount(Imports.INCLUDED)+" axioms, "+pato_classes.size()+" classes)");
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
                if (pato_classes.contains(p)) {
                    String axiomcode = ax.hashCode() + "";
                    String axiomtype = ax.getAxiomType().getName();
                    Map<String, String> rec = new HashMap<>();
                    rec.put("axiomid", axiomcode);
                    rec.put("axiom_incl","unknown");
                    rec.put("axiomtype", axiomtype);
                    rec.put("o_path", f_o.getAbsolutePath());
                    rec.put("o", f_o.getName());
                    rec.put("iri", p.getIRI().toString());
                    boolean pat_only = patoOnly(ax.getClassesInSignature(),pato_classes);
                    rec.put("pato_only", pat_only+"");
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
        Export.writeCSV(data_axiom, new File(out, "patosurvey_axiomdata_" + f_o.getName() + ".csv"));
        Export.writeCSV(data_expressions, new File(out, "patosurvey_expressiondata_" + f_o.getName() + ".csv"));
        Export.writeCSV(data_class, new File(out, "patosurvey_classdata_" + f_o.getName() + ".csv"));
    }

    private static int getImpact(OWLClass pato, Set<OWLAxiom> axioms, OWLReasoner r, Set<OWLClass> pato_sig) {
        Set<OWLAxiom> used_in = new HashSet<>();
        Set<OWLClass> branch = new HashSet<>();
        branch.add(pato);
        branch.addAll(r.getSubClasses(pato,false).getFlattened());
        branch.remove(df.getOWLNothing());
        branch.remove(df.getOWLThing());
        for(OWLAxiom ax:axioms) {
            if(!patoOnly(ax.getClassesInSignature(),pato_sig)){
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

    private static boolean patoOnly(Set<OWLClass> sigAx,Set<OWLClass> pato) {
        return pato.containsAll(sigAx);
    }

    private static Set<OWLClass> computePatoClasses(OWLOntology pato) {
        Set<OWLClass> properties = new HashSet<>();
        for (OWLClass p : pato.getClassesInSignature(Imports.INCLUDED)) {
            if (p.getIRI().toString().contains("PATO_")) {
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
