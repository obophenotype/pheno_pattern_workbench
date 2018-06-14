package monarch.ontology.phenoworkbench.browser.candident;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.vaadin.server.DownloadStream;
import com.vaadin.server.Page;
import com.vaadin.server.ResourceReference;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Upload;
import com.vaadin.ui.VerticalLayout;

import monarch.ontology.phenoworkbench.analytics.pattern.generation.OntologyClass;
import monarch.ontology.phenoworkbench.analytics.pattern.reconciliation.Candidate;
import monarch.ontology.phenoworkbench.analytics.pattern.reconciliation.CandidateIdentifierApp;
import monarch.ontology.phenoworkbench.browser.basic.LabelManager;

class CandidateGridPanel extends VerticalLayout {
	private static final long serialVersionUID = 3484502786500683355L;

	private final CandidateGrid grid;
	private final Upload upload;
	private final Button bt_load = new Button("Download");
	private final CandidateKB gp;

	CandidateGridPanel(CandidateKB gp, CandidateIdentifierApp app) {
		grid = new CandidateGrid(gp);
		this.gp = gp;
		setSizeFull();
		setMargin(false);
		JSONFileUploader receiver = new JSONFileUploader(gp,app) {

			@Override
			protected void parseCandidateJson(JsonNode n) {
				 {
						System.out.println(prettyPrintJsonString(n));
						for (JsonNode candidate : n) {
							Set<OntologyClass> candidateClasses = new HashSet<>();
							boolean candidate_complete = true;
							String label = candidate.path("description").asText();
							System.out.println("Loading candidate "+label);
							for (JsonNode c : candidate.path("classes")) {
								String iri = c.path("iri").asText();
								Set<OntologyClass> ontologyClasses = app.getOntologyClasses(iri);
								if (ontologyClasses.isEmpty()) {
									Notification.show(iri + " not in currently loaded classes!");
									candidate_complete = false;
									break;
								}
								candidateClasses.addAll(ontologyClasses);
							}
							if (candidate_complete&&!candidateClasses.isEmpty()) {
								Candidate c = new Candidate();
								c.addOntologyClasses(candidateClasses);
								kb.addCandidate(c);
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
		downloadExportFile(gp.exportCandidate().getBytes());
	}

	private void downloadExportFile(byte[] toDownload) {
		StreamResource.StreamSource source = new StreamResource.StreamSource() {
			private static final long serialVersionUID = 21828054412044862L;

			@Override
			public InputStream getStream() {
				return new ByteArrayInputStream(toDownload);
			}
		};

		StreamResource resource = new StreamResource(source, "candidates.xml") {
			private static final long serialVersionUID = -552993349680185987L;
			DownloadStream downloadStream;

			@Override
			public DownloadStream getStream() {
				if (downloadStream == null)
					downloadStream = super.getStream();
				return downloadStream;
			}
		};
		resource.getStream().setParameter("Content-Disposition", "attachment;filename=\"candidates.xml\"");
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
		vl.addComponent(LabelManager.htmlLabel("<strong>Candidates</strong>"));
		bt_load.setWidth("120px");
		vl.addComponent(bt_load);
		vl.addComponent(upload);
		return vl;
	}

}
