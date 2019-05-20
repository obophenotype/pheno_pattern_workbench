package monarch.ontology.phenoworkbench.analytics.pattern.report;

import monarch.ontology.phenoworkbench.analytics.inferenceanalysis.DefinitionImpactOntologyCompare;
import monarch.ontology.phenoworkbench.analytics.inferenceanalysis.OntologyStatsRunner;
import monarch.ontology.phenoworkbench.analytics.pattern.generation.PatternGenerator;
import monarch.ontology.phenoworkbench.analytics.pattern.generation.PatternManager;
import monarch.ontology.phenoworkbench.util.*;
import monarch.ontology.phenoworkbench.util.Timer;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.*;

public class FastPatternSurvey {

    private RenderManager render = RenderManager.getInstance();
    private PatternGenerator patternGenerator = new PatternGenerator(render);

    private List<Map<String, String>> data_c = new ArrayList<>();
    private Map<OWLClass, Set<DefinedClass>> definedClasses = new HashMap<>();
    private Set<DefinedClass> allDefinedClasses = new HashSet<>();

    private final String phenoclass;
    private final String uri;
    private final String id;
    private final File out;
    private final File outont;

    private FastPatternSurvey(String uri, String phenoclass, String id, File out, File outont) {
        this.uri = uri;
        this.phenoclass = phenoclass;
        this.id = id;
        this.out = out;
        this.outont = outont;
    }

    public void runAnalysis() {
        OntologyUtils.p("Process Ontologies" + Timer.getSecondsElapsed("PatternExtractor::runAnalysis"));
        try {
            OWLOntology o = OWLManager.createOWLOntologyManager().loadOntology(IRI.create(uri));
            render.addLabel(o);

            OntologyUtils.p("Create Reasoner" + Timer.getSecondsElapsed("PatternExtractor::runAnalysis"));
            BranchLoader branches = new BranchLoader();
            Reasoner rs = new Reasoner(o);

            OntologyUtils.p("Precompute unsatisfiable classes" + Timer.getSecondsElapsed("PatternExtractor::runAnalysis"));
            branches.loadBranches(Collections.singleton(phenoclass), o.getClassesInSignature(Imports.INCLUDED), true, rs.getOWLReasoner());
            OntologyUtils.p("Subclasses" + Timer.getSecondsElapsed("A"));

            OntologyUtils.p("Extract definitions" + Timer.getSecondsElapsed("A"));
            patternGenerator.extractDefinedClasses(o.getAxioms(Imports.INCLUDED), false).forEach(this::putD);

            //Is needed to extract grammars:
            PatternManager man = new PatternManager(allDefinedClasses, rs, patternGenerator, render);
            System.out.println(man.getAllDefinedClasses().size());

            OntologyUtils.p("Harvest Data" + Timer.getSecondsElapsed("A"));
            OntologyUtils.p(branches.getAllClassesInBranches().size());
            branches.getAllClassesInBranches().forEach(this::harvest);
            OWLOntologyManager man2 = OWLManager.createOWLOntologyManager();
            Set<OWLAxiom> axioms = new HashSet<>();
            SyntacticLocalityModuleExtractor slme = new SyntacticLocalityModuleExtractor(o.getOWLOntologyManager(),o, ModuleType.BOT);
            Set<OWLEntity> signature = new HashSet<>(definedClasses.keySet());
            axioms.addAll(slme.extract(signature));
           /* OWLDataFactory df = OWLManager.getOWLDataFactory();

            for(OWLClass c:definedClasses.keySet()) {
                for(DefinedClass d:definedClasses.get(c)) {
                    axioms.add(df.getOWLEquivalentClassesAxiom(d.getOWLClass(),d.getDefiniton()));
                }
            }*/
            OWLOntology outo = man2.createOntology(axioms);
            man2.saveOntology(outo,new FileOutputStream(new File(outont,id+"_definitions.owl")));
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        } catch (OWLOntologyStorageException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void putD(DefinedClass d) {
        if (!definedClasses.containsKey(d.getOWLClass())) {
            definedClasses.put(d.getOWLClass(), new HashSet<>());
        }
        definedClasses.get(d.getOWLClass()).add(d);
        allDefinedClasses.add(d);
    }

    private void harvest(OWLClass c) {
        String iri = c.getIRI().toString();
        Map<String, String> rec_c = new HashMap<>();
        rec_c.put("o", uri);
        rec_c.put("iri", iri);
        data_c.add(rec_c);
        if (definedClasses.containsKey(c)) {
            int rec_ct = 1;
            for (DefinedClass d : definedClasses.get(c)) {
                rec_c.put("def", rec_ct+"");
                rec_c.put("iri", c.getIRI().toString());
                rec_c.put("pattern", d.getPatternString());
                rec_c.put("grammar_sig", d.getGrammar().getGrammarSignature());
                rec_c.put("grammar", d.getGrammar().getOriginal());
                rec_c.put("eq", OntologyStatsRunner.isEQDefinition(d.getDefiniton()) + "");
                rec_ct++;
            }
        }
    }

    public static void main(String[] args) {

        String uri = args[0];
        String phenoclass = args[1];
        String id = args[2];
        File out = new File(args[3]);
        File outont = new File(args[4]);

        FastPatternSurvey p = new FastPatternSurvey(uri, phenoclass, id,out,outont);
        p.runAnalysis();
        p.exportAll(out);
    }

    private void exportAll(File out) {
        String id = this.id.replaceAll("[^a-zA-z0-9_]", "");
        Export.writeCSV(data_c, new File(out, "data_d_" + id + ".csv"));
    }


}