package monarch.ontology.phenoworkbench.analytics.inferenceanalysis;

import monarch.ontology.phenoworkbench.util.KB;
import monarch.ontology.phenoworkbench.util.OntologyDebugReport;
import monarch.ontology.phenoworkbench.util.OntologyUtils;
import monarch.ontology.phenoworkbench.util.RenderManager;
import org.apache.commons.io.FileUtils;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;

import java.io.File;
import java.io.IOException;
import java.util.*;


public class ImportsGraphPrint {

    private RenderManager render = new RenderManager();
    private OntologyDebugReport report = new OntologyDebugReport();

    private final long start = System.currentTimeMillis();
    private final Set<String> pd;
    private final Map<String, OWLOntology> allImportsAcrossOntologies = new HashMap<>();
    Map<String,String> import_location_to_iri = new HashMap<>();


    public ImportsGraphPrint(Set<String> pd) {
        this.pd = pd;
    }

    public static void main(String[] args) throws IOException {
        File pd = new File(args[0]);
        File out = new File(args[1]);


        ImportsGraphPrint p = new ImportsGraphPrint(new HashSet<>(FileUtils.readLines(pd,"UTF-8")));
        p.prepare();
        try {
            p.printResults(out);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void prepare() {
        for (String ourl : pd) {
            processOntology(Imports.INCLUDED, ourl);
        }
    }

    public void printResults(File out) throws IOException {
        report.addLine("# Analysing individual ontologies for inferences");
        for (String f: allImportsAcrossOntologies.keySet()) {
            report.addLine("## Ontology: " + f);
            report.addEmptyLine();
            OntologyUtils.p("#### ANALYSING ONTOLOGY: "+f);

            OWLOntology imports = allImportsAcrossOntologies.get(f);
            for(OWLImportsDeclaration dec:imports.getImportsDeclarations()) {
                printRecursive(imports.getOWLOntologyManager(),dec, 1);
            }
            report.addEmptyLine();
        }

        FileUtils.writeLines(new File(out,"report_import_analysis.md"), report.getLines());
    }

    private void printRecursive(OWLOntologyManager man, OWLImportsDeclaration dec, int level) {

        String repeated = new String(new char[level]).replace("\0", "  ");
        OWLOntology o = man.getImportedOntology(dec);
        report.addLine(repeated+" * "+iri(o)+ " ("+o.getAxioms(Imports.EXCLUDED).size()+"/"+o.getAxioms(Imports.INCLUDED).size()+" axioms): "+dec.getIRI());
        for(OWLImportsDeclaration imp:o.getImportsDeclarations()) {
            printRecursive(man,imp,level+1);
        }
    }

    private String iri(OWLOntology o) {
        return o.getOntologyID().getOntologyIRI().or(IRI.create("unknown")).toString();
    }


    private void processOntology(Imports imports, String ofile) {
        OntologyUtils.p("Preparing "+ofile);
        try {
            OWLOntology o = KB.getInstance().getOntology(ofile).get();
            render.addLabel(o);
            allImportsAcrossOntologies.put(ofile,o);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String printTime() {
        long current = System.currentTimeMillis();
        long duration = current - start;
        return " ("+(duration/1000)+" sec)";
    }

}
