package monarch.ontology.phenoworkbench.browser.views;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import monarch.ontology.phenoworkbench.browser.BasicLayout;
import monarch.ontology.phenoworkbench.browser.BranchGrid;
import monarch.ontology.phenoworkbench.browser.Branches;
import monarch.ontology.phenoworkbench.browser.analytics.SubClassRedundancy;

import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

public class AxiomRedundancyAnalyserView extends BasicLayout {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8440654086826039938L;
	private BranchGrid branchGrid = new BranchGrid();

	public AxiomRedundancyAnalyserView(UI ui, File tmp) {
		super(ui, tmp, "Ontology Inference Analysis");
		setAdditionalSettingsComponent(branchGrid,true);
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
		downloadFiles(selectedItems, ontologiesdir);
		File branches = Branches.prepareBranchesFile(getTmpdir(),branchGrid.getBranches());
		
		VerticalLayout vl_sbcl_redundancy = new VerticalLayout();
		vl_sbcl_redundancy.setMargin(false);
		vl_sbcl_redundancy.setSpacing(true);
		
		
		for(File ofile:ontologiesdir.listFiles((f)->f.getName().endsWith(".owl"))) {
			SubClassRedundancy p = new SubClassRedundancy(ofile, branches);
			Label l = getHTMLFromMarkdown(p.getReportLines());
			vl_sbcl_redundancy.addComponent(l);
		}
		
		setResults(vl_sbcl_redundancy, true);
		
	}
}
