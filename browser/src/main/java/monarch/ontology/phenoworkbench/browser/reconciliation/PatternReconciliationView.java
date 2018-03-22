package monarch.ontology.phenoworkbench.browser.reconciliation;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import monarch.ontology.phenoworkbench.browser.basic.BasicLayout;
import monarch.ontology.phenoworkbench.analytics.pattern.reconciliation.PatternReconciler;

import com.vaadin.ui.UI;
import monarch.ontology.phenoworkbench.util.Files;
import monarch.ontology.phenoworkbench.util.Timer;

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
		options.put("lazyalign", "no");
		options.put("bidirectionalmapping", "yes");
		options.put("confidencethreshold", "0.9");
		options.put("mappings", "https://raw.githubusercontent.com/obophenotype/upheno/master/mappings/hp-to-mp-bestmatches.tsv");
		return options;
	}

	@Override
	protected void runAnalysis(Set<String> selectedItems) {
		selectedItems.forEach(System.out::println);

		boolean imports = runOptionOrNull("imports").equals("yes");
		boolean lazyalign = runOptionOrNull("lazyalign").equals("yes");
		boolean bidirection = runOptionOrNull("bidirectionalmapping").equals("yes");
		double confidencethreshold = Double.valueOf(runOptionOrNull("confidencethreshold"));
		String mapping = runOptionOrNull("mappings");

		File mappings = downloadFile(mapping, "txt");

		System.out.println("Prepare DefinedClass Reconciler");
		PatternReconciler p = new PatternReconciler(selectedItems, mappings, imports, lazyalign, bidirection, confidencethreshold);
		System.out.println("Layout DefinedClass Reconciler");
ReconcilerLayoutPanel l_rec = new ReconcilerLayoutPanel(p);
System.out.println("Done Layout");
        setResults(l_rec, true);
        System.out.println("Done Setting Results");
        Timer.printTimings();
	}

	
}
