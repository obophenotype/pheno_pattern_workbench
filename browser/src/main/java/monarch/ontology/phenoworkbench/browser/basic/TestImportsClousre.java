package monarch.ontology.phenoworkbench.browser.basic;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.io.File;

public class TestImportsClousre {
    public static void main(String[] args) {
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        OWLOntology o = null;
        try {
            o = man.loadOntologyFromOntologyDocument(new File("/Users/matentzn/Downloads/agentrole.owl"));
            System.out.println("Closure");
            for(OWLOntology imp:o.getImportsClosure()) {
                System.out.println(imp.getOntologyID().getOntologyIRI());
            }
            System.out.println("Imports");
            for(OWLOntology imp:o.getImports()) {
                System.out.println(imp.getOntologyID().getOntologyIRI());
            }
            System.out.println("Direct imports");
            for(OWLOntology imp:o.getDirectImports()) {
                System.out.println(imp.getOntologyID().getOntologyIRI());
            }
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }

    }
}
