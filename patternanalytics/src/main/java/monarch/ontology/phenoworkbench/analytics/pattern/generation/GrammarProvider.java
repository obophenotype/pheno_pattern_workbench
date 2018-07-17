package monarch.ontology.phenoworkbench.analytics.pattern.generation;

import java.util.Set;

import monarch.ontology.phenoworkbench.util.DefinedClass;
import monarch.ontology.phenoworkbench.util.PatternGrammar;

public interface GrammarProvider {

	Set<PatternGrammar> getSubsumedGrammars(DefinedClass definedClass);

    int getInstanceCount(PatternGrammar grammar);
}
