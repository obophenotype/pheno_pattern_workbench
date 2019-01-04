package monarch.ontology.phenoworkbench.analytics.pattern.report;

import monarch.ontology.phenoworkbench.util.*;
import monarch.ontology.phenoworkbench.util.Timer;
import org.apache.commons.io.FileUtils;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;


public class ClassLabelWriter {

    private RenderManager render = RenderManager.getInstance();

    private List<Map<String,String>> data_i = new ArrayList<>();
    private IRIManager iriManager;

    private final Set<String> uris;
    private final String id;
    private OWLOntology o = null;
    private OWLOntology o_imp = null;

    private ClassLabelWriter(Set<String> uris, String id, Map<String,String> ns2pre) {
        this.uris = uris;
        this.id = id;
        this.iriManager = new IRIManager(ns2pre);
    }

    public void runAnalysis() {
        OntologyUtils.p("Process Ontologies" + Timer.getSecondsElapsed("PatternExtractor::runAnalysis"));

        try {
            o_imp = OWLManager.createOWLOntologyManager().createOntology();
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }
OWLDataFactory df = OWLManager.getOWLDataFactory();
        for(String uri:uris) {
            try {
                o = OWLManager.createOWLOntologyManager().loadOntology(IRI.create(uri));
                render.addLabel(o);
                o_imp.getOWLOntologyManager().applyChange(new AddImport(o_imp,df.getOWLImportsDeclaration(IRI.create(uri))));
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
        try {
            o_imp.getOWLOntologyManager().saveOntology(o_imp,new FileOutputStream(new File(out,"onts_necessary.owl")));
        } catch (OWLOntologyStorageException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static class AssembleZP {
        private BranchLoader branches = null;
        private RenderManager render = RenderManager.getInstance();
        OntologyDebugReport lines = new OntologyDebugReport();

        private static String BASEIRI = "http://ebi.ac.uk/";


        public static void main(String[] args) {
            File f_zp = new File("/ws/ontologies/zp_analysis.owl");
           IRI zpowl =  IRI.create("https://raw.githubusercontent.com/obophenotype/zebrafish-phenotype-ontology-build/master/zp.owl");
           Set<IRI> imports = new HashSet<>();
           imports.add(zpowl);
           imports.add(IRI.create("http://purl.obolibrary.org/obo/upheno/imports/zfa_import.owl"));
            imports.add(IRI.create("http://purl.obolibrary.org/obo/upheno/imports/go_import.owl"));
            imports.add(IRI.create("http://purl.obolibrary.org/obo/upheno/imports/cl_import.owl"));
            imports.add(IRI.create("http://purl.obolibrary.org/obo/upheno/imports/pato_import.owl"));
            imports.add(IRI.create("http://purl.obolibrary.org/obo/upheno/imports/chebi_import.owl"));
            imports.add(IRI.create("http://purl.obolibrary.org/obo/upheno/imports/uberon_import.owl"));
            imports.add(IRI.create("http://purl.obolibrary.org/obo/uberon/bridge/uberon-bridge-to-zfa.owl"));
            imports.add(IRI.create("http://purl.obolibrary.org/obo/uberon/bridge/cl-bridge-to-zfa.owl"));
           imports.add(IRI.create("http://purl.obolibrary.org/obo/ro.owl"));
           OWLOntologyManager man = OWLManager.createOWLOntologyManager();
           OWLDataFactory df = man.getOWLDataFactory();
           OWLClass upheno = df.getOWLClass(IRI.create("http://purl.obolibrary.org/obo/UPHENO_0001002"));
           Set<OWLAxiom> axioms = new HashSet<>();
            try {
                OWLOntology zp = man.loadOntologyFromOntologyDocument(zpowl);
                for(OWLAxiom ax:zp.getAxioms()) {
                            if(ax instanceof OWLEquivalentClassesAxiom) {
                                OWLEquivalentClassesAxiom eq = (OWLEquivalentClassesAxiom)ax;
                                for(OWLClass ce:eq.getNamedClasses()) {
                                    axioms.add(df.getOWLSubClassOfAxiom(ce,upheno));
                                }
                            }
                }
                OWLOntologyManager m = OWLManager.createOWLOntologyManager();
                OWLOntology o = m.createOntology(axioms, IRI.create(BASEIRI+"zp_analysis.owl"));
                for(IRI imp:imports) {
                    OWLImportsDeclaration importDeclaration=m.getOWLDataFactory().getOWLImportsDeclaration(imp);
                    m.applyChange(new AddImport(o, importDeclaration));
                }
                m.saveOntology(o,new FileOutputStream(f_zp));
            } catch (OWLOntologyCreationException e) {
                e.printStackTrace();
            } catch (OWLOntologyStorageException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        }

        public List<String> getReportLines() {
            return lines.getLines();
        }
    }
}
