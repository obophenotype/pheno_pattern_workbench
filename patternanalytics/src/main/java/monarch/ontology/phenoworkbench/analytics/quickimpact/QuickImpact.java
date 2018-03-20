package monarch.ontology.phenoworkbench.analytics.quickimpact;

import monarch.ontology.phenoworkbench.analytics.pattern.generation.*;
import monarch.ontology.phenoworkbench.analytics.pattern.impact.DefinedClassImpactCalculator;
import monarch.ontology.phenoworkbench.analytics.pattern.impact.OntologyClassImpact;
import monarch.ontology.phenoworkbench.analytics.pattern.impact.ImpactMode;
import monarch.ontology.phenoworkbench.util.*;
import monarch.ontology.phenoworkbench.util.Timer;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;

import java.util.*;
import java.util.stream.Collectors;

public class QuickImpact {

    private DefinedClassImpactCalculator definedClassImpactCalculator;
    private UberOntology o;
    private Reasoner r;
    private PatternManager man;

    public QuickImpact(Set<String> corpus, String patternsiri, boolean imports, ImpactMode mode, int samplesize) {
        Timer.start("QuickImpact::QuickImpact()");

        System.out.println("QI: Loading Uber Ontology: " + Timer.getSecondsElapsed("QuickImpact::QuickImpact()"));
        Imports i = imports ? Imports.INCLUDED : Imports.EXCLUDED;
        Timer.start("QuickImpact::QuickImpact()::UberOntology()");
        o = new UberOntology(i, corpus);
        Timer.end("QuickImpact::QuickImpact()::UberOntology()");

        PatternGenerator patternGenerator = new PatternGenerator(o.getRender());

        System.out.println("QI: Create new Uber Ontology.." + Timer.getSecondsElapsed("QuickImpact::QuickImpact()"));
        Timer.start("QuickImpact::QuickImpact()::o.createNewUberOntology()");
        OWLOntology all = o.createNewUberOntology();
        Timer.end("QuickImpact::QuickImpact()::o.createNewUberOntology()");

        System.out.println("QI: Preparing definedClasses" + Timer.getSecondsElapsed("QuickImpact::QuickImpact()"));
        Timer.start("QuickImpact::QuickImpact()::preparePatterns()");
        Set<DefinedClass> definedClasses = preparePatterns(patternsiri, mode, samplesize, i, patternGenerator, all);
        Timer.end("QuickImpact::QuickImpact()::preparePatterns()");

        System.out.println("QI: Preparing pattern reasoner" + Timer.getSecondsElapsed("QuickImpact::QuickImpact()"));
        Timer.start("QuickImpact::QuickImpact()::preparePatternReasoner()");
        r = preparePatternReasoner(definedClasses, all);
        Timer.end("QuickImpact::QuickImpact()::preparePatternReasoner()");

        Set<DefinedClass> allDefinedClasses = new HashSet<>(definedClasses);

        System.out.println("QI: Extract definition definedClasses.." + Timer.getSecondsElapsed("QuickImpact::QuickImpact()"));
        Timer.start("QuickImpact::QuickImpact()::allDefinedClasses.addAll(patternGenerator.extractDefinedClasses(o.getAllAxioms(), true))");
        allDefinedClasses.addAll(patternGenerator.extractDefinedClasses(o.getAllAxioms(), true));
        Timer.end("QuickImpact::QuickImpact()::allDefinedClasses.addAll(patternGenerator.extractDefinedClasses(o.getAllAxioms(), true))");

        System.out.println("QI: Preparing DefinedClass Manager.." + Timer.getSecondsElapsed("QuickImpact::QuickImpact()"));
        Timer.start("QuickImpact::QuickImpact()::PatternManager()");
        man = new PatternManager(allDefinedClasses,r,patternGenerator,o.getRender());
        Timer.end("QuickImpact::QuickImpact()::PatternManager()");

        System.out.println("QI: Preparing pattern impact" + Timer.getSecondsElapsed("QuickImpact::QuickImpact()"));
        Timer.start("QuickImpact::QuickImpact()::DefinedClassImpactCalculator()");
        definedClassImpactCalculator = new DefinedClassImpactCalculator(o, r.getUnsatisfiableClasses(), new HashSet<>());
        Timer.end("QuickImpact::QuickImpact()::DefinedClassImpactCalculator()");

        System.out.println("QI: Computing impact.." + Timer.getSecondsElapsed("QuickImpact::QuickImpact()"));
        Timer.start("QuickImpact::QuickImpact()::precomputeImpactMap()");
        definedClassImpactCalculator.precomputeImpactMap(man.getAllDefinedClasses());
        Timer.end("QuickImpact::QuickImpact()::precomputeImpactMap()");

        System.out.println("QI: Done.." + Timer.getSecondsElapsed("QuickImpact::QuickImpact()"));
        Timer.end("QuickImpact::QuickImpact()");
        Timer.printTimings();
    }

