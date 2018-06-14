package monarch.ontology.phenoworkbench.analytics.inferenceanalysis;

import monarch.ontology.phenoworkbench.analytics.pattern.generation.DefinedClass;

import java.util.HashSet;
import java.util.Set;

public class DefinitionSet {
    private Set<DefinedClass> definitions = new HashSet<>();
    public void setDefinitions(Set<DefinedClass> definitions) {
        this.definitions = definitions;
    }

    public Set<DefinedClass> getDefinitions() {
        return definitions;
    }
}
