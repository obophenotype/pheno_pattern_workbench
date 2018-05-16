package monarch.ontology.phenoworkbench.util;

import org.apache.commons.io.FileUtils;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class BranchLoader {

    //private Set<OWLClass> branchHeads = new HashSet<>();
    private Set<OWLClass> allClassesInBranches = new HashSet<>();
    private Set<OWLClass> unsatisfiableClasses = new HashSet<>();
    private OWLDataFactory df = OWLManager.getOWLDataFactory();

    /*
    public BranchLoader(File f, OWLOntology o) {
        try {
            loadBranches(FileUtils.readLines(f, "utf-8"),o.getClassesInSignature(Imports.INCLUDED));
        } catch (IOException e) {
            e.printStackTrace();
        }
    } */

    public BranchLoader() {

    }

    public void loadBranches(Collection<String> bs, Set<OWLClass> ontologysignature, boolean add_owl_thing) {
        loadBranches(bs,ontologysignature,add_owl_thing,null);
    }

    public void loadBranches(Collection<String> bs, Set<OWLClass> ontologysignature, boolean add_owl_thing, OWLReasoner r) {
        for(String b:bs){
            OWLClass c = df.getOWLClass(IRI.create(b));
            if(ontologysignature.contains(c)||(add_owl_thing&&c.equals(df.getOWLThing()))) {
                allClassesInBranches.add(c);
            }
        }
        if(r!=null) {
            addUnsatisfiableClasses(r);
            addSubclassesToBranches(r);
        }
    }

    private void addSubclassesToBranches(OWLReasoner r) {
        allClassesInBranches.addAll(OntologyUtils.getSubClasses(allClassesInBranches, r));
        allClassesInBranches.remove(df.getOWLThing());
        allClassesInBranches.remove(df.getOWLNothing());
        allClassesInBranches.removeAll(unsatisfiableClasses);
    }

    public Set<OWLClass> getUnsatisfiableClasses() {
        return unsatisfiableClasses;
    }

    public Set<OWLClass> getAllClassesInBranches() {
        return allClassesInBranches;
    }

    public void addUnsatisfiableClasses(OWLReasoner r) {
        unsatisfiableClasses.addAll(r.getUnsatisfiableClasses().getEntities());
        allClassesInBranches.removeAll(unsatisfiableClasses);
    }

}
