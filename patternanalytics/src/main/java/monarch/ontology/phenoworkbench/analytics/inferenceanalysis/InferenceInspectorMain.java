package monarch.ontology.phenoworkbench.analytics.inferenceanalysis;

import monarch.ontology.phenoworkbench.util.RenderManager;
import monarch.ontology.phenoworkbench.util.Subsumption;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class InferenceInspectorMain {
    public static void main(String[] args) throws OWLOntologyCreationException {
        args = new String[2];
        args[0] = "/ws2/human-phenotype-ontology/src/ontology/hp-edit.owl";
        args[1] = "/ws/human-phenotype-ontology/src/ontology/hp-edit.owl";
        File f1 = new File(args[0]);
        File f2 = new File(args[1]);
        System.out.println("Loading O1");
        OWLOntology o = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(f1);
        System.out.println("Loading O2");
        OWLOntology o2 = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(f2);

        OWLReasoner r = new ElkReasonerFactory().createReasoner(o);
        OWLReasoner r2 = new ElkReasonerFactory().createReasoner(o2);



        RenderManager labels  = RenderManager.getInstance();
        labels.addLabel(o);

        Set<Subsumption> sub1 = SubsumptionUtils.getSubsumptions(r,o,true);
        Set<Subsumption> sub2 = SubsumptionUtils.getSubsumptions(r2,o2,true);
        Set<OWLClass> changedSuperClasses = new HashSet<>();

        System.out.println("Compute lost Axioms");
        Set<Subsumption> axiomslost = new HashSet<>();
        axiomslost.addAll(sub1);
        axiomslost.removeAll(sub2);
        axiomslost.forEach(a->changedSuperClasses.add(a.getSub_c()));

        System.out.println("Compute gained Axioms");
        Set<Subsumption> axiomsgained = new HashSet<>();
        axiomsgained.addAll(sub2);
        axiomsgained.removeAll(sub1);
        axiomsgained.forEach(a->changedSuperClasses.add(a.getSub_c()));

        for(OWLClass c:changedSuperClasses) {
            System.out.println("Gained:");
            axiomsgained.stream().filter(a->a.getSub_c().equals(c)).forEach(a->System.out.println(a.getLabelledName(labels)));
            System.out.println("Lost:");
            axiomslost.stream().filter(a->a.getSub_c().equals(c)).forEach(a->System.out.println(a.getLabelledName(labels)));
            System.out.println("----------------------------------");
        }

        System.out.println("Lost: "+axiomslost.size());
        System.out.println("Gained: "+axiomsgained.size());

    }
}
