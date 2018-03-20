package monarch.ontology.phenoworkbench.browser.reportviews;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import monarch.ontology.phenoworkbench.browser.basic.BasicLayout;
import monarch.ontology.phenoworkbench.browser.basic.BranchGrid;
import monarch.ontology.phenoworkbench.browser.basic.Branches;
import monarch.ontology.phenoworkbench.analytics.pattern.report.PatternExtractor;

import monarch.ontology.phenoworkbench.util.Files;

public class PatternAnalyserView extends BasicLayout{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8440240868260139938L;
	private BranchGrid branchGrid = new BranchGrid();

	public PatternAnalyserView() {
		super("Ontology DefinedClass Analysis");
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

		File resultsdir = deleteMakeTmpDirectory("pa_results").get();
		File branches = Branches.prepareBranchesFile(Files.getInstance().getTmpdir(),branchGrid.getBranches());
        PatternExtractor p = new PatternExtractor(selectedItems, branches, imports, addsubclasses, samplesize);
        p.run();
		p.printResults(resultsdir);
		writeMarkdownToResults(p.getReportLines(),true);
	}

	
}
