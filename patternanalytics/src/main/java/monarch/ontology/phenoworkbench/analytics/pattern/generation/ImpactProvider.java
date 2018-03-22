package monarch.ontology.phenoworkbench.analytics.pattern.generation;

import java.util.Optional;

import monarch.ontology.phenoworkbench.analytics.pattern.generation.DefinedClass;
import monarch.ontology.phenoworkbench.analytics.pattern.impact.OntologyClassImpact;

public interface ImpactProvider {

	public Optional<OntologyClassImpact> getImpact(OntologyClass c);

}
