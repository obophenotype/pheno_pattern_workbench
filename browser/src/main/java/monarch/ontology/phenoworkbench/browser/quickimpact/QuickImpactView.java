package monarch.ontology.phenoworkbench.browser.quickimpact;

import java.util.*;

import com.vaadin.ui.*;

import monarch.ontology.phenoworkbench.analytics.pattern.generation.OntologyClass;
import monarch.ontology.phenoworkbench.analytics.pattern.generation.PatternClass;
import monarch.ontology.phenoworkbench.analytics.pattern.impact.ImpactMode;
import monarch.ontology.phenoworkbench.analytics.quickimpact.QuickImpact;
import monarch.ontology.phenoworkbench.browser.basic.BasicLayout;
import monarch.ontology.phenoworkbench.analytics.pattern.generation.DefinedClass;
import monarch.ontology.phenoworkbench.util.Timer;

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
    protected void runAnalysis(Set<String> selectedOntologies) {
        System.out.println("runAnalysis()" + Timer.getSecondsElapsed("QuickImpactView::runAnalysis()"));
        Timer.start("QuickImpactView::runAnalysis()");

        boolean imports = runOptionOrNull("imports").equals("yes");
        String patternfileiri = runOptionOrNull("patterns");

        System.out.println("Initialising quick impact" + Timer.getSecondsElapsed("QuickImpactView::runAnalysis()"));
        ImpactMode mode = sl_quickmode.getSelectedItem();
        Timer.start("QuickImpactView::QuickImpact()");
        QuickImpact p = new QuickImpact(selectedOntologies, patternfileiri, imports, mode, 10);
        Timer.end("QuickImpactView::QuickImpact()");

        System.out.println("Initialising tree" + Timer.getSecondsElapsed("QuickImpactView::runAnalysis()"));
        Timer.start("QuickImpactView::PatternTree()");
        PatternTree tree = new PatternTree(p.getTopPatterns());
        Timer.end("QuickImpactView::PatternTree()");

        PatternInfoBox impactbox = new PatternInfoBox();
        VerticalLayout vl_infobox = prepareInfoBoxLayout(impactbox);

        System.out.println("Initialising grid" + Timer.getSecondsElapsed("QuickImpactView::runAnalysis()"));
        WeightedPatternGrid grid = new WeightedPatternGrid(p);

        System.out.println("Initialising remaining layout elements" + Timer.getSecondsElapsed("QuickImpactView::runAnalysis()"));
        tree.addItemClickListener(event -> updateInfoBox(p, impactbox, event.getItem(), grid, tree));
        grid.addItemClickListener(event -> updateInfoBox(p, impactbox, event.getItem(), grid, tree));
        Panel panel_patterns = preparePanel(grid, "Patterns");
        Panel info = preparePanel(vl_infobox, "DefinedClass info");
        Panel panel_tree = preparePanel(tree, "Browser");
        HorizontalSplitPanel split_tree = prepareSplitPanel(info, panel_tree);
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
            OntologyClass pc = ((PatternTreeItem) pi).getPatternClass();
            impactbox.setValue(pc, p);
            if (pc instanceof PatternClass) {
                g.getWeightedPattern((PatternClass) pc).ifPresent(wp -> {
                    g.select(wp);
                    g.scrollTo(g.indexOf(wp));
                });
            }
            tree.expandLoad((PatternTreeItem) pi);
        } else if (pi instanceof WeightedPattern) {
            DefinedClass pc = ((WeightedPattern) pi).getPattern();
            impactbox.setValue(pc, p);

            for (PatternTreeItem pp : tree.getMapPatternTreeItem(pc)) {
                tree.expand(pp);
                tree.select(pp);
                //TODO implement scrollto
                //tree.scrollTo(1);
            }

            this.getUI().push();
        }
    }


}
