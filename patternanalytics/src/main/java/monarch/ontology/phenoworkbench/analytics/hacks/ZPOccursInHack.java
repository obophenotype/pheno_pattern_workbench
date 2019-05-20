package monarch.ontology.phenoworkbench.analytics.hacks;

import monarch.ontology.phenoworkbench.util.*;
import org.apache.commons.io.FileUtils;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.OWLObjectDuplicator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class ZPOccursInHack {

    static OWLDataFactory df = OWLManager.getOWLDataFactory();

    public static void main(String[] args) throws OWLOntologyCreationException, IOException, OWLOntologyStorageException {
        File definitionsowl = new File(args[0]);
        File patternfile = new File(args[1]);
        File outowl = new File(args[2]);

        List<String> iris = FileUtils.readLines(patternfile,"utf-8");
        Set<OWLClass> phenotypes = new HashSet<>();
        iris.forEach(i->phenotypes.add(df.getOWLClass(IRI.create(i))));

        IRI occurs_in = IRI.create("http://purl.obolibrary.org/obo/BFO_0000066");
        IRI part_of = IRI.create("http://purl.obolibrary.org/obo/BFO_0000050");

        Map<IRI, IRI> replace = new HashMap<>();
        replace.put(part_of, occurs_in);
        OWLObjectDuplicator replacer = new OWLObjectDuplicator(df, replace);

        System.out.println("Loading Ontology");
        OWLOntology o = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(definitionsowl);
        Set<OWLAxiom> remove = new HashSet<>();
        Set<OWLAxiom> add = new HashSet<>();

        System.out.println("Running replacement");
        for(OWLAxiom ax:o.getAxioms(AxiomType.EQUIVALENT_CLASSES)) {
            for(OWLClassExpression c:ax.getNestedClassExpressions()) {
                if(c.isClassExpressionLiteral()) {
                    if (phenotypes.contains(c)) {
                        OWLAxiom replaced = replacer.duplicateObject(ax);
                        add.add(replaced);
                        remove.add(ax);
                    }
                }
            }
        }

        System.out.println("Saving Ontology");
        o.getOWLOntologyManager().removeAxioms(o,remove);
        o.getOWLOntologyManager().addAxioms(o,add);
        o.getOWLOntologyManager().saveOntology(o,new FileOutputStream(outowl));
        System.out.println("Conversion done");
    }
}