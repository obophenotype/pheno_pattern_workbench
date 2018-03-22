package monarch.ontology.phenoworkbench.analytics.pattern.reconciliation;

import monarch.ontology.phenoworkbench.analytics.pattern.generation.*;
import monarch.ontology.phenoworkbench.analytics.pattern.impact.OntologyClassImpact;
import monarch.ontology.phenoworkbench.util.*;
import monarch.ontology.phenoworkbench.util.Timer;
import org.apache.commons.io.FileUtils;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;

import java.io.File;
import java.nio.charset.Charset;
import java.util.*;

public class PatternReconciler implements GrammarProvider, ImpactProvider, ExplanationRenderProvider {

    private UberOntology o;
    private PatternManager patternManager;
    private Reasoner r;
    private Map<OntologyClass, Map<OntologyClass, PatternReconciliationCandidate>> patternReconciliation;
    private List<PatternReconciliationCandidate> reconciliations = new ArrayList<>();
    private long maxReconciliationImpact = 0;
    private PatternProvider patternProvider;

    public PatternReconciler(Set<String> corpus, File mappings, boolean imports, boolean lazyalign, boolean bidirectionmapping, double confidencethreshold) {
        Timer.start("PatternReconciler::PatternReconciler()");
        try {
            System.out.println("QI: Loading Uber Ontology: " + Timer.getSecondsElapsed("PatternReconciler::PatternReconciler()"));
            Imports i = imports ? Imports.INCLUDED : Imports.EXCLUDED;
            o = new UberOntology(i, corpus);
            System.out.println("QI: Initialising pattern generator.." + Timer.getSecondsElapsed("PatternReconciler::PatternReconciler()"));
            PatternGenerator patternGenerator = new PatternGenerator(o.getRender());
            System.out.println("QI: Create new Uber Ontology.." + Timer.getSecondsElapsed("PatternReconciler::PatternReconciler()"));
            OWLOntology all = o.createNewUberOntology();
            System.out.println("Done... Axiomct: " + o.getAllAxioms().size() + Timer.getSecondsElapsed("PatternReconciler::PatternReconciler()"));
            System.out.println("QI: Preparing pattern reasoner" + Timer.getSecondsElapsed("PatternReconciler::PatternReconciler()"));
            r = new Reasoner(all);
            Set<DefinedClass> allDefinedClasses = new HashSet<>(patternGenerator.extractDefinedClasses(all.getAxioms(i), true));
            patternManager = new PatternManager(allDefinedClasses,r,patternGenerator,o.getRender());
            System.out.println("QI: Preparing patterns" + Timer.getSecondsElapsed("PatternReconciler::PatternReconciler()"));
            patternReconciliation = preparePatternMap(mappings, bidirectionmapping, lazyalign, confidencethreshold);
            patternProvider = new PatternProviderDefaultImpl(patternManager);
            //patternImpact = new DefinedClassImpactCalculator(o, r.getOWLReasoner(), r.getUnsatisfiableClasses(), new HashSet<>());
            System.out.println("QI: Computing impact.." + Timer.getSecondsElapsed("PatternReconciler::PatternReconciler()"));
            //patternImpactMap = patternImpact.precomputeImpactMap(allDefinedClasses);

            System.out.println("QI: Done.." + Timer.getSecondsElapsed("PatternReconciler::PatternReconciler()"));


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private Map<OntologyClass, Map<OntologyClass, PatternReconciliationCandidate>> preparePatternMap(File mappinFile, boolean bidirectionmapping, boolean lazyalign, double confidencethreshold) {
        Map<OntologyClass, Map<OntologyClass, PatternReconciliationCandidate>> patternReconciliation = new HashMap<>();
        List<IRIMapping> mapping = parseMappings(mappinFile, confidencethreshold);
        Map<IRI, DefinedClass> iriPatternMap = new HashMap<>();
        Map<String,Long> mapGrammarEffect = new HashMap();

        patternManager.getAllDefinedClasses().forEach(p -> iriPatternMap.put(p.getOWLClass().getIRI(), p));
        if (!lazyalign) {
            System.out.println("QI: Computing alignments.." + Timer.getSecondsElapsed("PatternReconciler::PatternReconciler()"));

            for (IRIMapping imap : mapping) {
                IRI iri = imap.getI1();
                IRI to = imap.getI2();
                indexReconciliationCandidate(patternReconciliation, iriPatternMap, mapGrammarEffect,imap,iri,to);
                if(bidirectionmapping) {
                    indexReconciliationCandidate(patternReconciliation, iriPatternMap, mapGrammarEffect,imap,to,iri);
                }

            }
        }
        reconciliations.forEach(r->r.setReconciliationEffect(mapGrammarEffect.get(r.getReconciliationclass())));
        maxReconciliationImpact = Collections.max(mapGrammarEffect.values());
        return patternReconciliation;
    }

    private void indexReconciliationCandidate(Map<OntologyClass, Map<OntologyClass, PatternReconciliationCandidate>> patternReconciliation, Map<IRI, DefinedClass> iriPatternMap, Map<String,Long> mapGrammarEffect, IRIMapping imap, IRI iri, IRI to) {
        Timer.start("PatternReconciler::indexReconciliationCandidate()");

        if (iriPatternMap.containsKey(iri) && iriPatternMap.containsKey(to)) {
            DefinedClass p = iriPatternMap.get(iri);
            DefinedClass p2 = iriPatternMap.get(to);
            if (!patternReconciliation.containsKey(p)) {
                patternReconciliation.put(p, new HashMap<>());
            }
            if (!patternReconciliation.get(p).containsKey(p2)) {
                PatternReconciliationCandidate pr = new PatternReconciliationCandidate(p, p2, o.getRender(), r);
                pr.setJaccardSimiliarity(imap.getJackard());
                pr.setSubclassSimilarity(imap.getSbcl());
                patternReconciliation.get(p).put(p2, pr);
                reconciliations.add(pr);
                incrementGrammarEffect(mapGrammarEffect, pr);
            }
        }
        Timer.start("PatternReconciler::indexReconciliationCandidate()");
    }

    private void incrementGrammarEffect(Map<String, Long> mapGrammarEffect, PatternReconciliationCandidate pr) {
        String reconclass = pr.getReconciliationclass();
        if(!mapGrammarEffect.containsKey(reconclass)) {
            mapGrammarEffect.put(reconclass,0l);
        }
        mapGrammarEffect.put(reconclass,mapGrammarEffect.get(reconclass)+1);
    }

    private List<IRIMapping> parseMappings(File mappinFile, double confidencethreshold) {

        List<IRIMapping> mappings = new ArrayList<>();
        if (mappinFile.isFile()) {
            try {
                List<String> lines = FileUtils.readLines(mappinFile, Charset.forName("utf-8"));
                for (String line : lines) {
                    String[] explosion = line.split("\t");
                    IRI iri1 = getIRI(explosion[0]);
                    //String logo1 = explosion[1];
                    IRI iri2 = getIRI(explosion[2]);
                    //String logo2 = explosion[3];
                    Double v1 = Double.valueOf(explosion[4]);
                    Double v2 = Double.valueOf(explosion[5]);
                    if (v1 < 0 || v1 > confidencethreshold) {
                        mappings.add(new IRIMapping(iri1, iri2, v1, v2));
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return mappings;
    }

    private IRI getIRI(String s) {
        String v = s;
        if (v.contains(":")) {
            String prefix = v.substring(0, v.indexOf(":"));
            v = v.replace(prefix + ":", "http://purl.obolibrary.org/obo/" + prefix + "_");
        }
        return IRI.create(v);
    }

    public ReconciliationCandidateSet getAllPatternReconciliations() {
        return new ReconciliationCandidateSet(reconciliations);
    }

    private Optional<Explanation> getSubsumptionExplanation(OntologyClass c, OntologyClass p) {
        return r.getExplanation(c.getOWLClass(), p.getOWLClass());
    }

    public Optional<ExplanationAnalyser> getSubsumptionExplanationRendered(OntologyClass subC, OntologyClass superC) {
        Optional<Explanation> explanation = getSubsumptionExplanation(subC, superC);
        if (explanation.isPresent()) {
            return Optional.of(new ExplantionAnalyserImpl(explanation.get(), new HashSet<>(), o.getRender()));
        }
        return Optional.empty();
    }

    @Override
    public Set<PatternGrammar> getSubsumedGrammars(DefinedClass p) {
        return patternManager.getSubsumedGrammars(p);
    }

    @Override
    public Optional<OntologyClassImpact> getImpact(OntologyClass c) {
        return Optional.empty();
    }

    public long getMaxReconciliationImpact() {
        return maxReconciliationImpact;
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
        pc.indirectChildren().forEach(c-> {
            addReconciliation(c, pcrs);
        } );
        Timer.end("PatternReconciler::getReconciliationsRelatedToClassOrChildren");
        return new ReconciliationCandidateSet(pcrs);
    }

    private void addReconciliation(OntologyClass pc, Set<PatternReconciliationCandidate> pcrs) {
        if(patternReconciliation.containsKey(pc)) {
            patternReconciliation.get(pc).forEach((k,v)->pcrs.add(v));
        }
    }
}
