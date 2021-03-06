package monarch.ontology.phenoworkbench.analytics.inferenceanalysis;

import monarch.ontology.phenoworkbench.util.Subsumption;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SubsumptionUtils {
    public static Map<OWLClass,Set<OWLClass>> getSuperClassMap(OWLReasoner r, OWLOntology o) {
        Map<OWLClass,Set<OWLClass>> superclasses = new HashMap<>();
        if(r!=null) {
            for(OWLClass c:r.getRootOntology().getClassesInSignature(Imports.INCLUDED)) {
                if(!superclasses.containsKey(c)) {
                    superclasses.put(c,new HashSet<>());
                }
                for (OWLClass s : r.getSuperClasses(c, true).getFlattened()) {
                    superclasses.get(c).add(s);
                }
            } }
        else {
            for(OWLAxiom ax:o.getAxioms(Imports.INCLUDED)) {
                if(ax instanceof OWLSubClassOfAxiom){
                    OWLSubClassOfAxiom sbcl = (OWLSubClassOfAxiom)ax;
                    OWLClassExpression subc = sbcl.getSubClass();
                    OWLClassExpression superc = sbcl.getSuperClass();
                    if(!subc.isAnonymous() && !superc.isAnonymous()) {
                        OWLClass c = (OWLClass)subc;
                        if(!superclasses.containsKey(c)) {
                            superclasses.put(c,new HashSet<>());
                        }
                        superclasses.get(c).add((OWLClass)superc);
                    }
                }
            }

        }
        return superclasses;
    }

    public static Set<Subsumption> getSubsumptions(OWLReasoner r, OWLOntology o,boolean direct) {
        Set<Subsumption> subs = new HashSet<>();
        if(r!=null) {
            for(OWLClass c:r.getRootOntology().getClassesInSignature(Imports.INCLUDED)) {
                for (OWLClass sub : r.getSubClasses(c, direct).getFlattened()) {
                    subs.add(new Subsumption(c, sub));
                }
            } }
        else {
            return getAssertedSubsumptions(o);
        }
        return subs;
    }

    public static Set<Subsumption> getAssertedSubsumptions(OWLOntology o) {
        Set<Subsumption> subs = new HashSet<>();
        for(OWLAxiom ax:o.getAxioms(Imports.INCLUDED)) {
            if(ax instanceof OWLSubClassOfAxiom){
                OWLSubClassOfAxiom sbcl = (OWLSubClassOfAxiom)ax;
                addSubClassOfIfNamedClass(subs, sbcl);
            } else if(ax instanceof OWLEquivalentClassesAxiom){
                OWLEquivalentClassesAxiom eq = (OWLEquivalentClassesAxiom)ax;
                for(OWLSubClassOfAxiom sbcl:eq.asOWLSubClassOfAxioms()) {
                    addSubClassOfIfNamedClass(subs, sbcl);
                }
            }
        }
        return subs;
    }

    private static void addSubClassOfIfNamedClass(Set<Subsumption> subs, OWLSubClassOfAxiom sbcl) {
        OWLClassExpression subc = sbcl.getSubClass();
        OWLClassExpression superc = sbcl.getSuperClass();
        if(!subc.isAnonymous() && !superc.isAnonymous()) {
            subs.add(new Subsumption((OWLClass)superc,(OWLClass)subc));
        }
    }
}
