package monarch.ontology.phenoworkbench.browser.views;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import monarch.ontology.phenoworkbench.browser.BasicLayout;
import monarch.ontology.phenoworkbench.browser.BranchGrid;
import monarch.ontology.phenoworkbench.browser.Branches;
import monarch.ontology.phenoworkbench.browser.ReconcilerGrid;
import monarch.ontology.phenoworkbench.browser.ReconcilerLayoutPanel;
import monarch.ontology.phenoworkbench.browser.analytics.PatternExtractor;
import monarch.ontology.phenoworkbench.browser.analytics.PatternReconciler;

import com.vaadin.ui.UI;

public class PatternReconciliationView extends BasicLayout{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8440240868260139938L;

	public PatternReconciliationView(UI ui, File tmp) {
		super(ui, tmp, "Pattern Reconciliation");
	}

	@Override
	protected Map<String, String> getRunOptions() {
		Map<String, String> options = new HashMap<>();
		options.put("imports", "yes");
		options.put("lazyalign", "no");
		options.put("bidirectionalmapping", "yes");
		options.put("mappings", "https://raw.githubusercontent.com/matentzn/ontologies/master/testmapping.txt");
		return options;
	}

	@Override
	protected void runAnalysis(Set<String> selectedItems) {
		selectedItems.forEach(System.out::println);

		boolean imports = runOptionOrNull("imports").equals("yes");
		boolean lazyalign = runOptionOrNull("lazyalign").equals("yes");
		boolean bidirection = runOptionOrNull("bidirectionalmapping").equals("yes");
		String mapping = runOptionOrNull("mappings");

		File ontologiesdir = deleteMakeTmpDirectory("pa_ontologies");
		downloadFiles(selectedItems, ontologiesdir);
		File mappings = downloadFile(getTmpdir(), mapping, "txt");

		PatternReconciler p = new PatternReconciler(ontologiesdir, mappings, imports, lazyalign, bidirection);
        ReconcilerLayoutPanel l_rec = new ReconcilerLayoutPanel(p);
        setResults(l_rec, true);
	}

	
}
