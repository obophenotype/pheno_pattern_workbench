package monarch.ontology.phenoworkbench.analytics.pattern.impact;

import monarch.ontology.phenoworkbench.analytics.pattern.generation.DefinedClass;
import monarch.ontology.phenoworkbench.analytics.pattern.generation.OntologyClass;
import monarch.ontology.phenoworkbench.analytics.pattern.generation.PatternGrammar;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GrammarIndex {

    private Map<PatternGrammar,Set<OntologyClass>> patternIndex = new HashMap<>();


    public GrammarIndex(Set<DefinedClass> classes) {
        for(DefinedClass c:classes) {
            if(!patternIndex.containsKey(c.getGrammar())) {
                patternIndex.put(c.getGrammar(),new HashSet<>());
            }
            patternIndex.get(c.getGrammar()).add(c);
        }
    }

    public int getInstanceCount(PatternGrammar p) {
        if(patternIndex.containsKey(p)) {
            return patternIndex.get(p).size();
        }
        return 0;
    }

}
