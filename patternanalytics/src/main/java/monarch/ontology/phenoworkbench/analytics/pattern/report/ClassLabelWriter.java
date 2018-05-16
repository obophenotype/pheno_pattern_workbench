package monarch.ontology.phenoworkbench.analytics.pattern.report;

import monarch.ontology.phenoworkbench.util.*;
import monarch.ontology.phenoworkbench.util.Timer;
import org.apache.commons.io.FileUtils;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;

import java.io.File;
import java.io.IOException;
import java.util.*;


public class ClassLabelWriter {

    private RenderManager render = new RenderManager();

    private List<Map<String,String>> data_i = new ArrayList<>();
    private IRIManager iriManager;

    private final Set<String> uris;
    private final String id;
    private OWLOntology o = null;

    private ClassLabelWriter(Set<String> uris, String id, Map<String,String> ns2pre) {
        this.uris = uris;
        this.id = id;
        this.iriManager = new IRIManager(ns2pre);
    }

    public void runAnalysis() {
        OntologyUtils.p("Process Ontologies" + Timer.getSecondsElapsed("PatternExtractor::runAnalysis"));

        for(String uri:uris) {
            try {
                o = OWLManager.createOWLOntologyManager().loadOntology(IRI.create(uri));
                render.addLabel(o);
            } catch (OWLOntologyCreationException e) {
                e.printStackTrace();
            }


            for (OWLEntity c : o.getSignature(Imports.INCLUDED)) {
                if(c instanceof OWLClass || c instanceof OWLNamedIndividual) {
                    continue;
                }
                Map<String, String> rec = new HashMap<>();
                rec.put("o", uri);
                rec.put("entity", c.getIRI().toString());
                rec.put("etype", c.getEntityType().getName());
                rec.put("jtype", c.getClass().getSimpleName());
                rec.put("sl", iriManager.getSafeLabel(c, o));
                rec.put("label", iriManager.getLabel(c, o));
                rec.put("ns", iriManager.getNamespace(c.getIRI()));
                rec.put("qsl", iriManager.getQualifiedSafeLabel(c, o));
                rec.put("shortform", iriManager.getShortForm(c.getIRI()));
                rec.put("curie", iriManager.getCurie(c));
                data_i.add(rec);
            }
        }

    }

    public static void main(String[] args) throws IOException {

        String uristring = args[0];
        String id = args[1];
        File ns2prefile = new File(args[2]);
        File out = new File(args[3]);

        Set<String> uris = new HashSet<>();
        uris.addAll(Arrays.asList(uristring.split("[|][|]")));

        System.out.print(uris);

        Map<String,String> ns2pre = new HashMap<>();
        for(String line: FileUtils.readLines(ns2prefile,"utf-8")) {
            String[] row= line.split(",");
            ns2pre.put(row[0].trim(),row[1].trim());
        }

        System.out.println(ns2pre);

        ClassLabelWriter p = new ClassLabelWriter(uris,id,ns2pre);
        p.runAnalysis();
        p.exportAll(out);
    }

    private void exportAll(File out) {
        String id = this.id.replaceAll("[^a-zA-z0-9_]","");
        Export.writeCSV(data_i, new File(out,"data_sig_"+id+".csv"));
    }

}
