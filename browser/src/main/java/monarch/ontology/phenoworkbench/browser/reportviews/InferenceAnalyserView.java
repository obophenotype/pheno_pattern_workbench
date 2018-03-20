package monarch.ontology.phenoworkbench.browser.reportviews;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import monarch.ontology.phenoworkbench.browser.basic.BasicLayout;
import monarch.ontology.phenoworkbench.analytics.inferenceanalysis.InferenceAnalyser;

import com.vaadin.ui.UI;

public class InferenceAnalyserView extends BasicLayout {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8440240868260139938L;

	public InferenceAnalyserView() {
		super("Ontology Inference Analysis");
	}

	@Override
	protected Map<String, String> getRunOptions() {
		Map<String, String> options = new HashMap<>();
		options.put("imports", "yes");
		return options;
	}

	@Override
	protected void runAnalysis(Set<String> selectedItems) {
		selectedItems.forEach(System.out::println);

		boolean imports = runOptionOrNull("imports").equals("yes");

		File resultsdir = deleteMakeTmpDirectory("ia_results").get();
		InferenceAnalyser p = new InferenceAnalyser(selectedItems, imports);
		p.prepare();
		try {
			p.printResults(resultsdir);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		writeMarkdownToResults(p.getReportLines(),true);
		
	}
}
