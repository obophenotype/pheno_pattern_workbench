package monarch.ontology.phenoworkbench.analytics.pattern.generation;

import monarch.ontology.phenoworkbench.util.DefinedClass;
import monarch.ontology.phenoworkbench.util.OntologyClass;
import monarch.ontology.phenoworkbench.util.PatternClass;

import java.util.Set;

public interface PatternProvider {
    Set<? extends OntologyClass> getTopPatterns(boolean excludeObsolete);
    Set<PatternClass> getPatternsAmongDefinedClasses(boolean excludeObsolete);
    Set<DefinedClass> getTopDefinedClasses(boolean excludeObsolete);
    Set<DefinedClass> getAllDefinedClasses(boolean excludeObsolete);
    Set<OntologyClass> getTopOntologyClasses(boolean excludeObsolete);
}
