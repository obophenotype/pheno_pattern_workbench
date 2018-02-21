package monarch.ontology.phenoworkbench.browser.analytics;

import monarch.ontology.phenoworkbench.browser.util.OntologyUtils;
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
import java.util.HashSet;
import java.util.Set;

public class BranchLoader {

    private Set<String> branches = new HashSet<>();
    private Set<OWLClass> branchHeads = new HashSet<>();
    private Set<OWLClass> allClassesInBranches = new HashSet<>();
    private Set<OWLClass> unsatisfiableClasses = new HashSet<>();
    private OWLDataFactory df = OWLManager.getOWLDataFactory();

    public BranchLoader(File f, OWLOntology o) {
        loadBranches(f,o.getClassesInSignature(Imports.INCLUDED));
    }

    private void loadBranches(File branchfile, Set<OWLClass> ontologysignature) {
        try {
            branches.addAll(FileUtils.readLines(branchfile, "utf-8"));
            for(String b:branches){
                OWLClass c = df.getOWLClass(IRI.create(b));
                if(ontologysignature.contains(c)) {
                    branchHeads.add(c);
                }
            }
            allClassesInBranches.addAll(branchHeads);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addSubclassesToBranches(OWLReasoner r) {
        allClassesInBranches.addAll(OntologyUtils.getSubClasses(allClassesInBranches, r));
        allClassesInBranches.remove(df.getOWLThing());
        allClassesInBranches.remove(df.getOWLNothing());
        allClassesInBranches.removeAll(unsatisfiableClasses);
    }

    public void addSubclassesToBranches(Reasoner r) {
        addSubclassesToBranches(r.getOWLReasoner());
    }

    public Set<OWLClass> getUnsatisfiableClasses() {
        return unsatisfiableClasses;
    }

    public Set<OWLClass> getAllClassesInBranches() {
        return allClassesInBranches;
    }

    public void addUnsatisfiableClasses(OWLReasoner r) {
        unsatisfiableClasses.addAll(r.getUnsatisfiableClasses().getEntities());
    }

    public Iterable<? extends OWLClass> getBranchHeads() {
        return branchHeads;
    }
}
