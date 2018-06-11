package monarch.ontology.phenoworkbench.browser.reconciliation;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import monarch.ontology.phenoworkbench.browser.basic.BasicLayout;
import monarch.ontology.phenoworkbench.analytics.pattern.reconciliation.PatternReconciler;

import monarch.ontology.phenoworkbench.util.IRIMapping;
import monarch.ontology.phenoworkbench.util.OBOMappingFileParser;
import monarch.ontology.phenoworkbench.util.OntologyEntry;
import monarch.ontology.phenoworkbench.util.Timer;
import org.semanticweb.owlapi.model.parameters.Imports;

public class PatternReconciliationView extends BasicLayout {

    /**
     *
     */
    private static final long serialVersionUID = 8440240868260139938L;

    public PatternReconciliationView() {
        super("DefinedClass Reconciliation");
    }

    @Override
    protected Map<String, String> getRunOptions() {
        Map<String, String> options = new HashMap<>();
        options.put("imports", "yes");
        options.put("bidirectionalmapping", "no");
        options.put("mappings", "https://raw.githubusercontent.com/obophenotype/upheno/master/mappings/hp-to-mp-bestmatches.tsv");
        return options;
    }

    @Override
    protected void runAnalysis(Set<OntologyEntry> selectedItems) {
        selectedItems.forEach(System.out::println);

        Imports imports = runOptionOrNull("imports").equals("yes") ? Imports.INCLUDED : Imports.EXCLUDED;
        boolean bidirection = runOptionOrNull("bidirectionalmapping").equals("yes");
        String mapping = runOptionOrNull("mappings");

        File mappingf = downloadFile(mapping, "txt");
        List<IRIMapping> mappings = OBOMappingFileParser.parseMappings(mappingf);
        System.out.println("Number of mappings: "+mappings.size());
        System.out.println("Prepare DefinedClass Reconciler");
        PatternReconciler p = new PatternReconciler(selectedItems, mappings);
        p.setBidirectionmapping(bidirection);
        p.setImports(imports);
        p.runAnalysis();
        System.out.println("Layout DefinedClass Reconciler");
        ReconcilerLayoutPanel l_rec = new ReconcilerLayoutPanel(p);
        System.out.println("Done Layout");
        setResults(l_rec, true);
        System.out.println("Done Setting Results");
        Timer.printTimings();
    }


}
