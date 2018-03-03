package monarch.ontology.phenoworkbench.browser.analytics;

import monarch.ontology.phenoworkbench.browser.util.OntologyUtils;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PatternImpact {

    private UberOntology o;
    private OWLReasoner r;
    private Set<OWLClass> ignore;
    private Set<OWLClass> restrict;
    private Map<Pattern,Impact> mapPatternImpact = new HashMap<>();

    public PatternImpact(UberOntology o, OWLReasoner r,Set<OWLClass> ignore,Set<OWLClass> restrict) {
        this.o = o;
        this.r = r;
        this.ignore = ignore;
        this.restrict = restrict;
    }

    public Impact getImpact(Pattern pattern) {
        if(!mapPatternImpact.containsKey(pattern)) {
            Impact imp = new Impact(pattern);
            computeMetrics(pattern.getOWLClass(),true,imp);
            computeMetrics(pattern.getOWLClass(),false,imp);
            mapPatternImpact.put(pattern,imp);
        }
        return mapPatternImpact.get(pattern);
    }

    public Map<Pattern,Impact> getImpactMap(Set<Pattern> patterns) {
        Map<Pattern,Impact> impact = new HashMap<>();
        int i = 0;
        for(Pattern c:patterns) {
            i++;
            if(i % 5000 == 0) {
                OntologyUtils.p("Computing metrics: "+i+"/"+patterns.size());
            }
            impact.put(c,getImpact(c));
        }
        return impact;
    }

    private void computeMetrics(OWLClass pattern, boolean direct, Impact impact) {
        for (OWLClass subclass : getPatternSubclasses(pattern, direct)) {
            // Metric 1: Number of subclasses overall
            impact.incrementImpact(direct);

            // Metric 2: Number of subclasses by ontology
            for (String oid : o.getOids()) {
                if (o.getSignature(oid).contains(subclass)) {
                    impact.incrementImpactByO(oid,direct);
                }
            }
        }
    }

    private Set<OWLClass> getPatternSubclasses(OWLClass namedExtractedDefinition, boolean direct) {
        Set<OWLClass> subcls = new HashSet<>(r.getSubClasses(namedExtractedDefinition, direct).getFlattened());
        subcls.removeAll(ignore);
        if(!restrict.isEmpty()) {
            subcls.retainAll(restrict);
        }
        return subcls;
    }

    public Impact noImpact(Pattern p) {
        return new Impact(p);
    }

    public class Impact {
        Map<String, Integer> directImpactByO = new HashMap<>();
        Map<String, Integer> indirectImpactByO = new HashMap<>();
        int directImpact = 0;
        int indirectImpact = 0;

        Impact(Pattern pattern) {
        }

        private void incrementDirectImpactByO(String oid) {
            if (!directImpactByO.containsKey(oid)) {
                directImpactByO.put(oid, 0);
            }
            directImpactByO.put(oid, directImpactByO.get(oid) + 1);
        }

        private void incrementIndirectImpactByO(String oid) {
            if (!indirectImpactByO.containsKey(oid)) {
                indirectImpactByO.put(oid, 0);
            }
            indirectImpactByO.put(oid, indirectImpactByO.get(oid) + 1);
        }

        private void incrementImpactByO(String oid, boolean direct) {
            if(direct) {
                incrementDirectImpactByO(oid);
            } else {
                incrementIndirectImpactByO(oid);
            }
        }

        private void incrementImpact(boolean direct) {
            if(direct) {
                directImpact++;
            } else {
                indirectImpact++;
            }
        }

        public int getDirectImpact() {
            return directImpact;
        }

        public int getIndirectImpact() {
            return indirectImpact;
        }

        public Map<String,Integer> getDirectImpactByO() {
            return directImpactByO;
        }

        public Map<String,Integer> getIndirectImpactByO() {
            return indirectImpactByO;
        }
    }
}
