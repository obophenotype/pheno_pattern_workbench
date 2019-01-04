package monarch.ontology.phenoworkbench.analytics.pattern.reconciliation;

import monarch.ontology.phenoworkbench.analytics.pattern.generation.*;
import monarch.ontology.phenoworkbench.analytics.pattern.impact.OntologyClassImpact;
import monarch.ontology.phenoworkbench.util.*;
import monarch.ontology.phenoworkbench.util.Timer;
import org.semanticweb.owlapi.model.*;

import java.util.*;

public class PatternReconciler extends PhenoAnalysisRunner implements GrammarProvider, ImpactProvider {

    private PatternManager patternManager;
    private Reasoner r;
    private Map<OntologyClass, Map<OntologyClass, PatternReconciliationCandidate>> patternReconciliation = new HashMap<>();
    private long maxReconciliationImpact = 0;
    private PatternProvider patternProvider;
    private ExplanationRenderProvider explanationProvider;

    private boolean bidirectionmapping = false;

    public PatternReconciler(Collection<OntologyEntry> corpus) {
        super(corpus);
    }

    public void runAnalysis() {
        String process = "PatternReconciler::runAnalysis()";
        try {
            log("Initialising pattern generator..", process);
            PatternGenerator patternGenerator = new PatternGenerator(getRenderManager());

            log("Create new Union Ontology..", process);
            OWLOntology all = createUnionOntology().orElseThrow(NullPointerException::new);
            log("Done... Axiomct: " + all.getAxioms(getImports()).size(), process);

            log("Preparing pattern reasoner");
            prepareReasoner(all);

            log("Preparing patterns", process);
            preparePatternManager(patternGenerator, all);
            preparePatternProvider();
            prepareExplanationProvider();
            log("Done..", "PatternReconciler::runAnalysis()");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void prepareExplanationProvider() {
        explanationProvider = new DefaultExplanationProvider(r, getRenderManager());
    }


    private void preparePatternProvider() {
        patternProvider = new PatternProviderDefaultImpl(patternManager);
    }

    private void prepareReasoner(OWLOntology all) {
        r = new Reasoner(all);
    }

    private void preparePatternManager(PatternGenerator patternGenerator, OWLOntology all) {
        patternManager = new PatternManager(extractDefined(patternGenerator, all.getAxioms(getImports())), r, patternGenerator, getRenderManager());
    }


    private Set<DefinedClass> extractDefined(PatternGenerator patternGenerator, Set<OWLAxiom> axioms) {
        return patternGenerator.extractDefinedClasses(axioms, true);
    }

    public Set<PatternReconciliationCandidate> preparePatternMap(Collection<IRIMapping> mapping) {

        Map<IRI, OntologyClass> iriPatternMap = new HashMap<>();
        Set<PatternReconciliationCandidate> reconciliations = new HashSet<>();
System.out.println(patternManager);
        System.out.println(patternManager.getAllClasses().size());

        for (OntologyClass ontologyClass : patternManager.getAllClasses()) {
            try {
                iriPatternMap.put(ontologyClass.getOWLClass().getIRI(), ontologyClass);
            } catch(Exception e) {
                e.printStackTrace();
                System.out.println("Entity causing this: "+ontologyClass);
            }
        }

        log("QI: Computing alignments.." + Timer.getSecondsElapsed("PatternReconciler::PatternReconciler()"));

        for (IRIMapping imap : mapping) {
            IRI iri = imap.getI1();
            IRI to = imap.getI2();
            if (iriPatternMap.containsKey(iri) ) {
                if(iriPatternMap.containsKey(to)) {
                    OntologyClass p = iriPatternMap.get(iri);
                    OntologyClass p2 = iriPatternMap.get(to);
                    indexReconciliationCandidate(imap, p, p2,reconciliations);
                    if (isBidirectionmapping()) {
                        indexReconciliationCandidate(imap, p2, p,reconciliations);
                    }
                }
            }
        }
        reconciliations.forEach(r -> r.setReconciliationEffect(getReconciliationEffect(r)));
        return reconciliations;
    }

    private long getReconciliationEffect(PatternReconciliationCandidate r) {
        Timer.start("PatternReconciler::getReconciliationEffect");

        ReconciliationCandidateSet s1 = getReconciliationsRelatedToClassOrChildren(r.getP1());
        ReconciliationCandidateSet s2 = getReconciliationsRelatedToClassOrChildren(r.getP2());
        ReconciliationCandidateSet union = s1.union(s2);
        long impact = union.items().stream().filter(pcr -> pcr.getReconciliationclass().equals(r.getReconciliationclass())).count();
        if (impact > maxReconciliationImpact) {
            maxReconciliationImpact = impact;
        }
        Timer.end("PatternReconciler::getReconciliationEffect");
        return impact;
    }

    private void indexReconciliationCandidate(IRIMapping imap, OntologyClass p, OntologyClass p2, Set<PatternReconciliationCandidate> reconciliations) {
        Timer.start("PatternReconciler::indexReconciliationCandidate()");
        if (!patternReconciliation.containsKey(p)) {
            patternReconciliation.put(p, new HashMap<>());
        }
        if (!patternReconciliation.get(p).containsKey(p2)) {
            PatternReconciliationCandidate pr = new PatternReconciliationCandidate(p, p2, Optional.of(r));
            pr.setSimiliarity(imap.getSimilarity());
            pr.setOtherMetrics(imap.getMetrics());
            patternReconciliation.get(p).put(p2, pr);

        }
        reconciliations.add(patternReconciliation.get(p).get(p2));
        Timer.start("PatternReconciler::indexReconciliationCandidate()");
    }

    @Override
    public Set<PatternGrammar> getSubsumedGrammars(DefinedClass p) {
        return patternManager.getSubsumedGrammars(p);
    }

    @Override
    public int getInstanceCount(PatternGrammar grammar) {
        return 0;
    }

    @Override
    public Optional<OntologyClassImpact> getImpact(OntologyClass c) {
        return Optional.empty();
    }


    public PatternProvider getPatternProvider() {
        return patternProvider;
    }

    public ReconciliationCandidateSet getReconciliationsRelatedTo(OntologyClass pc) {
        Set<PatternReconciliationCandidate> pcrs = new HashSet<>();
        addReconciliation(pc, pcrs);
        return new ReconciliationCandidateSet(pcrs);
    }

    public ReconciliationCandidateSet getReconciliationsRelatedToClassOrChildren(OntologyClass pc) {
        Timer.start("PatternReconciler::getReconciliationsRelatedToClassOrChildren");
        Set<PatternReconciliationCandidate> pcrs = new HashSet<>();
        addReconciliation(pc, pcrs);
        pc.getNode().indirectChildren().forEach(n->n.getEquivalenceGroup().forEach(c -> addReconciliation(c, pcrs)));
        Timer.end("PatternReconciler::getReconciliationsRelatedToClassOrChildren");
        return new ReconciliationCandidateSet(pcrs);
    }

    private void addReconciliation(OntologyClass pc, Set<PatternReconciliationCandidate> pcrs) {
        if (patternReconciliation.containsKey(pc)) {
            patternReconciliation.get(pc).forEach((k, v) -> pcrs.add(v));
        }
    }

    private boolean isBidirectionmapping() {
        return bidirectionmapping;
    }

    public void setBidirectionmapping(boolean bidirectionmapping) {
        this.bidirectionmapping = bidirectionmapping;
    }

    public ExplanationRenderProvider getExplanationProvider() {
        return explanationProvider;
    }
}
