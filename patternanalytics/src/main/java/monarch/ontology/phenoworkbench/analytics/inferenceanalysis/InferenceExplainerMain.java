package monarch.ontology.phenoworkbench.analytics.inferenceanalysis;

import monarch.ontology.phenoworkbench.util.*;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owl.explanation.api.Explanation;
import org.semanticweb.owl.explanation.api.ExplanationGenerator;
import org.semanticweb.owl.explanation.api.ExplanationGeneratorFactory;
import org.semanticweb.owl.explanation.api.ExplanationManager;
import org.semanticweb.owl.explanation.impl.blackbox.checker.InconsistentOntologyExplanationGeneratorFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.*;

public class InferenceExplainerMain {

    public static OWLDataFactory df = OWLManager.getOWLDataFactory();
    OWLAxiom entailment_inconsistent = df.getOWLSubClassOfAxiom(df.getOWLThing(), df.getOWLNothing());
    private ExplanationGenerator<OWLAxiom> gen = null;

    InferenceExplainerMain(OWLOntology o, OWLReasoner r) {


        if(!r.isConsistent()) {
            InconsistentOntologyExplanationGeneratorFactory genFacI = new InconsistentOntologyExplanationGeneratorFactory(new ElkReasonerFactory(),1000000);
            gen = genFacI.createExplanationGenerator(o);

        } else {
            ExplanationGeneratorFactory<OWLAxiom> genFac = ExplanationManager.createExplanationGeneratorFactory(new ElkReasonerFactory());
            gen = genFac.createExplanationGenerator(o);
        }
    }

    Set<Explanation<OWLAxiom>> getExplanations(OWLAxiom axiom, int number) {
        return gen.getExplanations(axiom, number);
    }

    public static void main(String[] args) throws OWLOntologyCreationException, FileNotFoundException, OWLOntologyStorageException {
        args = new String[3];
        args[0] = "https://raw.githubusercontent.com/monarch-initiative/monarch-ontology/master/monarch.owl";
        args[2] = "http://purl.obolibrary.org/obo/BFO_0000001";
        args[1] = "http://www.w3.org/2002/07/owl#Thing";
        IRI ontology = IRI.create(args[0]);
        System.out.println("Loading Ontology");
        OWLOntology o = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(ontology);
        OWLDataFactory df = OWLManager.getOWLDataFactory();
        OWLObjectProperty derives = df.getOWLObjectProperty(IRI.create("http://purl.obolibrary.org/obo/RO_0001000"));

        RenderManager labels  = RenderManager.getInstance();
        labels.addLabel(o);

        for(OWLOntology importO:o.getImportsClosure()) {
            System.out.println(importO.getOntologyID());
            importO.getLogicalAxioms(Imports.EXCLUDED).stream().filter(ax->ax.containsEntityInSignature(derives)).forEach(System.out::println);
        }

        System.exit(0);

        SyntacticLocalityModuleExtractor slm = new SyntacticLocalityModuleExtractor(o.getOWLOntologyManager(),o, ModuleType.BOT);
        OWLClass cSub = df.getOWLClass(IRI.create(args[1]));
        OWLClass cSuper = df.getOWLClass(IRI.create(args[2]));
        Set<OWLEntity> key = new HashSet<>();
        key.add(cSub);
        key.add(cSuper);
        Set<OWLAxiom> axs = slm.extract(key);
        OWLOntology omod = OWLManager.createOWLOntologyManager().createOntology(axs);
        omod.getOWLOntologyManager().saveOntology(omod,new FileOutputStream(new File("/data/monarch.mod.owl")));
        Set<OWLClass> unsatisfiable = new HashSet<>();
        OWLReasoner r = new ElkReasonerFactory().createReasoner(omod);

        unsatisfiable.addAll(r.getUnsatisfiableClasses().getEntitiesMinus(df.getOWLNothing()));

        System.out.println("Computing Explanations");
        ExplanationGenerator<OWLAxiom> gen = null;

        if(!r.isConsistent()) {
            InconsistentOntologyExplanationGeneratorFactory genFacI = new InconsistentOntologyExplanationGeneratorFactory(new ElkReasonerFactory(),1000000);
            gen = genFacI.createExplanationGenerator(omod);
            OWLAxiom entailment = df.getOWLSubClassOfAxiom(df.getOWLThing(), df.getOWLNothing());
            Set<Explanation<OWLAxiom>> expl = gen.getExplanations(entailment, 1);
            printExplanations(labels, key, expl);

        } else {


            ExplanationGeneratorFactory<OWLAxiom> genFac = ExplanationManager.createExplanationGeneratorFactory(new ElkReasonerFactory());

            gen = genFac.createExplanationGenerator(omod);
            int i = 0;




            OntologyUtils.p("Generating explanations:");
            for (OWLClass unsat : unsatisfiable) {
                i++;
                if(i>10) {
                    break;
                }
                OWLAxiom entailment = df.getOWLEquivalentClassesAxiom(df.getOWLNothing(), unsat);
                Set<Explanation<OWLAxiom>> expl = gen.getExplanations(entailment, 1);
                printExplanations(labels, Collections.singleton(unsat), expl);
            }
            System.out.println("Compute lost Axioms");

            OWLAxiom entailment = df.getOWLSubClassOfAxiom(cSub, cSuper);
            Set<Explanation<OWLAxiom>> expl = gen.getExplanations(entailment, 1);

            printExplanations(labels, key, expl);

        }


    }

    private static void printExplanations(RenderManager labels, Set<OWLEntity> key, Set<Explanation<OWLAxiom>> expl) {
        for (Explanation<OWLAxiom> ex : expl) {
            ExplanationAnalyser analyser = new ExplantionAnalyserImpl(new monarch.ontology.phenoworkbench.util.Explanation(ex), key, labels);
            analyser.getReport(1).forEach(System.out::println);
        }
    }
}
