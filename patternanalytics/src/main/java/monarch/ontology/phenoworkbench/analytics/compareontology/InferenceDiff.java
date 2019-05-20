package monarch.ontology.phenoworkbench.analytics.compareontology;

import monarch.ontology.phenoworkbench.analytics.inferenceanalysis.DefinitionSet;
import monarch.ontology.phenoworkbench.analytics.inferenceanalysis.PhenoEntities;
import monarch.ontology.phenoworkbench.util.*;
import org.apache.commons.io.FileUtils;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.OWLXMLDocumentFormat;
import org.semanticweb.owlapi.formats.TurtleDocumentFormat;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class InferenceDiff {

    static OWLDataFactory df = OWLManager.getOWLDataFactory();

    public static void main(String[] args) throws OWLOntologyCreationException, IOException {
        File f1 = new File(args[0]);
        File f2 = new File(args[1]);
        File branchlist = new File(args[2]);
        File out = new File(args[3]);

        System.out.println("Loading O1");
        OWLOntology o1 = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(f1);
        System.out.println("Loading O2");
        OWLOntology o2 = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(f2);

        System.out.println("Preparing reasoner 1");
        Reasoner r1 = new Reasoner(o1);
        System.out.println("Preparing reasoner 2");
        Reasoner r2 = new Reasoner(o2);

        System.out.println("Preparing labels and branchdata");
        BranchLoader bl = new BranchLoader();
        bl.loadBranches(FileUtils.readLines(branchlist,"utf-8"),o1.getClassesInSignature(),true,r1.getOWLReasoner());
        bl.loadBranches(FileUtils.readLines(branchlist,"utf-8"),o2.getClassesInSignature(),true,r2.getOWLReasoner());

        RenderManager ren = RenderManager.getInstance();
        ren.addLabel(o1);
        ren.addLabel(o2);

        System.out.println("Running DIFF");
        Map<Subsumption,String> diff = diff(r1,r2,bl.getAllClassesInBranches());
        List<Map<String,String>> csv = new ArrayList<>();
        for(Subsumption s:diff.keySet()) {
            Map<String,String> rec = new HashMap<>();
            rec.put("sub_iri",s.getSub_c().getIRI().toString());
            rec.put("super_iri",s.getSuper_c().getIRI().toString());
            rec.put("sub_label",ren.getLabel(s.getSub_c()));
            rec.put("super_label",ren.getLabel(s.getSuper_c()));
            rec.put("category",diff.get(s));
            rec.put("o1",f1.getName());
            rec.put("o2",f2.getName());
            csv.add(rec);
        }
        System.out.println("Exporting");
        Export.writeCSV(csv,out);
    }


    private static Map<Subsumption,String> diff(Reasoner r1, Reasoner r2, Set<OWLClass> branch) {
        Map<Subsumption,String> diff = new HashMap<>();
        Set<OWLClass> unsat = new HashSet<>();
        unsat.addAll(r1.getUnsatisfiableClasses());
        unsat.addAll(r2.getUnsatisfiableClasses());
        unsat.remove(df.getOWLNothing());
        for(OWLClass u:unsat) {
            if(r1.getUnsatisfiableClasses().contains(u)) {
                if(r2.getUnsatisfiableClasses().contains(u)) {
                    diff.put(new Subsumption(u,df.getOWLNothing()),"unsat_both");
                } else {
                    diff.put(new Subsumption(u,df.getOWLNothing()),"unsat_r1");
                }
            } else {
                diff.put(new Subsumption(u,df.getOWLNothing()),"unsat_r2");
            }
        }
        for(OWLClass branchclass:branch) {
            for(OWLClass c_sub:r1.getSubclassesOf(branchclass,true,true)) {
                if(!r2.getSubclassesOf(branchclass,false,true).contains(c_sub)){
                    diff.put(new Subsumption(branchclass,c_sub),"o_not_o2");
                }
            }
            for(OWLClass c_super:r1.getSuperClassesOf(branchclass,true,true)) {
                if(!r2.getSuperClassesOf(branchclass,false,true).contains(c_super)){
                    diff.put(new Subsumption(c_super,branchclass),"o_not_o2");
                }
            }
            for(OWLClass c_sub:r2.getSubclassesOf(branchclass,true,true)) {
                if(!r1.getSubclassesOf(branchclass,false,true).contains(c_sub)){
                    diff.put(new Subsumption(branchclass,c_sub),"o2_not_o");
                }
            }
            for(OWLClass c_super:r2.getSuperClassesOf(branchclass,true,true)) {
                if(!r1.getSuperClassesOf(branchclass,false,true).contains(c_super)){
                    diff.put(new Subsumption(c_super,branchclass),"o2_not_o");
                }
            }
        }
        return diff;
    }



}