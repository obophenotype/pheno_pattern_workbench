package monarch.ontology.phenoworkbench.analytics.inferenceanalysis;

import monarch.ontology.phenoworkbench.util.*;
import org.apache.commons.io.FileUtils;
import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;


public class InferenceAnalyser {

    private RenderManager render = new RenderManager();
    private OntologyDebugReport report = new OntologyDebugReport();

    private final long start = System.currentTimeMillis();
    private final Set<String> pd;
    private final Imports imports;
    private final Map<String, Set<OWLAxiom>> allAxiomsAcrossOntologies = new HashMap<>();

    public InferenceAnalyser(Set<String> pd, boolean imports) {
        this.pd = pd;
        this.imports = imports ? Imports.INCLUDED : Imports.EXCLUDED;
    }

    public static void main(String[] args) throws IOException {
        File pd = new File(args[0]);
        boolean imports = args[1].contains("i");
        File out = new File(args[2]);


        InferenceAnalyser p = new InferenceAnalyser(new HashSet<>(FileUtils.readLines(pd,"UTF-8")), imports);
        p.prepare();
        try {
            p.printResults(out);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void prepare() {
        for (String ourl : pd) {
            //if(ourl.endsWith("hp.owl"))
            processOntology(imports, ourl);
        }
    }

    public void printResults(File out) throws IOException {
        report.addLine("# Analysing individual ontologies for inferences");
        for (String f: allAxiomsAcrossOntologies.keySet()) {
            report.addLine("## Ontology: " + f);
            report.addEmptyLine();
            OntologyUtils.p("#### ANALYSING ONTOLOGY: "+f);
            //...
            try {
                OWLOntology o = OWLManager.createOWLOntologyManager().createOntology(allAxiomsAcrossOntologies.get(f));
                OntologyUtils.p("Creating reasoners"+printTime());
                OWLReasoner rel = createELReasoner(o);
                //OWLReasoner rdl = createDLReasoner(o);
                OWLReasoner rsl = createStructuralReasoner(o);
                OntologyUtils.p("Subs: EL"+printTime());
                Set<Subsumption> subsEL = getSubsumptions(rel, o);
                OntologyUtils.p("Subs: DL"+printTime());
                //Set<Subsumption> subsDL = getSubsumptions(rdl, o);
                OntologyUtils.p("Subs: Structural"+printTime());
                Set<Subsumption> subsSL = getSubsumptions(rsl, o);
                OntologyUtils.p("Subs: Syntactic"+printTime());
                Set<Subsumption> subsSYN = getSubsumptions(null, o);

                OntologyUtils.p("Super: EL"+printTime());
                Map<OWLClass,Set<OWLClass>> superclassmapEL = getSuperClassMap(rel, o);
                OntologyUtils.p("Subs: DL"+printTime());
                //Map<OWLClass,Set<OWLClass>> superclassmapDL = getSuperClassMap(rdl, o);
                OntologyUtils.p("Super: Structural"+printTime());
                Map<OWLClass,Set<OWLClass>> superclassmapSL = getSuperClassMap(rsl, o);
                OntologyUtils.p("Super: Syntactic"+printTime());
                Map<OWLClass,Set<OWLClass>> superclassmapSYN = getSuperClassMap(null, o);
                OntologyUtils.p("");
                OntologyUtils.p("RESULTS:"+printTime());
                //printSubsInfo(subsDL,"DL");
                printSubsInfo(subsEL,"EL");
                printSubsInfo(subsSL,"Structural");
                printSubsInfo(subsSYN,"Syntactic");

                //printSuperInfo(superclassmapDL,"DL");
                printSuperInfo(superclassmapEL,"EL");
                printSuperInfo(superclassmapSL,"Structural");
                printSuperInfo(superclassmapSYN,"Syntactic");

            } catch (OWLOntologyCreationException e) {
                e.printStackTrace();
            }

            report.addEmptyLine();
        }

        FileUtils.writeLines(new File(out,"report_inference_analysis.md"), report.getLines());
    }

    public List<String> getReportLines() {
        return report.getLines();
    }

    private void printSuperInfo(Map<OWLClass, Set<OWLClass>> map, String label) {
        int multipleinheritance = 0;
        int singleinheritance = 0;
        for(OWLClass c:map.keySet()) {
            if(map.get(c).size()>1) {
                multipleinheritance++;
            }
            else {
                singleinheritance++;
            }
        }
        report.addLine("* "+label+": multiple inheritance: "+multipleinheritance);
        report.addLine("* "+label+": single inheritance: "+singleinheritance);
        report.addLine("* "+label+": % multiple inheritance: "+Math.round((double)100*((double)multipleinheritance/(double)(multipleinheritance+singleinheritance))));
    }

    private void printSubsInfo(Set<Subsumption> subs, String label) {
        report.addLine("* "+label+": Number subs: "+subs.size());
    }

    private Map<OWLClass,Set<OWLClass>> getSuperClassMap(OWLReasoner r,OWLOntology o) {
        Map<OWLClass,Set<OWLClass>> superclasses = new HashMap<>();
        if(r!=null) {
            for(OWLClass c:r.getRootOntology().getClassesInSignature(Imports.INCLUDED)) {
                if(!superclasses.containsKey(c)) {
                    superclasses.put(c,new HashSet<>());
                }
                for (OWLClass s : r.getSuperClasses(c, true).getFlattened()) {
                    superclasses.get(c).add(s);
                }
            } }
        else {
            for(OWLAxiom ax:o.getAxioms(Imports.INCLUDED)) {
                if(ax instanceof OWLSubClassOfAxiom){
                    OWLSubClassOfAxiom sbcl = (OWLSubClassOfAxiom)ax;
                    OWLClassExpression subc = sbcl.getSubClass();
                    OWLClassExpression superc = sbcl.getSuperClass();
                    if(!subc.isAnonymous() && !superc.isAnonymous()) {
                     OWLClass c = (OWLClass)subc;
                        if(!superclasses.containsKey(c)) {
                            superclasses.put(c,new HashSet<>());
                        }
                        superclasses.get(c).add((OWLClass)superc);
                    }
                }
            }

        }
        return superclasses;
    }

    private Set<Subsumption> getSubsumptions(OWLReasoner r,OWLOntology o) {
        Set<Subsumption> subs = new HashSet<>();
        if(r!=null) {
        for(OWLClass c:r.getRootOntology().getClassesInSignature(Imports.INCLUDED)) {
            for (OWLClass sub : r.getSubClasses(c, false).getFlattened()) {
                subs.add(new Subsumption(c, sub));
            }
        } }
        else {
            for(OWLAxiom ax:o.getAxioms(Imports.INCLUDED)) {
                if(ax instanceof OWLSubClassOfAxiom){
                    OWLSubClassOfAxiom sbcl = (OWLSubClassOfAxiom)ax;
                    OWLClassExpression subc = sbcl.getSubClass();
                    OWLClassExpression superc = sbcl.getSuperClass();
                    if(!subc.isAnonymous() && !superc.isAnonymous()) {
                       subs.add(new Subsumption((OWLClass)subc,(OWLClass)superc));
                    }
                }
            }
        }

        return subs;
    }


    private void processOntology(Imports imports, String ofile) {
        OntologyUtils.p("Preparing "+ofile);
        try {
            OWLOntology o = KB.getInstance().getOntology(ofile).get();
            render.addLabel(o);
            allAxiomsAcrossOntologies.put(ofile,o.getAxioms(imports));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private OWLReasoner createELReasoner(OWLOntology o) {
        return new ElkReasonerFactory().createReasoner(o);
    }

    private OWLReasoner createStructuralReasoner(OWLOntology o) {
        return new StructuralReasonerFactory().createReasoner(o);
    }

    private OWLReasoner createDLReasoner(OWLOntology o) {
        //OntologyUtils.p("WARNING: REPLACE REASONER");
        //TODO Replace reasoner
        return new ReasonerFactory().createReasoner(o);
        //return new ElkReasonerFactory().createReasoner(o);
    }

    private String printTime() {
        long current = System.currentTimeMillis();
        long duration = current - start;
        return " ("+(duration/1000)+" sec)";
    }
}
