package monarch.ontology.phenoworkbench.analytics.subclassredundancy;

import com.google.common.collect.Sets;
import monarch.ontology.phenoworkbench.util.BranchLoader;
import monarch.ontology.phenoworkbench.util.*;
import org.apache.commons.io.FileUtils;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SubClassRedundancy {
    private BranchLoader branches = null;
    private RenderManager render = new RenderManager();
    OntologyDebugReport lines = new OntologyDebugReport();

    private String BASEIRI = "http://ebi.ac.uk/";

    public SubClassRedundancy(String iri, File branchfile) {



        p("# Analysing: "+iri);
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        try {
            p("* Loading ontology..");
            OWLOntology base = KB.getInstance().getOntology(iri).get();
            render.addLabel(base);
            branches = new BranchLoader(branchfile,base);
            p("* done.."+Timer.getSecondsElapsed("SubClassRedundancy::SubClassRedundancy()"));

            Set<OWLAxiom> all_axioms = new HashSet<>(base.getAxioms(Imports.INCLUDED));
            OWLOntology o_all = OWLManager.createOWLOntologyManager().createOntology(all_axioms, IRI.create(BASEIRI + "o_all"));

            Set<OWLOntology> imports_wo_base = new HashSet<>();
            imports_wo_base.addAll(base.getImportsClosure());
            imports_wo_base.remove(base);

            p("## Imports");
            Set<OWLAxiom> allaxiomsexceptbase = new HashSet<>();
            imports_wo_base.forEach(i->p(i.getOntologyID().getOntologyIRI().or(IRI.create("NOIRI")).toString()));
            imports_wo_base.forEach(i->allaxiomsexceptbase.addAll(i.getAxioms(Imports.EXCLUDED)));

            p("* Creating analysis ontologies..");
            OWLOntology o_exceptbase = OWLManager.createOWLOntologyManager().createOntology(allaxiomsexceptbase, IRI.create(BASEIRI + "o_except_base"));
            //
            //
            p("* done.."+Timer.getSecondsElapsed("SubClassRedundancy::SubClassRedundancy()"));

            p("* Performing reasoning..");

            Reasoner r_exceptbase = new Reasoner(o_exceptbase);
            branches.addSubclassesToBranches(r_exceptbase);

            Set<OWLAxiom> impliedwobase = r_exceptbase.getInferredSubclassOfAxioms();


            /*
            All implied axioms disregarding those implied by the imports
             */
            Reasoner r_all = new Reasoner(o_all);
            Set<OWLAxiom> before_all = r_all.getInferredSubclassOfAxioms();
            before_all.removeAll(impliedwobase);

            /*
            All asserted axioms disregarding those implied by the imports
             */
            Set<OWLAxiom> before_asserted = r_all.getAssertedSubclassOfAxioms(branches.getAllClassesInBranches());
            before_asserted.removeAll(impliedwobase);

            Set<OWLAxiom> stripped_sbcl_in_base = stripSubClassAxioms(base.getAxioms(Imports.EXCLUDED));
            stripped_sbcl_in_base.addAll(allaxiomsexceptbase);
            OWLOntology o_subclassstripped = OWLManager.createOWLOntologyManager().createOntology(stripped_sbcl_in_base,IRI.create(BASEIRI + "o_after_strip"));
            Reasoner r_scl_stripped = new Reasoner(o_subclassstripped);
            Set<OWLAxiom> after_stripping = r_scl_stripped.getInferredSubclassOfAxioms();
            after_stripping.removeAll(impliedwobase);
            p("* done.."+Timer.getSecondsElapsed("SubClassRedundancy::SubClassRedundancy()"));

            p("* Performing analysis..");
            Set<OWLAxiom> intersection = Sets.intersection(after_stripping, before_all);
            Set<OWLAxiom> intersection_asserted = Sets.intersection(after_stripping, before_asserted);

            int unionsize = Sets.union(before_all, after_stripping).size();
            int diffsize = Sets.symmetricDifference(after_stripping, before_all).size();
            int intersectionsize = intersection.size();
            int onlybefore = anotb(before_all, after_stripping).size();
            int onlyafter = anotb(after_stripping, before_all).size();
            int intersectionasserted = intersection_asserted.size();
            p("* done.."+Timer.getSecondsElapsed("SubClassRedundancy::SubClassRedundancy()"));

            p("## Bug: Axioms that were implied before removing subclasses, but are not anymore");
            p("Restricted to maximum 50 axioms");

            int i = 0;
            for (OWLAxiom ax : anotb(after_stripping, before_all)) {
                if(i>50) break;
                p("* "+render.renderForMarkdown(ax));
                i++;
            }

            i = 0;
            p("## Branches of interest: ");
            for(OWLClass c:branches.getBranchHeads()) {
                render.renderTreeForMarkdown(c,r_all.getOWLReasoner(),lines.getLines(),1);
            }
            p("## How many subclasses are recapitulated (i.e. redundant) wrt. defined classes?");
            p("Restricted to maximum 50 axioms");
            for (OWLAxiom ax : intersection_asserted) {
                if(i>50) break;
                p("* "+render.renderForMarkdown(ax));
                i++;
            }

            p("## Summary");
            p("* All implied Subclass axioms: " + before_all.size());
            p("* Implied axioms after stripping: " + after_stripping.size());
            p("* All asserted SubClassOf axioms: " + before_asserted.size());
            p("* Union: " + unionsize);
            p("* Diff: " + diffsize);
            p("* Redundant (implications): " + intersectionsize);
            p("* Redundant (assertions): " + intersectionasserted);
            p("* Only after: " + onlyafter);
            p("* Only before: " + onlybefore);

        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }
    }

    private Set<OWLAxiom> stripSubClassAxioms(Set<OWLAxiom> axioms) {
        Set<OWLAxiom> nsbcl = new HashSet<>();
        for (OWLAxiom ax : axioms) {
            if (!(ax instanceof OWLSubClassOfAxiom)) {
                nsbcl.add(ax);
            }
        }
        return nsbcl;
    }


    public void exportOutput(File out) {
        try {
            FileUtils.writeLines(out, lines.getLines());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void p(String s) {
        System.out.println(s);
        lines.addLine(s);
    }

    private Set<OWLAxiom> anotb(Set<OWLAxiom> a, Set<OWLAxiom> b) {
        Set<OWLAxiom> set = new HashSet<>();
        set.addAll(a);
        set.removeAll(b);
        return set;
    }


    private Set<OWLAxiom> subclassStripped(Set<OWLLogicalAxiom> logicalAxioms) {
        Set<OWLAxiom> nsbcl = new HashSet<>();
        for (OWLAxiom ax : logicalAxioms) {
            if (ax instanceof OWLSubClassOfAxiom) {

            } else {
                nsbcl.add(ax);
            }
        }
        return nsbcl;
    }

    public static void main(String[] args) {
        File odir = new File("/data/corpora/sbcl");
        if(odir.isDirectory()) FileUtils.deleteQuietly(odir);
        odir.mkdir();
        File outdir = new File("/ws/phenotypepatternanalysis/subclassredundancy/");
        File branches = new File("/data/dockerdata/branches2.txt");
        Set<IRI> phenotypeontologies = new HashSet<>();
        //phenotypeontologies.add(IRI.create("http://purl.obolibrary.org/obo/mp.owl"));

        phenotypeontologies.add(IRI.create("http://purl.obolibrary.org/obo/hp.owl"));
        //phenotypeontologies.add(IRI.create("http://purl.obolibrary.org/obo/fbcv/dpo.owl"));
        //phenotypeontologies.add(IRI.create("http://purl.obolibrary.org/obo/wbphenotype.owl"));
        //phenotypeontologies.add(IRI.create("http://purl.obolibrary.org/obo/zfa.owl"));
        //phenotypeontologies.add(IRI.create("http://purl.obolibrary.org/obo/xao.owl"));
        //phenotypeontologies.add(IRI.create("http://purl.obolibrary.org/obo/MFOEM.owl"));
        //phenotypeontologies.add(IRI.create("http://purl.obolibrary.org/obo/abo_in_nbo.owl"));
        //phenotypeontologies.add(IRI.create("http://purl.obolibrary.org/obo/nbo.owl"));
        //phenotypeontologies.add(IRI.create("http://purl.obolibrary.org/obo/pato.owl"));

        //phenotypeontologies.add(IRI.create("http://purl.obolibrary.org/obo/uberon.owl"));
        //phenotypeontologies.add(IRI.create(" http://purl.obolibrary.org/obo/MPATH.owl"));
        //phenotypeontologies.add(IRI.create("http://purl.obolibrary.org/obo/upheno.owl"));
        //phenotypeontologies.add(IRI.create("http://purl.obolibrary.org/obo/doid.owl"));
        //phenotypeontologies.add(IRI.create("http://purl.obolibrary.org/obo/go/extensions/go-plus.owl"));

        for(IRI iri:phenotypeontologies) {
            try {
                String filename = iri.toString().replaceAll("[^A-Za-z0-9]", "") + ".owl";
                SubClassRedundancy sbcl = new SubClassRedundancy(iri.toString(), branches);
                File out = new File(outdir,filename+"_subclassredundancy_report.md");
                sbcl.exportOutput(out);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public List<String> getReportLines() {
        return lines.getLines();
    }
}