    private Set<DefinedClass> preparePatterns(String patternsfile, ImpactMode mode, int samplesize, Imports i, PatternGenerator patternGenerator, OWLOntology all) {
        Set<DefinedClass> definedClasses = new HashSet<>();
        switch (mode) {
            case EXTERNAL:
                Set<OWLAxiom> axioms = new HashSet<>();
                KB.getInstance().getOntology(patternsfile).ifPresent(ont->{axioms.addAll(ont.getAxioms(i));this.o.getRender().addLabel(ont);});

                definedClasses.addAll(patternGenerator.extractDefinedClasses(axioms, false));
                break;
            case ALL:
                definedClasses.addAll(patternGenerator.generateDefinitionPatterns(all.getAxioms(i), new Reasoner(all).getOWLReasoner(),samplesize));
                break;
            case THING:
                definedClasses.addAll(patternGenerator.generateThingPatterns(all.getAxioms(i)));
                break;
            default:
                definedClasses.addAll(patternGenerator.generateThingPatterns(all.getAxioms(i)));
        }
        return definedClasses;
    }


    private Reasoner preparePatternReasoner(Set<DefinedClass> definedClasses, OWLOntology uberOntology) {
        OWLDataFactory df = OWLManager.getOWLDataFactory();
        Set<OWLAxiom> patternAxioms = new HashSet<>();
        for(DefinedClass p: definedClasses) {
            patternAxioms.add(df.getOWLEquivalentClassesAxiom(p.getOWLClass(),p.getDefiniton()));
        }
        uberOntology.getOWLOntologyManager().addAxioms(uberOntology,patternAxioms);
        return new Reasoner(uberOntology);
    }



    public Set<PatternGrammar> getSubsumedGrammars(DefinedClass p) {
        return man.getSubsumedGrammars(p);
    }

    public Set<DefinedClass> getAllDefinedClasses() {
        return man.getAllDefinedClasses();
    }

    public Set<PatternClass> getTopPatterns() {
        Timer.start("QuickImpact::getTopPatterns()");
        Set<PatternClass> patterns = new HashSet<>();
        new HashSet<>();
        Timer.start("QuickImpact::getTopPatterns()::getPatternsAmongDefinedClasses");
        Set<PatternClass> allPatternClasses = getPatternsAmongDefinedClasses();
        Set<OWLClass> allPatternOWLClasses = allPatternClasses.stream().map(PatternClass::getOWLClass).collect(Collectors.toSet());
        Timer.end("QuickImpact::getTopPatterns()::getPatternsAmongDefinedClasses");

        for (PatternClass c : allPatternClasses) {
            Timer.start("QuickImpact::getTopPatterns()::getSuperClasses:noneMatch");
            if (c.indirectParents().stream().noneMatch(parent->allPatternOWLClasses.contains(parent.getOWLClass()))) {
                patterns.add(c);
            }
            Timer.end("QuickImpact::getTopPatterns()::getSuperClasses:noneMatch");
        }
        System.out.println("TOP:"+patterns.size());

        Timer.end("QuickImpact::getTopPatterns()");
        return patterns;
    }

    public Set<PatternClass> getPatternsAmongDefinedClasses() {
        return getAllDefinedClasses().stream().filter(PatternClass.class::isInstance).map(PatternClass.class::cast).collect(Collectors.toSet());
    }

    public Optional<OntologyClassImpact> getImpact(OntologyClass c) {
        return definedClassImpactCalculator.getImpact(c);
    }

    private Optional<Explanation> getSubsumptionExplanation(OntologyClass c, OntologyClass p) {
        return r.getExplanation(c.getOWLClass(),p.getOWLClass());
    }

    public Optional<ExplanationAnalyser> getSubsumptionExplanationRendered(OntologyClass subC, OntologyClass superC) {
        Optional<Explanation> explanation = getSubsumptionExplanation(subC,superC);
        if(explanation.isPresent()) {
            return Optional.of(new ExplantionAnalyserImpl(explanation.get(),new HashSet<>(),o.getRender()));
        }
        return Optional.empty();
    }
}
