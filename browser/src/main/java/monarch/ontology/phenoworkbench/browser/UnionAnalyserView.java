package monarch.ontology.phenoworkbench.browser;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.vaadin.viritin.label.RichText;

import com.vaadin.ui.UI;

import monarch.ontology.phenoworkbench.browser.unionanalytics.CorpusDebugger;
import monarch.ontology.phenoworkbench.browser.util.StringUtils;

public class UnionAnalyserView extends BasicLayout {

	/**
	 * 
	 */
	private static final long serialVersionUID = 255453218992079876L;

	public UnionAnalyserView(UI ui, File tmp) {
		super(ui, tmp, "Ontology Union Analysis");
	}

	@Override
	protected Map<String, String> getRunOptions() {
		Map<String, String> options = new HashMap<>();
		options.put("imports", "yes");
		options.put("maxunsat", "1");
		options.put("maxexplanation", "1");
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

		getUIFixed().access(new Runnable() {

			@Override
			public void run() {
				p.run();
				writeMarkdownToResults(p.getReportLines());
			}
		});
	}

}
