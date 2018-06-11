package monarch.ontology.phenoworkbench.browser.candident;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import monarch.ontology.phenoworkbench.browser.basic.BasicLayout;
import monarch.ontology.phenoworkbench.util.OntologyEntry;
import monarch.ontology.phenoworkbench.util.Timer;
import org.semanticweb.owlapi.model.parameters.Imports;
import monarch.ontology.phenoworkbench.analytics.pattern.reconciliation.CandidateIdentifierApp;

public class CandidentView extends BasicLayout {

    /**
     *
     */
    private static final long serialVersionUID = 8440240868260139938L;

    public CandidentView() {
        super("Candidate Identification");
    }

    @Override
    protected Map<String, String> getRunOptions() {
        Map<String, String> options = new HashMap<>();
        options.put("imports", "yes");
        return options;
    }

    @Override
    protected void runAnalysis(Set<OntologyEntry> selectedItems) {
        selectedItems.forEach(System.out::println);

        Imports imports = runOptionOrNull("imports").equals("yes") ? Imports.INCLUDED : Imports.EXCLUDED;

        CandidateIdentifierApp p = new CandidateIdentifierApp(selectedItems);
        p.setImports(imports);
        p.runAnalysis();
        CandidentLayoutPanel l_rec = new CandidentLayoutPanel(p);
        setResults(l_rec, true);
        Timer.printTimings();
    }


}
