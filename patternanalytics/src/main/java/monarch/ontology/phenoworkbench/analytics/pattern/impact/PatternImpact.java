package monarch.ontology.phenoworkbench.analytics.pattern.impact;

import monarch.ontology.phenoworkbench.analytics.pattern.Pattern;
import monarch.ontology.phenoworkbench.util.OntologyUtils;
import monarch.ontology.phenoworkbench.util.UberOntology;
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

    private long getimpact = 0;
    private long patternsub = 0;
    private long computemetrics = 0;

    public PatternImpact(UberOntology o, OWLReasoner r,Set<OWLClass> ignore,Set<OWLClass> restrict) {
        this.o = o;
        this.r = r;
        this.ignore = ignore;
        this.restrict = restrict;
    }


    public Impact getImpact(Pattern pattern) {
        long s = System.currentTimeMillis();
        if(!mapPatternImpact.containsKey(pattern)) {
            Impact imp = new Impact(pattern);
            computeMetrics(pattern.getOWLClass(),true,imp);
            computeMetrics(pattern.getOWLClass(),false,imp);
            mapPatternImpact.put(pattern,imp);
        }
        getimpact +=(System.currentTimeMillis()-s);
        return mapPatternImpact.get(pattern);
    }

    public Map<Pattern,Impact> getImpactMap(Set<Pattern> patterns) {
        long s = System.currentTimeMillis();
        Map<Pattern,Impact> impact = new HashMap<>();
        int i = 0;
        for(Pattern c:patterns) {
            i++;
            if(i % 5000 == 0) {
                OntologyUtils.p("Computing metrics: "+i+"/"+patterns.size());
            }
            impact.put(c,getImpact(c));
        }
        System.out.println("getImpact(): "+getimpact);
        System.out.println("getPatternSubclasses(): "+patternsub);
        System.out.println("computeMetrics(): "+computemetrics);
        System.out.println("getImpactMap(): "+(System.currentTimeMillis()-s));
        return impact;
    }

    private void computeMetrics(OWLClass pattern, boolean direct, Impact impact) {
        Set<OWLClass> subs = getPatternSubclasses(pattern, direct);
        long s = System.currentTimeMillis();
        for (OWLClass subclass : subs) {
            // Metric 1: Number of subclasses overall
            impact.incrementImpact(direct);

            // Metric 2: Number of subclasses by ontology
            for (String oid : o.getOids()) {
                if (o.getSignature(oid).contains(subclass)) {
                    impact.incrementImpactByO(oid,direct);
                }
            }
        }
        computemetrics +=(System.currentTimeMillis()-s);
    }

    private Set<OWLClass> getPatternSubclasses(OWLClass namedExtractedDefinition, boolean direct) {
        long s = System.currentTimeMillis();
        Set<OWLClass> subcls = new HashSet<>(r.getSubClasses(namedExtractedDefinition, direct).getFlattened());
        subcls.removeAll(ignore);
        if(!restrict.isEmpty()) {
            subcls.retainAll(restrict);
        }
        patternsub +=(System.currentTimeMillis()-s);
        return subcls;
    }

    public Impact noImpact(Pattern p) {
        return new Impact(p);
    }
}
