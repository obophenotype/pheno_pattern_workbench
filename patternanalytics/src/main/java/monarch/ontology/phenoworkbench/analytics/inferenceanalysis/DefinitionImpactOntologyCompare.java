package monarch.ontology.phenoworkbench.analytics.inferenceanalysis;

import monarch.ontology.phenoworkbench.analytics.pattern.generation.DefinedClass;
import monarch.ontology.phenoworkbench.util.Subsumption;
import monarch.ontology.phenoworkbench.util.Timer;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import java.util.*;

public class DefinitionImpactOntologyCompare extends OntologyCompare {

    private final DefinitionSet base;
    private final OWLDataFactory df = OWLManager.getOWLDataFactory();
    private final List<String> csv = new ArrayList<>();

    public DefinitionImpactOntologyCompare(DefinitionSet base, DefinitionSet compare, OWLReasonerFactory rf, OWLOntology o, String oid) {
        super();
        this.base = base;
        String process = "impactcompare";
        log("String defs",process);
        Optional<OWLOntology> bare_opt = stripBaseDefinitions(o);
        if(bare_opt.isPresent()) {
            OWLOntology o_bare = bare_opt.get();
            try {
                log("Create ontologies",process);

                OWLOntology o_base = copy(o_bare);
                OWLOntology o_comp = copy(o_bare);
                addDefinitions(o_base,base);
                addDefinitions(o_comp,compare);
                log("Creating reasoners..",process);

                OWLReasoner r_bare = rf.createReasoner(o_bare);
                OWLReasoner r_base = rf.createReasoner(o_base);
                OWLReasoner r_comp = rf.createReasoner(o_comp);

                log("Obtaining subsumptions",process);

                Set<Subsumption> sub_bare = SubsumptionUtils.getSubsumptions(r_bare,o_bare);
                Set<Subsumption> sub_base = SubsumptionUtils.getSubsumptions(r_base,o_base);
                Set<Subsumption> sub_comp = SubsumptionUtils.getSubsumptions(r_comp,o_comp);

                log("Computing diffs, sub_bare: "+sub_bare.size(),process);
                log("Computing diffs, base: "+sub_base.size(),process);
                log("Computing diffs, sub_comp: "+sub_comp.size(),process);

                Set<Subsumption> sub_bare_not_base = diff(sub_bare,sub_base);
                Set<Subsumption> sub_base_not_bare = diff(sub_base,sub_base);
                Set<Subsumption> sub_bare_not_comp = diff(sub_bare,sub_comp);
                Set<Subsumption> sub_comp_not_bare = diff(sub_comp,sub_base);
                Set<Subsumption> sub_base_not_comp = diff(sub_base,sub_comp);
                Set<Subsumption> sub_comp_not_base = diff(sub_comp,sub_base);

                log("Computing diffs, sub_base_not_bare: "+sub_base_not_bare.size(),process);
                log("Extracting csv",process);

                getCsv().add("sub,super,comp,o");
                extractCSV(oid, sub_bare_not_base, "sub_bare_not_base");
                extractCSV(oid, sub_base_not_bare, "sub_base_not_bare");
                extractCSV(oid, sub_bare_not_comp, "sub_bare_not_comp");
                extractCSV(oid, sub_comp_not_bare, "sub_comp_not_bare");
                extractCSV(oid, sub_base_not_comp, "sub_base_not_comp");
                extractCSV(oid, sub_comp_not_base, "sub_comp_not_base");

            } catch (OWLOntologyCreationException e) {
                e.printStackTrace();
            }

        }

    }

    private void log(String s, String process) {
        System.out.println(s+" "+Timer.getSecondsElapsed(process));
    }

    private void extractCSV(String oid, Set<Subsumption> sub_bare_not_base, String id) {
        for(Subsumption s:sub_bare_not_base) {
            getCsv().add(s.getSub_c()+","+s.getSuper_c()+","+id+","+oid);
        }
    }

    private Set<Subsumption> diff(Set<Subsumption> s1, Set<Subsumption> s2) {
        Set<Subsumption> diff = new HashSet<>(s1);
        diff.removeAll(s2);
        return diff;
    }

    private void addDefinitions(OWLOntology o, DefinitionSet base) {
        Set<OWLAxiom> axioms = new HashSet<>();
        base.getDefinitions().forEach(d->axioms.add(df.getOWLEquivalentClassesAxiom(d.getDefiniton(),d.getOWLClass())));
        o.getOWLOntologyManager().addAxioms(o,axioms);
    }

    private OWLOntology copy(OWLOntology o_bare) throws OWLOntologyCreationException {
        return OWLManager.createOWLOntologyManager().createOntology(o_bare.getAxioms(Imports.INCLUDED));
    }

    private Optional<OWLOntology> stripBaseDefinitions(OWLOntology o) {
        Set<OWLAxiom> axioms = new HashSet<>();
        for(OWLAxiom ax:o.getAxioms()) {
            if(ax instanceof OWLEquivalentClassesAxiom) {
                for(DefinedClass definedClass:base.getDefinitions()) {
                    if (!ax.getNestedClassExpressions().contains(definedClass.getDefiniton())) {
                        axioms.add(ax);
                        break;
                    }
                }
            } else {
                axioms.add(ax);
            }
        }
        try {
            OWLOntology out = OWLManager.createOWLOntologyManager().createOntology(axioms);
            return Optional.of(out);
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public List<String> getCsv() {
        return csv;
    }
}
