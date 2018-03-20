package monarch.ontology.phenoworkbench.browser.reportviews;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import monarch.ontology.phenoworkbench.browser.basic.BasicLayout;
import monarch.ontology.phenoworkbench.browser.basic.BranchGrid;
import monarch.ontology.phenoworkbench.browser.basic.Branches;
import monarch.ontology.phenoworkbench.analytics.subclassredundancy.SubClassRedundancy;

import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import monarch.ontology.phenoworkbench.browser.basic.LabelManager;
import monarch.ontology.phenoworkbench.util.Files;

public class AxiomRedundancyAnalyserView extends BasicLayout {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8440654086826039938L;
	private BranchGrid branchGrid = new BranchGrid();

	public AxiomRedundancyAnalyserView() {
		super("Ontology Inference Analysis");
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

		File branches = Branches.prepareBranchesFile(Files.getInstance().getTmpdir(),branchGrid.getBranches());
		
		VerticalLayout vl_sbcl_redundancy = new VerticalLayout();
		vl_sbcl_redundancy.setMargin(false);
		vl_sbcl_redundancy.setSpacing(true);
		
		
		for(String ofile:selectedItems) {
			SubClassRedundancy p = new SubClassRedundancy(ofile, branches);
			Label l = LabelManager.htmlLabelFromMarkdown(p.getReportLines());
			vl_sbcl_redundancy.addComponent(l);
		}
		
		setResults(vl_sbcl_redundancy, true);
		
	}
}
