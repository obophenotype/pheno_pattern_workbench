package monarch.ontology.phenoworkbench.analytics.inferenceanalysis;

import monarch.ontology.phenoworkbench.util.Export;
import monarch.ontology.phenoworkbench.util.RenderManager;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.dlsyntax.renderer.DLSyntaxObjectRenderer;
import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class LabelGeneratorRunner {

    public static OWLDataFactory df = OWLManager.getOWLDataFactory();
    public static RenderManager renderManager = RenderManager.getInstance();
    public static Set<PropertyUsage> usages = new HashSet<>();


    public static void main(String[] args) throws OWLOntologyCreationException {
        args = new String[3];
        args[0] = "/Volumes/EBI/tmp/ro/ro.owl";
        args[1] = "/Volumes/EBI/tmp/ro/ro_labels.csv";
        //args[2] = "/data/rosurvey/";
        File f_o = new File(args[0]);
        File f_label = new File(args[1]);

        System.out.println("Loading " + f_o.getName());
        OWLOntology o = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(f_o);

        renderManager.addLabel(o);

        List<Map<String, String>> data_labels = new ArrayList<>();

        for (OWLEntity p : o.getSignature(Imports.INCLUDED)) {
            Map<String, String> rec = new HashMap<>();
            rec.put("filename",f_o.getAbsolutePath());
            rec.put("entity", p.getIRI().toString());
            rec.put("type",p.getEntityType().getName());
            rec.put("label", renderManager.getLabel(p));
            data_labels.add(rec);
        }

        Export.writeCSV(data_labels, f_label);

    }




}
