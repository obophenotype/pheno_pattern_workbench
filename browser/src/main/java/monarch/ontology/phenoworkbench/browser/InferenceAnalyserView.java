package monarch.ontology.phenoworkbench.browser;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import monarch.ontology.phenoworkbench.browser.unionanalytics.InferenceAnalyser;
import monarch.ontology.phenoworkbench.browser.util.StringUtils;

import com.vaadin.ui.UI;

public class InferenceAnalyserView extends BasicLayout {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8440240868260139938L;

	public InferenceAnalyserView(UI ui, File tmp) {
		super(ui, tmp, "Ontology Inference Analysis");
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

		File ontologiesdir = deleteMakeTmpDirectory("ia_ontologies");
		downloadOntologies(selectedItems, ontologiesdir);
		File resultsdir = deleteMakeTmpDirectory("ia_results");
		InferenceAnalyser p = new InferenceAnalyser(ontologiesdir, imports);
		getUIFixed().access(new Runnable() {

			@Override
			public void run() {
				p.prepare();
				try {
					p.printResults(resultsdir);
					writeMarkdownToResults(p.getReportLines());
		
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			
		});
	}
}
