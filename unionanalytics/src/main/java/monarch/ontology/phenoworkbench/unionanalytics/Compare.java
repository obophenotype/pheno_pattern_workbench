package monarch.ontology.phenoworkbench.unionanalytics;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.TurtleDocumentFormat;
import org.semanticweb.owlapi.model.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.Set;

public class Compare {
    public static void main(String[] args) throws OWLOntologyCreationException {
        File f1 = new File(args[0]);
        File f2 = new File(args[1]);
        File outfile = new File(args[2]);
        System.out.println("Loading O1");
        OWLOntology o = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(f1);
        System.out.println("Loading O2");
        OWLOntology o2 = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(f2);

        System.out.println("Compute lost Axioms");
        Set<OWLAxiom> axiomslost = new HashSet<>();
        axiomslost.addAll(o.getAxioms());
        axiomslost.removeAll(o2.getAxioms());

        System.out.println("Compute lost Annotations");
        o.getSignature().forEach(a->axiomslost.addAll(o.getAnnotationAssertionAxioms(a.getIRI())));
        o2.getSignature().forEach(a->axiomslost.removeAll(o2.getAnnotationAssertionAxioms(a.getIRI())));

        System.out.println(axiomslost.size());

        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        OWLOntology out = man.createOntology(IRI.create("ebi:compare"));
        man.addAxioms(out, axiomslost);

        try {
            man.saveOntology(out,new TurtleDocumentFormat(),new FileOutputStream(outfile));
        } catch (OWLOntologyStorageException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
