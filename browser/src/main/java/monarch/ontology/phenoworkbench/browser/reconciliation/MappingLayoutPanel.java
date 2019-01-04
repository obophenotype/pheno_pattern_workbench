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
import monarch.ontology.phenoworkbench.util.*;

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
    private final CandidentLayoutPanel l_rec;
    private final TabSheet tabsheet = new TabSheet();

    MappingLayoutPanel(PatternReconciler p, ReconciliationCandidateSet cset) {
        CandidateIdentifierApp app = new CandidateIdentifierApp(UberOntology.instance().getOntologyEntries());
        app.setImports(p.getImports());
        app.runAnalysis();
        l_rec = new CandidentLayoutPanel(app);
        setSizeFull();
        setMargin(false);
        KB kb = KB.getInstance();
        tab_grid = new MappingGridPanel(p,cset, c -> infoClick(c,p,tab_reconciliation), kb::addMapping, kb::blacklistMapping,true);
        tab_tree = new ReconciliationTreePanel(p,cset, c -> infoClick(c,p,tab_reconciliation), kb::addMapping, kb::blacklistMapping);
        tab_reconciliation.addListener(this::switchTabToReconciliation);
        tab_grid_mappings = new MappingGridPanel(p, kb.getMappings(), c -> infoClick(c,p,tab_reconciliation), this::downloadDescription, kb::removeMapping,false);
        tab_grid_blacklist = new MappingGridPanel(p, kb.getMappingBlacklist(), c -> infoClick(c,p,tab_reconciliation), this::downloadDescription, kb::whitelistMapping,false);
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
        tabsheet.addTab(l_rec, "Candidate Search");
        tabsheet.addTab(tab_grid_mappings, "Validated Mappings");
        tabsheet.addTab(tab_grid_blacklist, "Mapping Blacklist");

        tabsheet.setSelectedTab(tab_grid);
        return tabsheet;
    }

    private void downloadDescription(PatternReconciliationCandidate c) {
        OntologyClass p1 = c.getP1();
        String p1Remainder = p1.getOWLClass().getIRI().getRemainder().or(p1.getIri());
        OntologyClass p2 = c.getP2();
        String p2Remainder = p2.getOWLClass().getIRI().getRemainder().or(p2.getIri());
        String reconID = p1Remainder+"_"+p2Remainder;
        String out = "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "  <head>\n" +
                "    <meta charset=\"utf-8\">\n" +
                "    <title>title</title>\n" + 
                "<style>\n" + 
                        
				" td.padding {\n" + 
				"    padding: 20px;\n" + 
				"    font-family: verdana;\n" + 
				"}\n" +
				
				" .border {\n" + 
				"    border: 2px solid black; \n" + 
				"    padding: 20px;\n" + 
				"}\n" +
								
				" .definition {\n" + 
				"    font-size: large; \n" + 
				"}\n" +
				
                "</style>" +
                
                "  </head>\n" +
                "  <body>\n" +
                "<strong>Reconilication ID: "+reconID+"</strong><br />" +
                "    <table>\n" +
                rowBegin("<strong>Definition 1</strong>","<strong>Definition 2</strong>","padding") +
                rowBegin(h(HTMLRenderUtils.renderOLSLinkout(p1)+" ("+p1Remainder+")"),h(HTMLRenderUtils.renderOLSLinkout(p2)+" ("+p2Remainder+")"),"padding") +
                rowBegin(a(p1.getIri(),p1.getIri()),a(p2.getIri(),p2.getIri()),"padding") +
                rowBegin(div("<strong>Definition:</strong> "+p1.getDescription(),"border"),div("<strong>Definition:</strong> "+p2.getDescription(),"border"),"padding") +
                rowBegin(h("Logical definition")+div(HTMLRenderUtils.renderOntologyDefinition(p1),"border definition"),h("Logical definition")+div(HTMLRenderUtils.renderOntologyDefinition(p2),"border definition"),"padding") +
                rowBegin(renderSignatureTable(p1),renderSignatureTable(p2),"padding") +
                "    <tr><td colspan=\"2\" class=\"padding\">"+renderReconciliationInfoTable(c)+"</td></tr>" +
                "    </table>\n" +
                "  </body>\n" +
                "</html>";
        downloadExportFile(out.getBytes(),"reconciliation_"+reconID+".html");
    }

    private String h(String s) {
		return "<h2>"+s+"</h2>";
	}

	private String div(String content, String s) {
		return "<div class='"+s+"'>"+content+"</div>";
	}

	private String a(String label, String href) {
		return "<a href='"+href+"' target='_blank'>"+label+"</a>";
	}

	private String renderReconciliationInfoTable(PatternReconciliationCandidate c) {
    	StringBuilder table = new StringBuilder();
			table.append("<table>\n"); 
			table.append("<tr><td>Same grammar?</td><td>"+c.isGrammarEquivalent()+"</td></tr>");
			table.append("<tr><td>Logically equivalent?</td><td>"+c.isGrammarEquivalent()+"</td></tr>");
			table.append("<tr><td>Defined class 1 SubClassOf Defined class 2?</td><td>"+c.isP1SubclassOfP2()+"</td></tr>");
			table.append("<tr><td>Defined class 2 SubClassOf Defined class 1?</td><td>"+c.isP2SubclassOfP1()+"</td></tr>");
			table.append("<tr><td>Signature overlap:</td><td>"+c.getSignatureOverlap()+"</td></tr>");
			table.append("<tr><td colspan='2'>");
			table.append("<h3>Common ancestors:</h3>"
					+ "<table border='1'>\n"); 
			c.getCommonAncestors().forEach(e -> {
				table.append("<tr><td>"+e.getLabel()+"</td><td>"+a(e.getIri(),e.getIri())+"</td></tr>");
			});
			table.append("</table>\n"); 
			table.append("</td></tr>");
			table.append("</table>\n"); 
		
	return table.toString();
	}

	private String renderSignatureTable(OntologyClass p) {
    		StringBuilder table = new StringBuilder();
    		if(p instanceof DefinedClass) {
    			DefinedClass d = (DefinedClass)p; 
    			table.append("<h3>Definition signature</h3><table border='1'>\n"); 
    			d.getDefiniton().getSignature().forEach(e -> {
    				table.append("<tr><td>"+UberOntology.instance().getRender().getLabel(e)+"</td><td>"+a(e.getIRI().toString(),e.getIRI().toString())+"</td></tr>");
    			});
    			table.append("</table>\n"); 
    		}
    		
		return table.toString();
	}

	private String rowBegin(String s1, String s2, String td_class) {
        return "    <tr>\n" +
                "    <td class=\""+td_class+"\">" + s1 + "</td>\n" +
                "    <td class=\""+td_class+"\">" + s2 + "</td>\n" +
                "    </tr>\n";
    }


    private void downloadExportFile(byte[] toDownload, String filename) {
        StreamResource resource = IOUtils.getStreamResource(toDownload,filename);
        ResourceReference ref = new ResourceReference(resource, this, "download");
        this.setResource("download", resource);
        Page.getCurrent().open(ref.getURL(), null);
    }

}
