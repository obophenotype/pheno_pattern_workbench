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

public class OntologyMetrics {

    public static OWLDataFactory df = OWLManager.getOWLDataFactory();
    public static RenderManager renderManager = RenderManager.getInstance();
    public static Set<PropertyUsage> usages = new HashSet<>();


    public static void main(String[] args) throws OWLOntologyCreationException {
        //args = new String[3];
        //args[0] = "/Volumes/EBI/tmp/ro/ro.owl";
        //args[1] = "/Volumes/EBI/tmp/ro/ontologies/agroportal_merged_cl.owl";
        //args[2] = "/data/rosurvey/";
        File f_o = new File(args[0]);
        File out = new File(args[1]);
        System.out.println("Loading O: "+f_o.getName());
        OWLOntology o = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(f_o);
        System.out.println("Gathering data: "+f_o.getName());
        renderManager.addLabel(o);
        List<Map<String, String>> data = new ArrayList<>();
        OWLObjectRenderer ren = new DLSyntaxObjectRenderer();
        for (OWLAxiom ax : o.getAxioms(Imports.EXCLUDED)) {
            String axiomcode = ax.hashCode() + "";
            String axiomtype = ax.getAxiomType().getName();
            Map<String, String> rec = new HashMap<>();
            rec.put("o", f_o.getAbsolutePath());
            rec.put("axiomid", axiomcode);
            rec.put("axiomtype", axiomtype);
            data.add(rec);
        }
        Export.writeCSV(data, new File(out, "rosurvey_allaxiomdata_" + f_o.getName() + ".csv"));
        System.out.println("Axiomdata gathering finished for "+f_o.getName());
    }

}
