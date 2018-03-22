package monarch.ontology.phenoworkbench.analytics.pattern.generation;

import java.util.Set;

import monarch.ontology.phenoworkbench.analytics.pattern.generation.DefinedClass;
import monarch.ontology.phenoworkbench.analytics.pattern.generation.PatternGrammar;

public interface GrammarProvider {

	Set<PatternGrammar> getSubsumedGrammars(DefinedClass definedClass);

}
