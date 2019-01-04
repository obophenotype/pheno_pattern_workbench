package monarch.ontology.phenoworkbench.browser.candident;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.vaadin.server.DownloadStream;
import com.vaadin.server.Page;
import com.vaadin.server.ResourceReference;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Upload;
import com.vaadin.ui.VerticalLayout;

import monarch.ontology.phenoworkbench.uiutils.basic.LabelManager;
import monarch.ontology.phenoworkbench.util.CandidateKB;
import monarch.ontology.phenoworkbench.util.OntologyClass;
import monarch.ontology.phenoworkbench.analytics.pattern.reconciliation.CandidateIdentifierApp;

class BlacklistGridPanel extends VerticalLayout {
	private static final long serialVersionUID = 3484502786500683355L;

	private final BlacklistGrid grid;
	private final Upload upload;
	private final Button bt_load = new Button("Download");
	private final CandidateKB gp;

	BlacklistGridPanel(CandidateKB gp, CandidateIdentifierApp app) {
		grid = new BlacklistGrid(gp);
		this.gp = gp;
		setSizeFull();
		setMargin(false);
		JSONFileUploader receiver = new JSONFileUploader(gp) {

			private static final long serialVersionUID = 4955354018657336505L;

			@Override
			protected void parseCandidateJson(JsonNode n) {
				{
					System.out.println(prettyPrintJsonString(n));
					for (JsonNode blacklisted : n) {
						String iri = blacklisted.path("blacklisted").asText();
						String context = blacklisted.path("context").asText();
						Set<OntologyClass> ontologyClasses = app.getOntologyClasses(iri);
						if (context.equals("global")) {
							ontologyClasses.forEach(cl -> kb.blacklist(cl));
						} else {
							ontologyClasses.forEach(cl -> app.getOntologyClasses(context).forEach(con -> kb.blacklist(con,cl)));
						}
					}
				}
			}

		};

		upload = new Upload(null, receiver);
		upload.addSucceededListener(receiver);

		bt_load.addClickListener(e -> doExport());

		addComponent(layoutHeader());
		addComponent(grid);
	}

	private void doExport() {
		downloadExportFile(gp.exportBlacklist().getBytes());
	}

	private void downloadExportFile(byte[] toDownload) {
		StreamResource.StreamSource source = new StreamResource.StreamSource() {
			private static final long serialVersionUID = 21828054412044862L;

			@Override
			public InputStream getStream() {
				return new ByteArrayInputStream(toDownload);
			}
		};

		StreamResource resource = new StreamResource(source, "blacklist.xml") {
			private static final long serialVersionUID = -552993349680185987L;
			DownloadStream downloadStream;

			@Override
			public DownloadStream getStream() {
				if (downloadStream == null)
					downloadStream = super.getStream();
				return downloadStream;
			}
		};
		resource.getStream().setParameter("Content-Disposition", "attachment;filename=\"blacklist.xml\"");
		resource.getStream().setParameter("Content-Type", "application/octet-stream");
		resource.getStream().setCacheTime(0);
		ResourceReference ref = new ResourceReference(resource, this, "download");
		this.setResource("download", resource);
		Page.getCurrent().open(ref.getURL(), null);
	}

	private Component layoutHeader() {
		HorizontalLayout vl = new HorizontalLayout();
		vl.setMargin(false);
		vl.setWidth("100%");
		vl.addComponent(LabelManager.htmlLabel("<strong>Blacklist</strong>"));
		bt_load.setWidth("120px");
		vl.addComponent(bt_load);
		vl.addComponent(upload);
		return vl;
	}

}
