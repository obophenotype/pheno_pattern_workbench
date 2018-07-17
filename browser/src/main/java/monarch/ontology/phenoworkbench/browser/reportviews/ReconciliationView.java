package monarch.ontology.phenoworkbench.browser.reportviews;

import com.vaadin.server.Page;
import com.vaadin.server.ResourceReference;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import monarch.ontology.phenoworkbench.browser.basic.HTMLRenderUtils;
import monarch.ontology.phenoworkbench.browser.basic.IOUtils;
import monarch.ontology.phenoworkbench.browser.basic.LabelManager;
import monarch.ontology.phenoworkbench.browser.reconciliation.MappingGridPanel;
import monarch.ontology.phenoworkbench.browser.reconciliation.ReconciliationTreePanel;
import monarch.ontology.phenoworkbench.util.KB;
import monarch.ontology.phenoworkbench.util.PatternReconciliationCandidate;
import monarch.ontology.phenoworkbench.util.StringUtils;

import java.util.Collection;

public class ReconciliationView extends VerticalLayout {

	/**
	 *
	 */
	private static final long serialVersionUID = 255453218992079876L;
	private final MappingGridPanel tab_grid;
	private final MappingGridPanel tab_grid_blacklist;
	private KB kb = KB.getInstance();
	private Button bt_downloadValidated = new Button("Download Validated Mappings");
	private final VerticalLayout tab_reconciliation = new VerticalLayout();
	private final TabSheet tabsheet = new TabSheet();

	public ReconciliationView() {
		setMargin(false);
		setSpacing(false);
		setWidth("100%");
		setHeight("100%");
		tab_grid = new MappingGridPanel(kb.getMappings(), c -> infoClick(c,tab_reconciliation), this::downloadDescription, kb::removeMapping,false);
		tab_grid_blacklist = new MappingGridPanel(kb.getMappingBlacklist(), c -> infoClick(c,tab_reconciliation), this::downloadDescription, kb::whitelistMapping,false);
		addComponent(LabelManager.labelH1("Reconciliation Canidates"));
		addComponent(bt_downloadValidated);
		bt_downloadValidated.addClickListener(click->downloadValidatedMappings(kb.getMappings().items()));
		addComponent(prepareTabs());
	}

	private TabSheet prepareTabs() {
		tabsheet.addTab(tab_grid, "Mapping Candidates");
		tabsheet.addTab(tab_grid_blacklist, "Mapping Blacklist");
		tabsheet.addTab(tab_reconciliation, "Mapping info");
		tabsheet.setSelectedTab(tab_grid);
		return tabsheet;
	}

	private void downloadValidatedMappings(Collection<PatternReconciliationCandidate> items) {
		String tsv = "";
		for(PatternReconciliationCandidate c:items) {
			//HP:0100300	Desmin bodies	MP:0003084	abnormal skeletal muscle fiber morphology	0.8205128205	1
			//I add similarity here twice because the original file has two different similarity measures (see upheno/mappings)
			tsv+=""+c.getP1().getIri().toString()+"	"+c.getP1().getLabel()+"	"+c.getP2().getIri().toString()+"	"+c.getP2().getLabel()+"	"+c.getSimiliarity()+"	"+c.getSimiliarity()+"\n";
		}
		downloadExportFile(tsv.getBytes(),"validated_mapping_"+StringUtils.getCurrentDateString()+".tsv");
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
			"    <td>"+HTMLRenderUtils.renderOntologyClass(c.getP1()) + "</td>\n" +
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

	private void infoClick(PatternReconciliationCandidate recon, VerticalLayout vl) {
		vl.removeAllComponents();
		Notification.show("Not yet implemented;");
	}

}
