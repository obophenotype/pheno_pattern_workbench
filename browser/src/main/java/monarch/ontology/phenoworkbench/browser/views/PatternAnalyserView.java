package monarch.ontology.phenoworkbench.browser.views;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import monarch.ontology.phenoworkbench.browser.BasicLayout;
import monarch.ontology.phenoworkbench.browser.BranchGrid;
import monarch.ontology.phenoworkbench.browser.Branches;
import monarch.ontology.phenoworkbench.browser.analytics.PatternExtractor;

import com.vaadin.ui.UI;

public class PatternAnalyserView extends BasicLayout{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8440240868260139938L;
	private BranchGrid branchGrid = new BranchGrid();

	public PatternAnalyserView(UI ui, File tmp) {
		super(ui, tmp, "Ontology Pattern Analysis");
		setAdditionalSettingsComponent(branchGrid,true);
	}

	@Override
	protected Map<String, String> getRunOptions() {
		Map<String, String> options = new HashMap<>();
		options.put("imports", "yes");
		options.put("addsubclasses", "yes");
		options.put("samplesize", "10");
		return options;
	}

	@Override
	protected void runAnalysis(Set<String> selectedItems) {
		selectedItems.forEach(System.out::println);

		boolean imports = runOptionOrNull("imports").equals("yes");
		boolean addsubclasses = runOptionOrNull("addsubclasses").equals("yes");
		int samplesize = Integer.valueOf(runOptionOrNull("samplesize"));

		File ontologiesdir = deleteMakeTmpDirectory("pa_ontologies");
		downloadFiles(selectedItems, ontologiesdir);
		File resultsdir = deleteMakeTmpDirectory("pa_results");
		File branches = Branches.prepareBranchesFile(getTmpdir(),branchGrid.getBranches());
        PatternExtractor p = new PatternExtractor(ontologiesdir, branches, imports, addsubclasses, samplesize);
        p.run();
		p.printResults(resultsdir);
		writeMarkdownToResults(p.getReportLines(),true);
	}

	
}
