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

    private RenderManager render = RenderManager.getInstance();
    private OntologyDebugReport report = new OntologyDebugReport();

    private final long start = System.currentTimeMillis();
    private final File pd;
    private final Imports imports;
    private final Map<String, Set<OWLAxiom>> allAxiomsAcrossOntologies = new HashMap<>();

    OWLOntology o = null;
    OWLReasoner rel = null;

    Set<Subsumption> subsSL = new HashSet<>();
    Set<Subsumption> subsEL = new HashSet<>();
    Set<Subsumption> subsSYN = new HashSet<>();
    Map<OWLClass,Set<OWLClass>> superclassmapSL = new HashMap<>();
    Map<OWLClass,Set<OWLClass>> superclassmapEL = new HashMap<>();
    Map<OWLClass,Set<OWLClass>> superclassmapSYN = new HashMap<>();


    public InferenceAnalyser(File ont, boolean imports) {
        this.pd = ont;
        this.imports = imports ? Imports.INCLUDED : Imports.EXCLUDED;
    }

    public static void main(String[] args) throws IOException {
        File ont = new File("/data/irtest/test.owl");
        InferenceAnalyser p = new InferenceAnalyser(ont,false);
        p.prepare();

    }

    public void prepare() {
        processOntology(imports, IRI.create(pd).toString());
        try {
            o = OWLManager.createOWLOntologyManager().createOntology(allAxiomsAcrossOntologies.get(IRI.create(pd).toString()));
            OntologyUtils.p("Creating reasoners"+printTime());
            rel = createELReasoner(o);
            //OWLReasoner rdl = createDLReasoner(o);
            OWLReasoner rsl = createStructuralReasoner(o);
            OntologyUtils.p("Subs: EL"+printTime());
            subsEL.addAll(SubsumptionUtils.getSubsumptions(rel, o,false));
            OntologyUtils.p("Subs: Structural"+printTime());
            subsSL.addAll(SubsumptionUtils.getSubsumptions(rsl, o,false));
            OntologyUtils.p("Subs: Syntactic"+printTime());
            subsSYN.addAll(SubsumptionUtils.getSubsumptions(null, o,false));

            OntologyUtils.p("Super: EL"+printTime());
            superclassmapEL.putAll(SubsumptionUtils.getSuperClassMap(rel, o));
           OntologyUtils.p("Super: Structural"+printTime());
            superclassmapSL.putAll(SubsumptionUtils.getSuperClassMap(rsl, o));
            OntologyUtils.p("Super: Syntactic"+printTime());
            superclassmapSYN.putAll(SubsumptionUtils.getSuperClassMap(null, o));


        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }
    }

    public OntologyDebugReport getMarkdownReport() {
        report.addLine("# Analysing individual ontologies for inferences");
        for (String f: allAxiomsAcrossOntologies.keySet()) {
            report.addLine("## Ontology: " + f);
            report.addEmptyLine();
            OntologyUtils.p("#### ANALYSING ONTOLOGY: "+f);
            //...
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

            report.addEmptyLine();
        }

        return report;
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

    public Set<Subsumption> getInferredNotAsserted() {
        Set<Subsumption> inferredNotAsserted = new HashSet<>(subsEL);
        inferredNotAsserted.removeAll(subsSL);
        return inferredNotAsserted;
    }

    InferenceExplainerMain explanationManager = null;
    OWLDataFactory df = OWLManager.getOWLDataFactory();

    public Set<Explanation> getExplanations(Subsumption s, int max_explanation) {
        Set<Explanation> explanations = new HashSet<>();
        if(explanationManager==null) {
            explanationManager = new InferenceExplainerMain(o,rel);
        }
        explanationManager.getExplanations(df.getOWLSubClassOfAxiom(s.getSub_c(),s.getSuper_c()),max_explanation).forEach(e->explanations.add(new Explanation(e)));
        return explanations;
    }
}
