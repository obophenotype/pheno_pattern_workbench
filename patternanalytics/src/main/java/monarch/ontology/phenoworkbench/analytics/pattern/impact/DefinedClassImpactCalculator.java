package monarch.ontology.phenoworkbench.analytics.pattern.impact;

import monarch.ontology.phenoworkbench.analytics.pattern.generation.OntologyClass;
import monarch.ontology.phenoworkbench.util.OntologyUtils;
import monarch.ontology.phenoworkbench.util.Timer;
import monarch.ontology.phenoworkbench.util.UberOntology;
import org.semanticweb.owlapi.model.OWLClass;

import java.util.*;

public class DefinedClassImpactCalculator {

    private UberOntology o;
    private Set<OWLClass> ignore;
    private Set<OWLClass> restrict;
    private Map<OntologyClass,OntologyClassImpact> mapPatternImpact = new HashMap<>();

    public DefinedClassImpactCalculator(UberOntology o, Set<OWLClass> ignore, Set<OWLClass> restrict) {
        this.o = o;
        this.ignore = ignore;
        this.restrict = restrict;
    }


    public Optional<OntologyClassImpact> getImpact(OntologyClass pattern) {
        return Optional.ofNullable(mapPatternImpact.get(pattern));
    }

    private void computePatternImpact(OntologyClass pattern) {
        Timer.start("DefinedClassImpactCalculator::precomputeImpactMap");
        if(!mapPatternImpact.containsKey(pattern)) {
            OntologyClassImpact imp = new OntologyClassImpact(pattern);
            computeMetrics(pattern,true,imp);
            computeMetrics(pattern,false,imp);
            mapPatternImpact.put(pattern,imp);
        }
        Timer.end("DefinedClassImpactCalculator::precomputeImpactMap");
    }

    public void precomputeImpactMap(Set<? extends OntologyClass> patterns) {
        Timer.start("DefinedClassImpactCalculator::precomputeImpactMap");
        int i = 0;
        for(OntologyClass pattern:patterns) {
            i++;
            if(i % 5000 == 0) {
                OntologyUtils.p("Computing metrics: "+i+"/"+patterns.size());
            }
            computePatternImpact(pattern);
        }
        Timer.end("DefinedClassImpactCalculator::precomputeImpactMap");
    }

    private void computeMetrics(OntologyClass pattern, boolean direct, OntologyClassImpact impact) {
        Set<OWLClass> subs = getPatternSubclasses(pattern, direct);
        Timer.start("DefinedClassImpactCalculator::computeMetrics");
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
        Timer.end("DefinedClassImpactCalculator::computeMetrics");
    }

    private Set<OWLClass> getPatternSubclasses(OntologyClass pattern, boolean direct) {
        Timer.start("DefinedClassImpactCalculator::getPatternSubclasses");

        Set<OWLClass> subcls = new HashSet<>();
        if(direct) {
            pattern.directChildren().forEach(child->subcls.add(child.getOWLClass()));
        } else {
            pattern.indirectChildren().forEach(child->subcls.add(child.getOWLClass()));
        }
        subcls.removeAll(ignore);
        if(!restrict.isEmpty()) {
            subcls.retainAll(restrict);
        }
        Timer.end("DefinedClassImpactCalculator::getPatternSubclasses");
        return subcls;
    }
}
