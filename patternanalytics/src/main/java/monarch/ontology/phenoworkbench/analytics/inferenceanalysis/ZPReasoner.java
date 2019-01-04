package monarch.ontology.phenoworkbench.analytics.inferenceanalysis;

import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import java.util.HashSet;
import java.util.Set;

public class ZPReasoner {

    OWLReasoner r_go;
    OWLReasoner r_zfa;
    OWLReasoner r_bspo;
    OWLReasoner r_mpath;
    OWLReasoner r_chebi;
    Set<OWLReasoner> reasoners = new HashSet<>();

    OWLDataFactory df = OWLManager.getOWLDataFactory();
    OWLClass occurrent = df.getOWLClass(IRI.create("http://purl.obolibrary.org/obo/BFO_0000003"));
    OWLClass continuant = df.getOWLClass(IRI.create("http://purl.obolibrary.org/obo/BFO_0000002"));
    Set<OWLClass> unclassified = new HashSet<>();

    ZPReasoner() {
        System.out.println("Loading ZPReasoner");
        try {
            OWLOntology o_go = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(IRI.create("http://purl.obolibrary.org/obo/go.owl"));
            OWLOntology o_bspo = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(IRI.create("http://purl.obolibrary.org/obo/bspo.owl"));
            OWLOntology o_chebi = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(IRI.create("http://purl.obolibrary.org/obo/chebi.owl"));
            OWLOntology o_mpath = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(IRI.create("http://purl.obolibrary.org/obo/mpath.owl"));
            OWLOntology o_zfa = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(IRI.create("https://raw.githubusercontent.com/matentzn/ontologies/master/zfa_inc_uberon.owl"));

            r_go = new ElkReasonerFactory().createReasoner(o_go);
            reasoners.add(r_go);
            r_bspo = new ElkReasonerFactory().createReasoner(o_bspo);
            reasoners.add(r_bspo);
            r_chebi = new ElkReasonerFactory().createReasoner(o_chebi);
            reasoners.add(r_chebi);
            r_mpath = new ElkReasonerFactory().createReasoner(o_mpath);
            reasoners.add(r_mpath);
            r_zfa = new ElkReasonerFactory().createReasoner(o_zfa);
            reasoners.add(r_zfa);
            reasoners.forEach(r->r.precomputeInferences(InferenceType.CLASS_HIERARCHY));
            System.out.println("Unsatisfiable classes in ZPGen: ");
            reasoners.forEach(r->r.getUnsatisfiableClasses().getEntitiesMinus(OWLManager.getOWLDataFactory().getOWLNothing()).forEach(System.out::println));
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }

        System.out.println("Finished loading ZPReasoner");
    }

    boolean isOccurrent(OWLClass c) {
        if(!isConceptClassified(c)) {
            unclassified.add(c);
            return false;
        }
        return isSuperClassOf(c,occurrent);
    }

    private OWLReasoner getReasoner(String s) {
        if(s.startsWith("GO")) {
            return r_go;
        }
        if(s.startsWith("BSPO")) {
            return r_bspo;
        }
        if(s.startsWith("CHEBI")) {
            return r_chebi;
        }
        if(s.startsWith("ZFA")) {
            return r_zfa;
        }
        if(s.startsWith("MPATH")) {
            return r_mpath;
        }
        System.out.println("Unknown entity namespace: "+s);
        return null;
    }

    boolean isContinuant(OWLClass c) {
        if(!isConceptClassified(c)) {
            unclassified.add(c);
            return false;
        }
        return isSuperClassOf(c,continuant);
    }

    private boolean isSuperClassOf(OWLClass c, OWLClass superC) {
        OWLReasoner r = getReasoner(c.getIRI().getRemainder().or(""));
        if(r.getUnsatisfiableClasses().contains(c)) {
            System.out.println("Warning! "+c+" is unsatisfiable!");
            return false;
        }
        return getReasoner(c.getIRI().getRemainder().or("")).getSuperClasses(c,false).getFlattened().contains(superC);
    }

    private boolean isConceptClassified(OWLClass c) {
        return getReasoner(c.getIRI().getRemainder().or("")).getRootOntology().getSignature(Imports.INCLUDED).contains(c);
    }
}
