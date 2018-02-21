package monarch.ontology.phenoworkbench.unionanalytics;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;

public class TestComplement {

    public static void main(String[] args) {
        //OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        //OWLOntology o = man.createOntology();
        OWLDataFactory df = OWLManager.getOWLDataFactory();
        OWLAxiom ax = df.getOWLEquivalentClassesAxiom(df.getOWLThing(),df.getOWLObjectComplementOf(df.getOWLClass(IRI.create("A"))));
        ax.getNestedClassExpressions().forEach(System.out::println);
    }
}
