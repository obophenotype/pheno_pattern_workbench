package monarch.ontology.phenoworkbench.analytics.pattern.reconciliation;

import monarch.ontology.phenoworkbench.analytics.pattern.generation.*;
import monarch.ontology.phenoworkbench.util.*;

import java.util.*;
import java.util.stream.Collectors;

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
                OntologyTermSet ts = new OntologyTermSet(oid, getO().getAxioms(oid.getOid()),patternGenerator, getRenderManager());
                ontologyTermSets.put(oid.getOid(),ts);
                KB.getInstance().cacheClasses(oid.getOid(),ts.items());
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


    public Collection<OntologyClassMatch> getSuggestions(OntologyClass p, Collection<Bucket> buckets) {
        List<OntologyClassMatch> suggested = new ArrayList<>();
        for(String oid:getCandidatesByOntology().keySet()) {
            for(OntologyClass c:getCandidatesByOntology().get(oid).items()) {
                double jacc_super = jacc_super(p,c);
                double jacc_substring = jacc_substring(p,c);
                double jacc_bucketsim = jacc_bucketsim(p,c,buckets);
                double matchstrength = (jacc_super+jacc_substring+jacc_bucketsim)/3;
                if(matchstrength>0.01) {
                    OntologyClassMatch match = new OntologyClassMatch(p,c);
                    match.setJacc_bucketsim(jacc_bucketsim);
                    match.setJacc_substring(jacc_substring);
                    match.setJacc_super(jacc_super);
                    match.setMatchstrength(matchstrength);
                    suggested.add(match);
                }
            }
        }
        return suggested;
    }

    private double jacc_substring(OntologyClass c1, OntologyClass c2) {
        Set<String> sub_c1 = new HashSet<>();
        sub_c1.addAll(Arrays.asList(c1.getLabel().split(" ")));
        Set<String> sub_c2 = new HashSet<>();
        sub_c2.addAll(Arrays.asList(c2.getLabel().split(" ")));
        return jacc(sub_c1,sub_c2);
    }

    private Set<String> iris(Set<OntologyClass> classes) {
        return classes.stream().map(OntologyClass::getIri).collect(Collectors.toSet());
    }

    private double jacc(Collection<String> s1, Collection<String> s2) {
        Set<String> inter = new HashSet<>(s1);
        inter.retainAll(s2);
        Set<String> union = new HashSet<>(s1);
        union.addAll(s2);
        return ((double)inter.size())/((double)union.size());
    }

    private double jacc_super(OntologyClass c1, OntologyClass c2) {
        return jacc(iris(c1.indirectParents()), iris(c2.indirectParents()));
    }

    private double jacc_bucketsim(OntologyClass c1, OntologyClass c2, Collection<Bucket> buckets) {
        List<Double> bucketsims = new ArrayList<>();
        for(Bucket b:buckets) {
            Map<String, String> search = b.getBucket();
            for(String oid:search.keySet()) {
                Set<String> results = new HashSet<>();
                getTermSet(oid).ifPresent(t->results.addAll(t.searchTerms(search.get(oid)).stream().map(term->term.getIri()).collect(Collectors.toSet())));
                if(results.contains(c1.getIri())&&results.contains(c1.getIri())) {
                    bucketsims.add(1.0/(double)results.size());
                }
            }
        }
        return bucketsims.stream().mapToDouble(c->c).max().orElse(0.0);
    }
}
