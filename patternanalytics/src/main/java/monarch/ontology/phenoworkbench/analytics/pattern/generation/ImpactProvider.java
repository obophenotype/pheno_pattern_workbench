package monarch.ontology.phenoworkbench.analytics.pattern.generation;

import java.util.Optional;

import monarch.ontology.phenoworkbench.analytics.pattern.impact.OntologyClassImpact;
import monarch.ontology.phenoworkbench.util.OntologyClass;

public interface ImpactProvider {

	public Optional<OntologyClassImpact> getImpact(OntologyClass c);

}
