package monarch.ontology.phenoworkbench.unionanalytics;

import ebi.ontology.utilities.OntologyDebugReport;
import ebi.ontology.utilities.OntologyFileExtension;
import ebi.ontology.utilities.OntologyUtils;
import ebi.ontology.utilities.RenderManager;
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
    private RenderManager render = new RenderManager();
    private String BASE = "http://ebi.debug.owl#";
    private OntologyDebugReport report = new OntologyDebugReport();
    private final long start = System.currentTimeMillis();

    private final File pd;
    private final String reasoner;
    private final int MAXUNSAT;
    private final int MAXEXPLANATIONPERUNSAT;
    private final Imports imports;
    private final Map<IRI, OWLOntology> allAxiomsAcrossOntologies = new HashMap<>();
    private final Map<OWLAxiom, Set<IRI>> allOntologiesAcrossAxioms = new HashMap<>();

    private CorpusDebugger(File pd, String reasoner, boolean imports, int maxunsat,int maxexplunsat) {
        this.pd = pd;
        this.reasoner = reasoner;
        this.MAXUNSAT = maxunsat;
        this.MAXEXPLANATIONPERUNSAT = maxexplunsat;
        this.imports = imports ? Imports.INCLUDED : Imports.EXCLUDED;
    }

    public static void main(String[] args) {
        File pd = new File(args[0]);
        boolean imports = args[1].contains("i");
        int maxunsat = Integer.valueOf(args[2]);
        int maxexplunsat = Integer.valueOf(args[3]);
        String reasoner = args[4];
        File outdir = new File(args[5]);


        CorpusDebugger p = new CorpusDebugger(pd, reasoner, imports, maxunsat, maxexplunsat);
        p.run();
        try {
            p.printResults(outdir);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void run() {
        for (File ourl : pd.listFiles(new OntologyFileExtension())) {
            //if(ourl.endsWith("hp.owl"))
            processOntology(imports, ourl);
        }
    }

    private void printResults(File out) throws IOException {
        report.addLine("[[Compatibility analysis of corpus "+pd.getPath()+"]]");
        report.addEmptyLine();
        report.addLine("# Analysing individual ontologies for ");
        for (IRI iri : allAxiomsAcrossOntologies.keySet()) {
            OntologyUtils.p("Analysing: "+iri+printTime());
            report.addLine("## Ontology: " + iri);
            OWLOntology o = allAxiomsAcrossOntologies.get(iri);
            checkProfileCompliance(o);
            checkConsistency(o);
            report.addEmptyLine();
        }


        analyseOntologyCompatibilityIssues();

        FileUtils.writeLines(new File(out,"report_corpus_debugger.md"), report.getLines());
    }

    private void analyseOntologyCompatibilityIssues() {
        report.addLine("# Analyse Incompatibilites of Union");
        OntologyUtils.p("Analysing Compatibility issues:"+printTime());
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
            report.addLine("## Explanations");
            OntologyUtils.p("Generating explanations:"+printTime());
            for (OWLClass unsat : unsatisfiable) {
                i++;
                OWLAxiom entailment = df.getOWLEquivalentClassesAxiom(df.getOWLNothing(), unsat);
                Set<Explanation<OWLAxiom>> expl = gen.getExplanations(entailment, MAXEXPLANATIONPERUNSAT);
                report.addLine("");
                report.addLine("## Explanations for unsatistifiable class: " + render.getLabel(unsat));
                report.addLine("* IRI: " + unsat.getIRI());

                countAxiomsAcrossExplanations(countaxiomsinannotations, expl);

                report.addLine("### All explanations for "+render.getLabel(unsat));
                int ct = 1;
                for (Explanation<OWLAxiom> ex : expl) {

                    report.addLine("#### Explanation "+ct);
                    ExplanationAnalyser analyser = new ExplantionAnalyserImpl(ex, Collections.singleton(unsat), render);
                    report.addLines(analyser.getReport(countaxiomsinannotations, allOntologiesAcrossAxioms));
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
                report.addLine("### "+ render.renderManchester((OWLAxiom) e.getKey()) + " (" + e.getValue()+")");
                report.addLine("Used in: ");
                report.addEmptyLine();
                for(IRI iri:allOntologiesAcrossAxioms.get(e.getKey())) {
                    report.addLine("* "+iri);
                }
            }

        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }
        report.addEmptyLine();
    }

    private void countAxiomsAcrossExplanations(HashMap<OWLAxiom, Integer> countaxiomsinannotations, Set<Explanation<OWLAxiom>> expl) {
        for (Explanation<OWLAxiom> ex : expl) {
            for (OWLAxiom ax : ex.getAxioms()) {
                if (!countaxiomsinannotations.containsKey(ax)) {
                    countaxiomsinannotations.put(ax, 0);
                }
                countaxiomsinannotations.put(ax, countaxiomsinannotations.get(ax) + 1);
            }
        }
    }

    private String printTime() {
        long current = System.currentTimeMillis();
        long duration = current - start;
        return " ("+(duration/1000)+" sec)";
    }

    private void processOntology(Imports imports, File ofile) {
        try {
            OWLOntology o = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(ofile);
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
    }

    private void checkConsistency(OWLOntology o) {
        report.addLine("### Consistency analysis");
        try {
            OWLReasoner rr = createReasoner(o);
            Set<OWLClass> unsat = new HashSet<>(rr.getUnsatisfiableClasses().getEntities());
            report.addLine("* Consistent:" + rr.isConsistent());
            report.addLine("* Unsatisfiable Classes: "+unsat.size());
            unsat.forEach(sat -> report.addLine("  * "+render.getLabel(sat)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        report.addEmptyLine();
    }

    private void checkProfileCompliance(OWLOntology o) {
        report.addLine("### Profile Conformance");
        report.addLine("This report does not show undeclared entity violations.");
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
            report.addLine("#### "+viogroup);
            for (OWLProfileViolation vio : vioMap.get(viogroup)) {
                if (!(vio instanceof UndeclaredEntityViolation)) {
                    report.addLine("* " + vio.toString());
                    report.addLine("  * Expression: " + vio.getExpression());
                    report.addLine("  * Axiom: " + vio.getAxiom());
                }
            }
        }
        report.addLine("#### Undeclared Entities");
        report.addLine("* Nr of undeclared entity violations: "+ctundeclared);
        report.addEmptyLine();
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

}
