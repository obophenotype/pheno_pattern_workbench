package monarch.ontology.phenoworkbench.browser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import monarch.ontology.phenoworkbench.browser.analytics.PatternExtractor;

import org.apache.commons.io.FileUtils;
import org.vaadin.viritin.label.RichText;


import monarch.ontology.phenoworkbench.browser.util.StringUtils;

import com.vaadin.ui.UI;

public class PatternAnalyserView extends BasicLayout{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8440240868260139938L;

	public PatternAnalyserView(UI ui, File tmp) {
		super(ui, tmp, "Ontology Pattern Analysis");
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
		downloadOntologies(selectedItems, ontologiesdir);
		File resultsdir = deleteMakeTmpDirectory("pa_results");
		
		File branches = prepareBranchesFile();
        PatternExtractor p = new PatternExtractor(ontologiesdir, branches, imports, addsubclasses, samplesize);

        getUIFixed().access(new Runnable() {

			@Override
			public void run() {
				runDebugger(p);
			}

			private void runDebugger(PatternExtractor p) {
				p.run();
				p.printResults(resultsdir);
				writeMarkdownToResults(p.getReportLines());
			}
		});
	}

	private File prepareBranchesFile() {
		File branchfile = new File(getTmpdir(),"branches.txt");
		List<String> branches = new ArrayList<>();
		branches.add("http://purl.obolibrary.org/obo/MP_0005386");
		branches.add("http://purl.obolibrary.org/obo/NBO_0000243");
		branches.add("http://purl.obolibrary.org/obo/FBcv_0000387");
		branches.add("http://purl.obolibrary.org/obo/WBPhenotype_0000517");
		branches.add("http://purl.obolibrary.org/obo/NBO_0000243");
		branches.add("http://purl.obolibrary.org/obo/NBO_0020110");
		branches.add("http://purl.obolibrary.org/obo/NBO_0000313");
		branches.add("http://purl.obolibrary.org/obo/PATO_0002265");
		
		try {
			if(branchfile.exists()) FileUtils.deleteQuietly(branchfile);
			FileUtils.writeLines(branchfile, branches);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return branchfile;
	}
}
