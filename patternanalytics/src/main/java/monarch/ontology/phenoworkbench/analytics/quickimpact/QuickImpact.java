package monarch.ontology.phenoworkbench.analytics.quickimpact;

import monarch.ontology.phenoworkbench.analytics.pattern.generation.*;
import monarch.ontology.phenoworkbench.analytics.pattern.impact.DefinedClassImpactCalculator;
import monarch.ontology.phenoworkbench.analytics.pattern.impact.GrammarIndex;
import monarch.ontology.phenoworkbench.analytics.pattern.impact.OntologyClassImpact;
import monarch.ontology.phenoworkbench.analytics.pattern.impact.ImpactMode;
import monarch.ontology.phenoworkbench.util.*;
import monarch.ontology.phenoworkbench.util.Timer;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.util.*;

public class QuickImpact extends PhenoAnalysisRunner implements GrammarProvider,ImpactProvider {

    private final ImpactMode mode;
    private final String patternsiri;
    private DefinedClassImpactCalculator definedClassImpactCalculator;
    private Reasoner r;
    private PatternManager man;
    private PatternProvider patternProviderDefault;
    private int samplesize = -1;
    private ExplanationRenderProvider explanationProvider;
private GrammarIndex grammarIndex;

    public QuickImpact(Set<OntologyEntry> corpus, String patternsiri, ImpactMode mode) {
        super(corpus);
       this.mode = mode;
       this.patternsiri = patternsiri;
    }

    @Override
    public void runAnalysis() {
        String process = "QuickImpact::QuickImpact()";
        Timer.start(process);

        log("QI: Loading Uber Ontology: ",process);

        PatternGenerator patternGenerator = new PatternGenerator(getRenderManager());

        log("QI: Create new Union Ontology..",process);
        Timer.start("QuickImpact::QuickImpact()::o.createNewUberOntology()");
        OWLOntology all = createUnionOntology().orElseThrow(NullPointerException::new);
        Timer.end("QuickImpact::QuickImpact()::o.createNewUberOntology()");

        log("QI: Preparing definedClasses",process);
        Timer.start("QuickImpact::QuickImpact()::preparePatterns()");
        Set<DefinedClass> definedClasses = preparePatterns(patternGenerator, all);
        Timer.end("QuickImpact::QuickImpact()::preparePatterns()");

        log("QI: Preparing pattern reasoner",process);
        Timer.start("QuickImpact::QuickImpact()::preparePatternReasoner()");
        r = preparePatternReasoner(definedClasses, all);
        Timer.end("QuickImpact::QuickImpact()::preparePatternReasoner()");

        Set<DefinedClass> allDefinedClasses = new HashSet<>(definedClasses);

        log("QI: Extract definition definedClasses..",process);
        Timer.start("QuickImpact::QuickImpact()::patternGenerator.extractDefinedClasses");
        allDefinedClasses.addAll(patternGenerator.extractDefinedClasses(getO().getAllAxioms(), true));
        Timer.end("QuickImpact::QuickImpact()::patternGenerator.extractDefinedClasses");

        log("QI: Preparing DefinedClass Manager..",process);
        Timer.start("QuickImpact::QuickImpact()::PatternManager()");
        man = new PatternManager(allDefinedClasses, r, patternGenerator, getRenderManager());
        Timer.end("QuickImpact::QuickImpact()::PatternManager()");

        log("QI: Preparing pattern impact",process);
        Timer.start("QuickImpact::QuickImpact()::DefinedClassImpactCalculator()");
        definedClassImpactCalculator = new DefinedClassImpactCalculator(getO(), r.getUnsatisfiableClasses(), new HashSet<>());
        Timer.end("QuickImpact::QuickImpact()::DefinedClassImpactCalculator()");

        log("QI: Computing impact..",process);
        Timer.start("QuickImpact::QuickImpact()::precomputeImpactMap()");
        definedClassImpactCalculator.precomputeImpactMap(man.getAllDefinedClasses());
        Timer.end("QuickImpact::QuickImpact()::precomputeImpactMap()");
        patternProviderDefault = new PatternProviderDefaultImpl(man);
        explanationProvider = new DefaultExplanationProvider(r,getRenderManager());
        grammarIndex = new GrammarIndex(getAllDefinedClasses());
        log("QI: Done..",process);
        Timer.end("QuickImpact::QuickImpact()");
        Timer.printTimings();
    }

    private Set<DefinedClass> preparePatterns(PatternGenerator patternGenerator, OWLOntology all) {
        Set<DefinedClass> definedClasses = new HashSet<>();
        switch (mode) {
            case EXTERNAL:
                Set<OWLAxiom> axioms = new HashSet<>();
                KB.getInstance().getOntology(patternsiri).ifPresent(ont -> {
                    axioms.addAll(ont.getAxioms(getImports()));
                    this.getRenderManager().addLabel(ont);
                });

                definedClasses.addAll(patternGenerator.extractDefinedClasses(axioms, false));
                break;
            case ALL:
                definedClasses.addAll(patternGenerator.generateDefinitionPatterns(all.getAxioms(getImports()), new Reasoner(all).getOWLReasoner(), samplesize));
                break;
            case THING:
                definedClasses.addAll(patternGenerator.generateThingPatterns(all.getAxioms(getImports())));
                break;
            default:
                definedClasses.addAll(patternGenerator.generateThingPatterns(all.getAxioms(getImports())));
        }
        return definedClasses;
    }


    private Reasoner preparePatternReasoner(Set<DefinedClass> definedClasses, OWLOntology uberOntology) {
        OWLDataFactory df = OWLManager.getOWLDataFactory();
        Set<OWLAxiom> patternAxioms = new HashSet<>();
        for (DefinedClass p : definedClasses) {
            patternAxioms.add(df.getOWLEquivalentClassesAxiom(p.getOWLClass(), p.getDefiniton()));
        }
        uberOntology.getOWLOntologyManager().addAxioms(uberOntology, patternAxioms);
        return new Reasoner(uberOntology);
    }


    public Set<PatternGrammar> getSubsumedGrammars(DefinedClass p) {
        return man.getSubsumedGrammars(p);
    }

    @Override
    public int getInstanceCount(PatternGrammar g) {
        return grammarIndex.getInstanceCount(g);
    }

    public Set<DefinedClass> getAllDefinedClasses() {
        return man.getAllDefinedClasses();
    }

    public Optional<OntologyClassImpact> getImpact(OntologyClass c) {
        return definedClassImpactCalculator.getImpact(c);
    }

    public PatternProvider getPatternProvider() {
        return patternProviderDefault;
    }

    public Set<? extends OntologyClass> getTopPatterns() {
        return getTopPatterns(true);
    }

    public Set<? extends OntologyClass> getTopPatterns(boolean excludeObsolete) {
        return getPatternProvider().getTopPatterns(excludeObsolete);
    }

    public Set<PatternClass> getPatternsAmongDefinedClasses() {
        return getPatternProvider().getPatternsAmongDefinedClasses(true);
    }

    public ExplanationRenderProvider getExplanationProvider() {
        return explanationProvider;
    }
}
