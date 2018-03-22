package monarch.ontology.phenoworkbench.analytics.pattern.generation;

import java.util.Set;

public interface PatternProvider {
    Set<? extends OntologyClass> getTopPatterns();
    Set<PatternClass> getPatternsAmongDefinedClasses();
    Set<DefinedClass> getTopDefinedClasses();
    Set<DefinedClass> getAllDefinedClasses();
    Set<OntologyClass> getTopOntologyClasses();
}
