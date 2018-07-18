package monarch.ontology.phenoworkbench.analytics.inferenceanalysis;

import monarch.ontology.phenoworkbench.util.DefinedClass;

public interface DefinitionTransformer {
    DefinitionSet get(DefinitionSet basicDefinitions);
    DefinedClass transform(DefinedClass cl);
}
