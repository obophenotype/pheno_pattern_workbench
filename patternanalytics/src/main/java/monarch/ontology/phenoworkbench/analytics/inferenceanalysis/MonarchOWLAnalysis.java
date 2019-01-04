package monarch.ontology.phenoworkbench.analytics.inferenceanalysis;

import monarch.ontology.phenoworkbench.util.Export;
import monarch.ontology.phenoworkbench.util.RenderManager;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;

import java.io.File;
import java.util.*;

public class MonarchOWLAnalysis {

    public static OWLDataFactory df = OWLManager.getOWLDataFactory();
    public static RenderManager renderManager = RenderManager.getInstance();
    public static Set<PropertyUsage> usages = new HashSet<>();


    public static void main(String[] args) throws OWLOntologyCreationException {
        args = new String[2];
        args[0] = "https://raw.githubusercontent.com/monarch-initiative/monarch-ontology/master/monarch.owl";
        args[1] = "/Volumes/EBI/tmp/ro/ro_labels.csv";
        //args[2] = "/data/rosurvey/";
        IRI f_o = IRI.create(args[0]);
        OWLOntology o = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(f_o);

        OWLClass nothing = df.getOWLNothing();

        for(OWLOntology impo:o.getImportsClosure()) {
            System.out.println(impo.getOntologyID().getOntologyIRI());
            for(OWLAxiom ax:impo.getAxioms(Imports.EXCLUDED)) {
                if(ax.containsEntityInSignature(nothing)) {

                    System.out.println(renderManager.render(ax));
                    System.out.println(ax);
                }
            }
        }

    }




}
