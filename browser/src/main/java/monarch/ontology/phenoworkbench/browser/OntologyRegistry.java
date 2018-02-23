package monarch.ontology.phenoworkbench.browser;

import java.util.HashSet;
import java.util.Set;

public class OntologyRegistry {
	
	private final Set<String> phenotypeontologies = new HashSet<>();
	
	public OntologyRegistry() {
		
		addOntology("http://purl.obolibrary.org/obo/upheno/monarch.owl");
		addOntology("http://purl.obolibrary.org/obo/mp.owl");
        addOntology("http://purl.obolibrary.org/obo/hp.owl");
        addOntology("https://raw.githubusercontent.com/FlyBase/flybase-controlled-vocabulary/master/releases/dpo.owl");
        addOntology("http://purl.obolibrary.org/obo/wbphenotype.owl");
        addOntology("http://purl.obolibrary.org/obo/zfa.owl");
        addOntology("http://purl.obolibrary.org/obo/xao.owl");
        addOntology("http://purl.obolibrary.org/obo/MFOEM.owl");
        addOntology("http://purl.obolibrary.org/obo/abo_in_nbo.owl");
        addOntology("http://purl.obolibrary.org/obo/nbo.owl");
        addOntology("http://purl.obolibrary.org/obo/pato.owl");
        addOntology("http://purl.obolibrary.org/obo/uberon.owl");
        addOntology(" http://purl.obolibrary.org/obo/MPATH.owl");
        addOntology("http://purl.obolibrary.org/obo/upheno.owl");
        addOntology("http://purl.obolibrary.org/obo/doid.owl");
        addOntology("http://purl.obolibrary.org/obo/go/extensions/go-plus.owl");

	}

	public Set<String> getOntologies() {
		return phenotypeontologies;
	}
	
	public void addOntology(String iri) {
		getOntologies().add(iri);
	}

}
