package monarch.ontology.phenoworkbench.analytics.inferenceanalysis;

import monarch.ontology.phenoworkbench.util.*;
import org.apache.commons.io.FileUtils;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;

import java.io.File;
import java.io.IOException;
import java.util.*;


public class OntologyStatsRunner {

    private RenderManager render = RenderManager.getInstance();
    private OntologyDebugReport report = new OntologyDebugReport();

    private final long start = System.currentTimeMillis();
    private final Set<String> pd;
    private final Imports imports;
    private final Map<String, Set<OWLAxiom>> allAxiomsAcrossOntologies = new HashMap<>();
    private final Map<String, Set<OWLEntity>> allSignaturesAcrossOntologies = new HashMap<>();
    private final Map<String, Map<OWLEntity,Set<OWLClassExpression>>> allSignaturesAcrossOntologiesWithDefinitions = new HashMap<>();


    public OntologyStatsRunner(Set<String> pd, boolean imports) {
        this.pd = pd;
        this.imports = imports ? Imports.INCLUDED : Imports.EXCLUDED;
    }

    public static void main(String[] args) throws IOException {
        File pd = new File(args[0]);
        boolean imports = args[1].contains("i");
        File out = new File(args[2]);


        OntologyStatsRunner p = new OntologyStatsRunner(new HashSet<>(FileUtils.readLines(pd,"UTF-8")), imports);
        p.prepare();
        try {
            p.printResults(out);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void prepare() {
        for (String ourl : pd) {
            //if(ourl.endsWith("hp.owl"))
            processOntology(imports, ourl);
        }
    }

    public void printResults(File out) throws IOException {
        report.addLine("# Analysing individual ontologies for inferences");
        for (String f: allAxiomsAcrossOntologies.keySet()) {
            report.addLine("## Ontology: " + f);
            report.addEmptyLine();
            OntologyUtils.p("#### ANALYSING ONTOLOGY: "+f);

            Set<OWLEntity> sig = allSignaturesAcrossOntologies.get(f);
            Map<OWLEntity,Set<OWLClassExpression>> map = allSignaturesAcrossOntologiesWithDefinitions.get(f);

            int eq = 0;
            int def = 0;
            List<Integer> numberOfDef = new ArrayList<>();

            for(OWLEntity e:map.keySet()) {
                boolean beq = false;
                for(OWLClassExpression ce:map.get(e)) {
                    beq = isEQDefinition(ce);
                }
                if(beq) {
                    eq++;
                }
                def++;
                numberOfDef.add(map.get(e).size());
            }

            OptionalDouble mean = numberOfDef.stream().mapToInt(a->a).average();

            report.addLine("Number of classes: "+sig.size());
            report.addLine("Number of definitions per class (only entities with def): "+mean.orElse(-1));
            report.addLine("Number of classes with definitions: "+def);
            report.addLine("Number of classes with eq definitions: "+eq);
            report.addEmptyLine();
        }

        FileUtils.writeLines(new File(out,"report_inference_analysis.md"), report.getLines());
    }

    public static boolean isEQDefinition(OWLClassExpression ce) {
        for(OWLEntity entity:ce.getSignature()) {
            if(entity.getIRI().toString().equals("http://purl.obolibrary.org/obo/RO_0000052")||entity.getIRI().toString().equals("http://purl.obolibrary.org/obo/RO_0002573")) {
                return true; //http://purl.obolibrary.org/obo/RO_0002573
            }
        }
        return false;
    }

    public List<String> getReportLines() {
        return report.getLines();
    }



    private void processOntology(Imports imports, String ofile) {
        OntologyUtils.p("Preparing "+ofile);
        try {
            OWLOntology o = KB.getInstance().getOntology(ofile).get();
            render.addLabel(o);
            allAxiomsAcrossOntologies.put(ofile,o.getAxioms(imports));
            Set<OWLEntity> sigWOImp = new HashSet<>(o.getClassesInSignature(Imports.EXCLUDED));
            for(OWLOntology imp:o.getImports()) {
               sigWOImp.removeAll(imp.getClassesInSignature(Imports.INCLUDED));
            }
            allSignaturesAcrossOntologies.put(ofile,sigWOImp);
            Map<OWLEntity,Set<OWLClassExpression>> mapEQ = extractDefinedClasses(o.getAxioms(Imports.INCLUDED));
            mapEQ.keySet().retainAll(sigWOImp);
            allSignaturesAcrossOntologiesWithDefinitions.put(ofile, mapEQ);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Map<OWLEntity,Set<OWLClassExpression>> extractDefinedClasses(Set<OWLAxiom> axioms) {
        Set<OWLEquivalentClassesAxiom> allDefinitions = getDefinitionAxioms(axioms);
        OntologyUtils.p("extractDefinedClasses(). Definitons: " + allDefinitions.size());
        Map<OWLEntity,Set<OWLClassExpression>> generatedDefinitions = new HashMap<>();

        int all = allDefinitions.size();
        int i = 0;
        long timespent_all = 0;

        for (OWLEquivalentClassesAxiom ax : allDefinitions) {
            long start = System.currentTimeMillis();
            i++;
            if(i % 5000 == 0) {
                OntologyUtils.p("Processing definition "+i+"/"+all);
            }
            Set<OWLClass> classes = new HashSet<>();
            Set<OWLClassExpression> ces = new HashSet<>();
            for (OWLClassExpression ce : ax.getClassExpressionsAsList()) {
                if (ce.isClassExpressionLiteral()) {
                    classes.add(ce.asOWLClass());
                } else  {
                    ces.add(ce);
                }
            }
            for(OWLClass c: classes) {
                for(OWLClassExpression ce:ces) {
                    if(!generatedDefinitions.containsKey(c)) {
                        generatedDefinitions.put(c,new HashSet<>());
                    }
                    generatedDefinitions.get(c).add(ce);
                }
            }
            timespent_all +=(System.currentTimeMillis()-start);
        }

        OntologyUtils.p("Generated: " + generatedDefinitions.size());
        OntologyUtils.p("All: "+timespent_all/1000);
        return generatedDefinitions;
    }

    private Set<OWLEquivalentClassesAxiom> getDefinitionAxioms(Set<OWLAxiom> axioms) {
        Set<OWLEquivalentClassesAxiom> allDefinitions = new HashSet<>();
        for (OWLAxiom owlAxiom : axioms) {
            if (owlAxiom instanceof OWLEquivalentClassesAxiom) {
                allDefinitions.add((OWLEquivalentClassesAxiom) owlAxiom);
            }
        }
        return allDefinitions;
    }


    private String printTime() {
        long current = System.currentTimeMillis();
        long duration = current - start;
        return " ("+(duration/1000)+" sec)";
    }
}
