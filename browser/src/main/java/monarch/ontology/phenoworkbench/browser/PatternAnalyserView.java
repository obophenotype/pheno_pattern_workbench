package monarch.ontology.phenoworkbench.browser;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.vaadin.viritin.label.RichText;


import monarch.ontology.phenoworkbench.browser.unionanalytics.CorpusDebugger;
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
		options.put("maxunsat", "10");
		options.put("maxexplanation", "10");
		options.put("reasoner", "elk");
		return options;
	}

	@Override
	protected void runAnalysis(Set<String> selectedItems) {
		selectedItems.forEach(System.out::println);

		boolean imports = runOptionOrNull("imports").equals("yes");
		int maxunsat = Integer.valueOf(runOptionOrNull("maxunsat"));
		int maxexplunsat = Integer.valueOf(runOptionOrNull("maxexplanation"));
		String reasoner = runOptionOrNull("reasoner");

		File ontologiesdir = deleteMakeTmpDirectory("us_ontologies");
		downloadOntologies(selectedItems, ontologiesdir);
		CorpusDebugger p = new CorpusDebugger(ontologiesdir, reasoner, imports, maxunsat, maxexplunsat);

		getUI().access(new Runnable() {

			@Override
			public void run() {
				runDebugger(p);
			}

			private void runDebugger(CorpusDebugger p) {
				p.run();
				List<String> report = p.getReportLines();
				StringBuilder sb = StringUtils.linesToStringBuilder(report);
				getRunAnalysisPanel().addResult(new RichText().withMarkDown(sb.toString()));
			}
		});
	}
}
