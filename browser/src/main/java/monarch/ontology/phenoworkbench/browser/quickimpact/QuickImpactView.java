package monarch.ontology.phenoworkbench.browser.quickimpact;

import java.util.*;

import com.vaadin.ui.*;

import monarch.ontology.phenoworkbench.util.OntologyClass;
import monarch.ontology.phenoworkbench.analytics.pattern.generation.PatternClass;
import monarch.ontology.phenoworkbench.analytics.pattern.impact.ImpactMode;
import monarch.ontology.phenoworkbench.analytics.quickimpact.QuickImpact;
import monarch.ontology.phenoworkbench.browser.basic.BasicLayout;
import monarch.ontology.phenoworkbench.browser.basic.LayoutUtils;
import monarch.ontology.phenoworkbench.util.DefinedClass;
import monarch.ontology.phenoworkbench.browser.basic.PatternTree;
import monarch.ontology.phenoworkbench.browser.basic.PatternTreeItem;
import monarch.ontology.phenoworkbench.util.OntologyEntry;
import monarch.ontology.phenoworkbench.util.Timer;
import org.semanticweb.owlapi.model.parameters.Imports;

public class QuickImpactView extends BasicLayout {

    /**
     *
     */
    private static final long serialVersionUID = 8440240868260139938L;
    private QuickImpactModeSelect sl_quickmode = new QuickImpactModeSelect();


    public QuickImpactView() {
        super("Quick Impact");
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
    protected void runAnalysis(Set<OntologyEntry> selectedOntologies) {
        System.out.println("runAnalysis()" + Timer.getSecondsElapsed("QuickImpactView::runAnalysis()"));
        Timer.start("QuickImpactView::runAnalysis()");

        Imports imports = runOptionOrNull("imports").equals("yes") ? Imports.INCLUDED : Imports.EXCLUDED;
        String patternfileiri = runOptionOrNull("patterns");

        System.out.println("Initialising quick impact" + Timer.getSecondsElapsed("QuickImpactView::runAnalysis()"));
        ImpactMode mode = sl_quickmode.getSelectedItem();
        Timer.start("QuickImpactView::QuickImpact()");
        QuickImpact p = new QuickImpact(selectedOntologies, patternfileiri, mode);
        p.setImports(imports);
        p.runAnalysis();
        Timer.end("QuickImpactView::QuickImpact()");

        System.out.println("Initialising tree" + Timer.getSecondsElapsed("QuickImpactView::runAnalysis()"));
        Timer.start("QuickImpactView::PatternTree()");
        PatternTree tree = new PatternTree(p.getTopPatterns());
        Timer.end("QuickImpactView::PatternTree()");

        PatternInfoBox impactbox = new PatternInfoBox();
        VerticalLayout vl_infobox = LayoutUtils.hlNoMarginNoSpacingNoSize(impactbox);
        Panel info = LayoutUtils.preparePanel(vl_infobox, "DefinedClass info");

        System.out.println("Initialising grid" + Timer.getSecondsElapsed("QuickImpactView::runAnalysis()"));
        WeightedPatternGrid grid = new WeightedPatternGrid(p);

        System.out.println("Initialising remaining layout elements" + Timer.getSecondsElapsed("QuickImpactView::runAnalysis()"));
        impactbox.addItemClickListener(event -> updateInfoBox(p, impactbox, event.getItem(), grid, tree));
        tree.addItemClickListener(event -> updateInfoBox(p, impactbox, event.getItem(), grid, tree));
        grid.addItemClickListener(event -> updateInfoBox(p, impactbox, event.getItem(), grid, tree));
        Panel panel_patterns = LayoutUtils.preparePanel(grid, "Patterns");
        
        Panel panel_tree = LayoutUtils.preparePanel(tree, "Browser");
        HorizontalSplitPanel split_tree = LayoutUtils.prepareSplitPanel(info, panel_tree,400);
        VerticalLayout vl_all = new VerticalLayout();
        vl_all.setMargin(false);
        vl_all.setSpacing(true);
        vl_all.setSizeFull();
        vl_all.addComponent(split_tree);
        vl_all.addComponent(panel_patterns);
        setResults(vl_all, true);

        Timer.end("QuickImpactView::runAnalysis()");
        System.out.println("Done:" + Timer.getSecondsElapsed("QuickImpactView::runAnalysis()"));
        Timer.printTimings();
    }


    



   


    private void updateInfoBox(QuickImpact p, PatternInfoBox impactbox, Object pi, WeightedPatternGrid g, PatternTree tree) {
        if (pi instanceof PatternTreeItem) {
            OntologyClass pc = ((PatternTreeItem) pi).getPatternClass();
            impactbox.setValue(pc, p.getExplanationProvider(),p,p);
            selectPatternInWeightedGrid(g, pc);
        } else if (pi instanceof WeightedPattern) {
            DefinedClass pc = ((WeightedPattern) pi).getPattern();
            impactbox.setValue(pc, p.getExplanationProvider(),p,p);
            tree.expandSelect(pc);
        } else if (pi instanceof OntologyClass) {
        		 OntologyClass pc = (OntologyClass) pi;
             impactbox.setValue(pc, p.getExplanationProvider(),p,p);
             tree.expandSelect(pc);
             selectPatternInWeightedGrid(g, pc);
        }
        this.getUI().push();
    }

	private void selectPatternInWeightedGrid(WeightedPatternGrid g, OntologyClass pc) {
		if (pc instanceof PatternClass) {
		    g.getWeightedPattern((PatternClass) pc).ifPresent(wp -> {
		        g.select(wp);
		        g.scrollTo(g.indexOf(wp));
		    });
		}
	}


}
