package monarch.ontology.phenoworkbench.browser.reconciliation;

import com.vaadin.ui.*;
import monarch.ontology.phenoworkbench.analytics.pattern.reconciliation.PatternReconciler;

class ReconcilerLayoutPanel extends VerticalLayout {

    /**
     *
     */
    private static final long serialVersionUID = -3354993059350574732L;
   
    private final VerticalLayout tab_reconciliation = new VerticalLayout();
    private final ReconciliationTreePanel tab_tree;
    private final ReconciliationGridPanel tab_grid;
    private final TabSheet tabsheet = new TabSheet();

    ReconcilerLayoutPanel(PatternReconciler p) {
        setSizeFull();
        setMargin(false);
        tab_grid = new ReconciliationGridPanel(p,tab_reconciliation);
        tab_tree = new ReconciliationTreePanel(p.getPatternProvider(),p,tab_reconciliation);
        tab_reconciliation.addListener(this::switchTabToReconciliation);
        addComponent(prepareTabs());    
    }

    private void switchTabToReconciliation(Event event) {
        System.out.println(event);
        System.out.println(event.getSource());
        tabsheet.setSelectedTab(tab_reconciliation);
    }

    private TabSheet prepareTabs() {

        tabsheet.addTab(tab_grid, "Reconciliation Candidates");
        tabsheet.addTab(tab_reconciliation, "Reconciliation");
        tabsheet.addTab(tab_tree, "Tree Browser");
        tabsheet.setSelectedTab(tab_grid);
        return tabsheet;
    }

}
