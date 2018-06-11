package monarch.ontology.phenoworkbench.analytics.pattern.reconciliation;

import monarch.ontology.phenoworkbench.analytics.pattern.generation.*;
import monarch.ontology.phenoworkbench.analytics.pattern.impact.OntologyClassImpact;
import monarch.ontology.phenoworkbench.util.*;

import java.util.*;

public class CandidateIdentifierApp extends PhenoAnalysisRunner  {

    private Map<String,OntologyTermSet> ontologyTermSets = new HashMap<>();


    public CandidateIdentifierApp(Set<OntologyEntry> corpus) {
        super(corpus);
    }

    public void runAnalysis() {
        String process = "PatternReconciler::runAnalysis()";
        try {
            log("Initialising pattern generator..", process);
            PatternGenerator patternGenerator = new PatternGenerator(getRenderManager());

            log("Create new Union Ontology..", process);
            for(OntologyEntry oid:getO().getOntologyEntries()) {
                ontologyTermSets.put(oid.getOid(),new OntologyTermSet(oid, getO().getAxioms(oid.getOid()),patternGenerator, getRenderManager()));
            }

            log("Done..", "PatternReconciler::runAnalysis()");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Set<OntologyClass> getOntologyClasses(String iri) {
        Set<OntologyClass> classes = new HashSet<>();
        for(OntologyTermSet ts:ontologyTermSets.values()) {
            ts.getTermByIRI(iri).ifPresent(classes::add);
        }
        return classes;
    }

    public Map<String,OntologyTermSet> getCandidatesByOntology() {
        return ontologyTermSets;
    }

    public Optional<OntologyTermSet> getTermSet(String oid) {
        if(ontologyTermSets.containsKey(oid)) {
            return Optional.of(ontologyTermSets.get(oid));
        }
        return Optional.empty();
    }


}
