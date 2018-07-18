package monarch.ontology.phenoworkbench.browser.reconciliation;

import com.vaadin.server.Page;
import com.vaadin.server.ResourceReference;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.*;
import monarch.ontology.phenoworkbench.analytics.pattern.reconciliation.CandidateIdentifierApp;
import monarch.ontology.phenoworkbench.analytics.pattern.reconciliation.PatternReconciler;
import monarch.ontology.phenoworkbench.browser.basic.HTMLRenderUtils;
import monarch.ontology.phenoworkbench.browser.basic.IOUtils;
import monarch.ontology.phenoworkbench.browser.candident.CandidentLayoutPanel;
import monarch.ontology.phenoworkbench.util.KB;
import monarch.ontology.phenoworkbench.util.PatternReconciliationCandidate;
import monarch.ontology.phenoworkbench.util.ReconciliationCandidateSet;
import monarch.ontology.phenoworkbench.util.UberOntology;

class MappingLayoutPanel extends VerticalLayout {

    /**
     *
     */
    private static final long serialVersionUID = -3354993059350574732L;
   
    private final VerticalLayout tab_reconciliation = new VerticalLayout();
    private final ReconciliationTreePanel tab_tree;
    private final MappingGridPanel tab_grid;
    private final MappingGridPanel tab_grid_mappings;
    private final MappingGridPanel tab_grid_blacklist;
    CandidentLayoutPanel l_rec;
    private final TabSheet tabsheet = new TabSheet();
    private final KB kb = KB.getInstance();

    MappingLayoutPanel(PatternReconciler p, ReconciliationCandidateSet cset) {
        CandidateIdentifierApp app = new CandidateIdentifierApp(UberOntology.instance().getOntologyEntries());
        app.setImports(p.getImports());
        app.runAnalysis();
        l_rec = new CandidentLayoutPanel(app);
        setSizeFull();
        setMargin(false);
        tab_grid = new MappingGridPanel(p,cset, c -> infoClick(c,p,tab_reconciliation), c -> kb.addMapping(c), c->kb.blacklistMapping(c),true);
        tab_tree = new ReconciliationTreePanel(p,cset,tab_reconciliation);
        tab_reconciliation.addListener(this::switchTabToReconciliation);
        tab_grid_mappings = new MappingGridPanel(p,kb.getMappings(), c -> infoClick(c,p,tab_reconciliation), this::downloadDescription, kb::removeMapping,false);
        tab_grid_blacklist = new MappingGridPanel(p,kb.getMappingBlacklist(), c -> infoClick(c,p,tab_reconciliation), this::downloadDescription, kb::whitelistMapping,false);
        addComponent(prepareTabs());    
    }

    private void infoClick(PatternReconciliationCandidate recon, PatternReconciler r, VerticalLayout vl) {
        vl.removeAllComponents();
        vl.addComponent(new ReconciliationPanel(recon, r));
    }


    private void switchTabToReconciliation(Event event) {
        System.out.println(event);
        System.out.println(event.getSource());
        tabsheet.setSelectedTab(tab_reconciliation);
    }

    private TabSheet prepareTabs() {

        tabsheet.addTab(tab_grid, "Reconciliation Candidates");
        tabsheet.addTab(tab_reconciliation, "Reconciliation Info");
        tabsheet.addTab(tab_tree, "Tree Browser");
        tabsheet.addTab(tab_grid_blacklist, "Candidate Search");
        tabsheet.addTab(tab_grid_mappings, "Validated Mappings");
        tabsheet.addTab(tab_grid_blacklist, "Mapping Blacklist");

        tabsheet.setSelectedTab(tab_grid);
        return tabsheet;
    }

    private void downloadDescription(PatternReconciliationCandidate c) {
        String out = "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "  <head>\n" +
                "    <meta charset=\"utf-8\">\n" +
                "    <title>title</title>\n" +
                "    <link rel=\"stylesheet\" href=\"style.css\">\n" +
                "    <script src=\"script.js\"></script>\n" +
                "  </head>\n" +
                "  <body>\n" +
                "    <table>\n" +
                "    <tr>\n" +
                "    <td>"+ HTMLRenderUtils.renderOntologyClass(c.getP1()) + "</td>\n" +
                "    <td>"+HTMLRenderUtils.renderOntologyClass(c.getP2()) + "</td>\n" +
                "    </tr>\n" +
                "    </table>\n" +
                "  </body>\n" +
                "</html>";
        downloadExportFile(out.getBytes(),"reconciliation_"+c.getReconciliationID()+".html");
    }


    private void downloadExportFile(byte[] toDownload, String filename) {
        StreamResource resource = IOUtils.getStreamResource(toDownload,filename);
        ResourceReference ref = new ResourceReference(resource, this, "download");
        this.setResource("download", resource);
        Page.getCurrent().open(ref.getURL(), null);
    }

}
