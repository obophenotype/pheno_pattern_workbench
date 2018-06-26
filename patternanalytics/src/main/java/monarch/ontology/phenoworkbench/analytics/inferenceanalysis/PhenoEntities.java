package monarch.ontology.phenoworkbench.analytics.inferenceanalysis;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

public class PhenoEntities {
    private static final OWLDataFactory df = OWLManager.getOWLDataFactory();
    public static final OWLObjectProperty op_inheresinpartof = df.getOWLObjectProperty(IRI.create("http://purl.obolibrary.org/obo/RO_0002314"));
    public static final OWLObjectProperty op_inheresin = df.getOWLObjectProperty(IRI.create("http://purl.obolibrary.org/obo/RO_0002503"));

    public static final OWLObjectProperty op_partof = df.getOWLObjectProperty(IRI.create("http://purl.obolibrary.org/obo/BFO_0000050"));
    public static final OWLObjectProperty op_towards = df.getOWLObjectProperty(IRI.create("http://purl.obolibrary.org/obo/RO_0002503"));

    public static final OWLObjectProperty op_haspart = df.getOWLObjectProperty(IRI.create("http://purl.obolibrary.org/obo/BFO_0000051"));

    public static final OWLClass c_quality=df.getOWLClass(IRI.create("http://purl.obolibrary.org/obo/PATO_0000001"));
}
