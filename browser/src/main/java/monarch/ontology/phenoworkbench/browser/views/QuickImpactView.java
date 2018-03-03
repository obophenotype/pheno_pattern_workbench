package monarch.ontology.phenoworkbench.browser.views;

import java.io.File;
import java.util.*;
import com.vaadin.ui.*;

import monarch.ontology.phenoworkbench.browser.BasicLayout;
import monarch.ontology.phenoworkbench.browser.PatternInfoBox;
import monarch.ontology.phenoworkbench.browser.PatternTree;
import monarch.ontology.phenoworkbench.browser.PatternTreeItem;
import monarch.ontology.phenoworkbench.browser.QuickImpactModeSelect;
import monarch.ontology.phenoworkbench.browser.WeightedPattern;
import monarch.ontology.phenoworkbench.browser.WeightedPatternGrid;
import monarch.ontology.phenoworkbench.browser.analytics.ImpactMode;
import monarch.ontology.phenoworkbench.browser.analytics.Pattern;
import monarch.ontology.phenoworkbench.browser.analytics.PatternClass;
import monarch.ontology.phenoworkbench.browser.analytics.QuickImpact;
import monarch.ontology.phenoworkbench.browser.util.Timer;

public class QuickImpactView extends BasicLayout {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8440240868260139938L;
	private Timer timer = new Timer();
	QuickImpactModeSelect sl_quickmode = new QuickImpactModeSelect();
	
	
	

	public QuickImpactView(UI ui, File tmp) {
		super(ui, tmp, "Quick Impact");
		setAdditionalSettingsComponent(sl_quickmode, true);
	}

	@Override
	protected Map<String, String> getRunOptions() {
		Map<String, String> options = new HashMap<>();
		options.put("imports", "yes");
		options.put("patterns",
				"https://raw.githubusercontent.com/obophenotype/upheno/master/src/patterns_out/patterns_merged.owl");
		return options;
	}

	@Override
	protected void runAnalysis(Set<String> selectedItems) {
		
		System.out.println("runAnalysis()" + timer.getTimeElapsed());
		boolean imports = runOptionOrNull("imports").equals("yes");
		String patternFile = runOptionOrNull("patterns");

		System.out.println("Preparing files" + timer.getTimeElapsed());
		File ontologiesdir = deleteMakeTmpDirectory("qi_ontologies");
		File patterndir = deleteMakeTmpDirectory("qi_pattern");
		downloadFiles(selectedItems, ontologiesdir);
		File patternontology = downloadFile(patterndir, patternFile);

		System.out.println("Initialising quick impact" + timer.getTimeElapsed());
		ImpactMode mode = sl_quickmode.getSelectedItem();
		QuickImpact p = new QuickImpact(ontologiesdir, patternontology, imports,mode,10);
		PatternTree tree = new PatternTree(p);

		PatternInfoBox impactbox = new PatternInfoBox();
		VerticalLayout vl_infobox = prepareInfoBoxLayout(impactbox);

		WeightedPatternGrid grid = new WeightedPatternGrid(p);

		tree.addItemClickListener(event -> updateInfoBox(p, impactbox, event.getItem(), grid,tree));
		grid.addItemClickListener(event -> updateInfoBox(p, impactbox, event.getItem(), grid,tree));
		Panel panel_patterns = preparePanel(grid, "Patterns");

		Panel info = preparePanel(vl_infobox, "Pattern info");
		Panel panel_tree = preparePanel(tree, "Browser");
		HorizontalSplitPanel split_tree = prepareSplitPanel(info, panel_tree);

		VerticalLayout vl_all = new VerticalLayout();
		vl_all.setMargin(false);
		vl_all.setSpacing(true);
		vl_all.setSizeFull();
		vl_all.addComponent(split_tree);
		vl_all.addComponent(panel_patterns);

		setResults(vl_all, true);
		System.out.println("Done:" + timer.getTimeElapsed());
	}


	private VerticalLayout prepareInfoBoxLayout(PatternInfoBox impactbox) {
		VerticalLayout vl_infobox = new VerticalLayout();
		vl_infobox.addComponent(impactbox);
		vl_infobox.setMargin(false);
		vl_infobox.setSpacing(false);
		vl_infobox.setSizeUndefined();
		return vl_infobox;
	}

	private Panel preparePanel(Component c, String label) {
		Panel panel = new Panel(label);
		panel.setWidth("100%");
		panel.setHeight("100%");
		//c.setSizeUndefined();
		c.setWidth("100%");
		panel.setContent(c);
		return panel;
	}

	private HorizontalSplitPanel prepareSplitPanel(Component right, Component left) {
		HorizontalSplitPanel split_tree = new HorizontalSplitPanel();
		split_tree.setSplitPosition(60, Unit.PERCENTAGE);
		split_tree.setWidth("100%");
		split_tree.setHeight("400px");
		split_tree.setFirstComponent(left);
		split_tree.setSecondComponent(right);
		return split_tree;
	}

	

	private void updateInfoBox(QuickImpact p, PatternInfoBox impactbox, Object pi, WeightedPatternGrid g, PatternTree tree) {
		if (pi instanceof PatternTreeItem) {
			PatternClass pc = ((PatternTreeItem) pi).getPatternClass();
			impactbox.setValue(pc, p);
			if (g.containsPattern(pc)) {
				WeightedPattern wp = g.getWeightedPattern(pc);
				g.select(wp);
				g.scrollTo(g.indexOf(wp));
			}
		} else if (pi instanceof WeightedPattern) {
			Pattern pc = ((WeightedPattern) pi).getPattern();
			impactbox.setValue(pc, p);

			
			for (PatternTreeItem pp : tree.getMapPatternTree().get(pc)) {
				tree.expand(pp, p);
				tree.select(pp);
				g.scrollTo(tree.getTreeItemPatterns().indexOf(pp));
			}
			this.getUI().push();
		}
	}

	

}
