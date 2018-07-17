package monarch.ontology.phenoworkbench.analytics.pattern.generation;

import java.util.Optional;

import monarch.ontology.phenoworkbench.util.OntologyClass;
import monarch.ontology.phenoworkbench.util.ExplanationAnalyser;

public interface ExplanationRenderProvider {

	Optional<ExplanationAnalyser> getSubsumptionExplanationRendered(OntologyClass current, OntologyClass p);

}
