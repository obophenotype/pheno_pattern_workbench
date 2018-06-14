package monarch.ontology.phenoworkbench.analytics.inferenceanalysis;

import monarch.ontology.phenoworkbench.analytics.pattern.generation.PatternGenerator;
import monarch.ontology.phenoworkbench.analytics.pattern.reconciliation.OntologyTermSet;
import monarch.ontology.phenoworkbench.util.*;
import org.apache.commons.io.FileUtils;
import org.obolibrary.oboformat.writer.OBOFormatWriter;
import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;


public class DefinitionPatternMigration  extends PhenoAnalysisRunner {

    private final Map<String,DefinitionSet> basicDefinitions = new HashMap<>();
    private final File out;

    public DefinitionPatternMigration(Set<OntologyEntry> pd, File out) {
        super(pd);
        this.out = out;
    }

    public static void main(String[] args) {
        File out = new File(args[0]);
        boolean imports = args[1].contains("i");

        ClassLoader classLoader = DefinitionPatternMigration.class.getClassLoader();
        File os = new File(classLoader.getResource("ontologies").getFile());
        File roots = new File(classLoader.getResource("phenotypeclasses").getFile());
        OntologyRegistry phenotypeontologies = new OntologyRegistry(os,roots);

        Set<OntologyEntry> entries = new HashSet<>(); //phenotypeontologies.getOntologies()
entries.add(new OntologyEntry("hp","http://purl.obolibrary.org/obo/hp.owl"));
        DefinitionPatternMigration p = new DefinitionPatternMigration(entries,out);
        p.setImports(imports ? Imports.INCLUDED : Imports.EXCLUDED);
        p.runAnalysis();
    }



    @Override
    public void runAnalysis() {
        String process = "DefinitionPatternMigration::runAnalysis()";
        try {
            log("Initialising pattern generator..", process);
            PatternGenerator patternGenerator = new PatternGenerator(getRenderManager());

            log("Create definition sets", process);
            for(OntologyEntry oid:getO().getOntologyEntries()) {
                    DefinitionSet defs = new DefinitionSet();
                    defs.setDefinitions(patternGenerator.extractDefinedClasses(getO().getAxioms(oid.getOid()),true));
                    basicDefinitions.put(oid.getOid(),defs);

            }

            log("Comparing..", process);
            for(String oid:basicDefinitions.keySet()) {
                String sub = "p";
                log(oid,sub);
                DefinitionSet basic = basicDefinitions.get(oid);
                log("Transforming..",sub);
                DefinitionSet towards = new TowardsDefinitionTransformation().get(basic);
                log("Create O..",sub);
                OWLOntology o = OWLManager.createOWLOntologyManager().createOntology(getO().getAxioms(oid));
                log("Comparing..",sub);
                OntologyCompare p = new DefinitionImpactOntologyCompare(basic,towards,new ElkReasonerFactory(),o,oid);
                log("Export..",sub);
                FileUtils.writeLines(new File(out,"axiom_diff_"+oid+".csv"), p.getCsv());
            }

            log("Done..", "DefinitionPatternMigration::runAnalysis()");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
