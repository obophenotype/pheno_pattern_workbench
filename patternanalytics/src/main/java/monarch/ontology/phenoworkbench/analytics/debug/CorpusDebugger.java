package monarch.ontology.phenoworkbench.analytics.debug;

import monarch.ontology.phenoworkbench.util.*;
import monarch.ontology.phenoworkbench.util.Timer;
import org.apache.commons.io.FileUtils;
import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owl.explanation.api.Explanation;
import org.semanticweb.owl.explanation.api.ExplanationGenerator;
import org.semanticweb.owl.explanation.api.ExplanationGeneratorFactory;
import org.semanticweb.owl.explanation.api.ExplanationManager;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.profiles.OWL2DLProfile;
import org.semanticweb.owlapi.profiles.OWLProfileReport;
import org.semanticweb.owlapi.profiles.OWLProfileViolation;
import org.semanticweb.owlapi.profiles.violations.UndeclaredEntityViolation;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class CorpusDebugger {

    private OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
    private RenderManager render = RenderManager.getInstance();
    private String BASE = "http://ebi.debug.owl#";
    private OntologyDebugReport report = new OntologyDebugReport();

    private final Set<OntologyEntry> pd;
    private final String reasoner;
    private final int MAXUNSAT;
    private final int MAXEXPLANATIONPERUNSAT;
    private final Imports imports;
    private final Map<IRI, OWLOntology> allAxiomsAcrossOntologies = new HashMap<>();
    private final Map<OWLAxiom, Set<IRI>> allOntologiesAcrossAxioms = new HashMap<>();

    public CorpusDebugger(Set<OntologyEntry> pd, String reasoner, boolean imports, int maxunsat,int maxexplunsat) {
        this.pd = pd;
        this.reasoner = reasoner;
        this.MAXUNSAT = maxunsat;
        this.MAXEXPLANATIONPERUNSAT = maxexplunsat;
        this.imports = imports ? Imports.INCLUDED : Imports.EXCLUDED;
    }

    public static void main(String[] args) throws IOException {
        File pd = new File(args[0]);
        boolean imports = args[1].contains("i");
        int maxunsat = Integer.valueOf(args[2]);
        int maxexplunsat = Integer.valueOf(args[3]);
        String reasoner = args[4];
        File outdir = new File(args[5]);

        OntologyRegistry phenotypeontologies = new OntologyRegistry(pd,new File(UUID.randomUUID().toString()));
        CorpusDebugger p = new CorpusDebugger(phenotypeontologies.getOntologies(), reasoner, imports, maxunsat, maxexplunsat);
        p.run();
        try {
            p.printResults(outdir);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void run() {
        Timer.start("CorpusDebugger::runAnalysis");
        for (OntologyEntry ourl : pd) {
            //if(ourl.endsWith("hp.owl"))
            processOntology(imports, ourl.getIri());
        }
        preparePrint();
        Timer.end("CorpusDebugger::runAnalysis");
    }

    private void preparePrint() {
        Timer.start("CorpusDebugger::preparePrint");
        report.clear();
        report.addLine("# Compatibility analysis of corpus");
        report.addLine("## Analysing individual ontologies for Profile violations");
        for (IRI iri : allAxiomsAcrossOntologies.keySet()) {
            report.addLine("### Ontology: " + iri);
            OWLOntology o = allAxiomsAcrossOntologies.get(iri);
            checkProfileCompliance(o);
            checkConsistency(o);
            report.addEmptyLine();
        }


        analyseOntologyCompatibilityIssues();
        Timer.end("CorpusDebugger::preparePrint");
    }

    public void printResults(File out) throws IOException {
        FileUtils.writeLines(new File(out,"report_corpus_debugger.md"), report.getLines());
    }

    private void analyseOntologyCompatibilityIssues() {
        report.addLine("## Analyse Incompatibilites of Union");
        report.addLine("* Unsatisfiable classes are marked with {}, Named anonymous classes are marked with [] and the class in questions is marked with ()");
        report.addLine("* The explanation class hierarchies only includes top level classes that have at least one child.");
        report.addLine("  * (Only those can be potentially relevant for debugging).");

        OntologyUtils.p("Analysing Compatibility issues:"+printTime("debug"));
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        try {

            OWLOntology o = man.createOntology();
            for (IRI iri : allAxiomsAcrossOntologies.keySet()) {
                man.addAxioms(o,allAxiomsAcrossOntologies.get(iri).getAxioms(Imports.EXCLUDED));
            }

            OWLReasoner r = createReasoner(o);
            OWLReasonerFactory rf = new ElkReasonerFactory();

            ExplanationGeneratorFactory<OWLAxiom> genFac = ExplanationManager.createExplanationGeneratorFactory(rf);

            ExplanationGenerator<OWLAxiom> gen = genFac.createExplanationGenerator(o);

            int i = 0;

            Set<OWLClass> unsatisfiable = new HashSet<>();
            unsatisfiable.addAll(r.getUnsatisfiableClasses().getEntitiesMinus(df.getOWLNothing()));


            //TODO Contemplate using this:
            //CompleteRootDerivedReasoner cr = new CompleteRootDerivedReasoner(o.getOWLOntologyManager(), r, rf);
            //unsatisfiable.addAll(cr.getRootUnsatisfiableClasses());

            HashMap<OWLAxiom, Integer> countaxiomsinannotations = new HashMap<>();
            report.addEmptyLine();
            report.addLine("### Explanations");
            OntologyUtils.p("Generating explanations:"+printTime("debug"));
            for (OWLClass unsat : unsatisfiable) {
                i++;
                OWLAxiom entailment = df.getOWLEquivalentClassesAxiom(df.getOWLNothing(), unsat);
                Set<Explanation<OWLAxiom>> expl = gen.getExplanations(entailment, MAXEXPLANATIONPERUNSAT);
                report.addLine("");
                report.addLine("#### Explanations for unsatistifiable " + render.getLabel(unsat));
                report.addLine("* IRI: " + unsat.getIRI());

                countAxiomsAcrossExplanations(countaxiomsinannotations, expl);
                int ct = 1;
                for (Explanation<OWLAxiom> ex : expl) {

                    report.addLine("  * Explanation "+ct);
                    ExplanationAnalyser analyser = new ExplantionAnalyserImpl(new monarch.ontology.phenoworkbench.util.Explanation(ex), Collections.singleton(unsat), render);
                    report.addLines(analyser.getReport(2));
                    ct++;
                }

                if (i > MAXUNSAT) {
                    break;
                }
            }

            TreeMap<Object, Integer> map = OntologyUtils.sortMapByValue(countaxiomsinannotations);

            report.addEmptyLine();
            report.addLine("## Frequently used axioms across unsatisfiability explanations");
            for(Map.Entry e:map.entrySet()) {
                report.addLine("* "+ render.renderManchester((OWLAxiom) e.getKey()) + " (" + e.getValue()+")");
                /*report.addLine("  * Used in: ");
                report.addEmptyLine();
                for(IRI iri:allOntologiesAcrossAxioms.get(e.getKey())) {
                    report.addLine("    * "+iri);
                } */
            }

        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }
        report.addEmptyLine();
    }

    private void countAxiomsAcrossExplanations(HashMap<OWLAxiom, Integer> countaxiomsinannotations, Set<Explanation<OWLAxiom>> expl) {
        Timer.start("CorpusDebugger::countAxiomsAcrossExplanations");
        for (Explanation<OWLAxiom> ex : expl) {
            for (OWLAxiom ax : ex.getAxioms()) {
                if (!countaxiomsinannotations.containsKey(ax)) {
                    countaxiomsinannotations.put(ax, 0);
                }
                countaxiomsinannotations.put(ax, countaxiomsinannotations.get(ax) + 1);
            }
        }
        Timer.end("CorpusDebugger::countAxiomsAcrossExplanations");
    }

    private String printTime(String process) {
       return Timer.getSecondsElapsed(process);
    }

    private void processOntology(Imports imports, String ofile) {
        Timer.start("CorpusDebugger::processOntology");
        try {
            OWLOntology o = KB.getInstance().getOntology(ofile).get();
            render.addLabel(o);
            for (OWLOntology imp : o.getImportsClosure()) {
                IRI niri = IRI.create(BASE + UUID.randomUUID());
                if (!imp.getOntologyID().getOntologyIRI().isPresent()) {
                    imp.getOWLOntologyManager().setOntologyDocumentIRI(imp, niri);
                }
                IRI iri = imp.getOntologyID().getOntologyIRI().or(niri);
                imp.getImportsDeclarations().forEach(impdec->imp.getOWLOntologyManager().applyChange(new RemoveImport(imp,impdec)));
                allAxiomsAcrossOntologies.put(iri, imp);
                for(OWLAxiom ax:imp.getAxioms(Imports.EXCLUDED)) {
                    if(!allOntologiesAcrossAxioms.containsKey(ax)) {
                        allOntologiesAcrossAxioms.put(ax,new HashSet<>());
                    }
                    allOntologiesAcrossAxioms.get(ax).add(iri);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        Timer.end("CorpusDebugger::processOntology");
    }

    private void checkConsistency(OWLOntology o) {
        report.addLine("* Consistency analysis");
        try {
            OWLReasoner rr = createReasoner(o);
            Set<OWLClass> unsat = new HashSet<>(rr.getUnsatisfiableClasses().getEntitiesMinus(df.getOWLNothing()));
            report.addLine("  * Consistent:" + rr.isConsistent());
            report.addLine("  * Unsatisfiable Classes: "+unsat.size());
            unsat.forEach(sat -> report.addLine("    * "+render.getLabel(sat)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        report.addEmptyLine();
    }

    private void checkProfileCompliance(OWLOntology o) {
        report.addLine("* Profile Conformance");
        OWL2DLProfile prof = new OWL2DLProfile();
        OWLProfileReport preport = prof.checkOntology(o);
        Map<String,Set<OWLProfileViolation>> vioMap = new HashMap<>();
        int ctundeclared = 0;
        for (OWLProfileViolation vio : preport.getViolations()) {
            if (vio instanceof UndeclaredEntityViolation) {
                ctundeclared++;
            } else {
                String cl = vio.getClass().getSimpleName();
                if(!vioMap.containsKey(cl)) {
                    vioMap.put(cl,new HashSet<>());
                }
                vioMap.get(cl).add(vio);
            }
        }

        for(String viogroup:vioMap.keySet()) {
            report.addLine("  * "+viogroup);
            for (OWLProfileViolation vio : vioMap.get(viogroup)) {
                if (!(vio instanceof UndeclaredEntityViolation)) {
                    report.addLine("    * " + vio.toString());
                    report.addLine("    * Expression: " + vio.getExpression());
                    report.addLine("    * Axiom: " + vio.getAxiom());
                }
            }
        }
        report.addLine("  * Undeclared Entities: "+ctundeclared);
    }

    private OWLReasoner createReasoner(OWLOntology o) {
        if(reasoner.equals("elk")) {
            return new ElkReasonerFactory().createReasoner(o);
        } else if(reasoner.equals("hermit")) {
            return new ReasonerFactory().createReasoner(o);
        } else{
            System.err.println("Unknown reasoner "+reasoner+", using default (ELK)");
            return new ElkReasonerFactory().createReasoner(o);
        }

        }

    public List<String> getReportLines() {
        return report.getLines();
    }
}
