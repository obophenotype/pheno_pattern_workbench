package monarch.ontology.phenoworkbench.analytics.schemausage;

import monarch.ontology.phenoworkbench.util.OntologyDebugReport;
import monarch.ontology.phenoworkbench.util.OntologyUtils;
import org.apache.commons.io.FileUtils;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class SchemaUsageRunner {
    static OntologyDebugReport report = new OntologyDebugReport();

    public static void main(String[] args) {
        File f = new File("/data/flybaseanalysis/data/diffflybase.owl");
        File outdir = new File("/ws/ontologycompare/out/");
        IRI schema = IRI.create("http://purl.obolibrary.org/obo/so.owl");
        int THRESHOLD = 500;

        try {
            OWLOntology o = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(f);
            OWLOntology oschema = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(schema);
            Map<String,Integer> axiomtypes = new HashMap<>();
            HashMap<OWLEntity,Integer> entitycounts = new HashMap<>();

            for(OWLAxiom ax:o.getAxioms(Imports.INCLUDED)) {
                String axt = ax.getAxiomType().getName();
                if(axt.equals("Declaration")) {
                    continue;
                }
                if(!axiomtypes.containsKey(axt)) {
                    axiomtypes.put(axt,0);
                }
                axiomtypes.put(axt,axiomtypes.get(axt)+1);
                for(OWLEntity e:ax.getSignature()) {
                    if(!entitycounts.containsKey(e)) {
                        entitycounts.put(e,0);
                    }
                    entitycounts.put(e,entitycounts.get(e)+1);
                }
            }

            TreeMap<Object, Integer> sortedMap = OntologyUtils.sortMapByValue(entitycounts);


            p("# Entities references more than "+THRESHOLD+" times:");
            TreeMap<Object, Integer> sortedSchemaMap = new TreeMap<>();
            for (Map.Entry<Object, Integer> entry : sortedMap.entrySet()) {
                OWLEntity e = (OWLEntity) entry.getKey();
                Integer v = entry.getValue();
                if(v>THRESHOLD) {
                    p("* "+OntologyUtils.getRandomLabelIfAny(e,o)+":"+v);
                }
                if(oschema.containsEntityInSignature(e)) {
                    sortedSchemaMap.put(e,v);
                }
            }

           p("# Referenced entities in SO:");

            for(Object e:sortedSchemaMap.keySet()) {
                p("* "+OntologyUtils.getRandomLabelIfAny((OWLEntity)e,oschema)+": "+sortedSchemaMap.get(e));
            }

           p("# Axiomtypes");
            for(String s:axiomtypes.keySet()) {

                p("* "+s+":"+axiomtypes.get(s));

            }

            try {
                FileUtils.writeLines(new File(outdir,"flybasediffreport.md"),report.getLines());
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }
    }

    private static void p(Object e) {
        System.out.println(e);
        report.addLine(e);
    }
}
