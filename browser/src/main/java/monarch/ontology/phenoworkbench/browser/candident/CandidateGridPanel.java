package monarch.ontology.phenoworkbench.browser.candident;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.vaadin.server.Page;
import com.vaadin.server.ResourceReference;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Upload;
import com.vaadin.ui.VerticalLayout;

import monarch.ontology.phenoworkbench.browser.basic.IOUtils;
import monarch.ontology.phenoworkbench.uiutils.basic.LabelManager;
import monarch.ontology.phenoworkbench.util.CandidateKB;
import monarch.ontology.phenoworkbench.util.KB;
import monarch.ontology.phenoworkbench.util.OntologyClass;
import monarch.ontology.phenoworkbench.util.Candidate;

public class CandidateGridPanel extends VerticalLayout {
	private static final long serialVersionUID = 3484502786500683355L;

	private final CandidateGrid grid;
	private final Upload upload;
	private final Button bt_load = new Button("Download");
	private final CandidateKB gp;

	public CandidateGridPanel() {
		gp = KB.getInstance();
		grid = new CandidateGrid(gp);
		setSizeFull();
		setMargin(false);
		JSONFileUploader receiver = new JSONFileUploader(gp) {

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
								Set<OntologyClass> ontologyClasses = KB.getInstance().getOntologyClasses(iri);
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
		StreamResource resource = IOUtils.getStreamResource(toDownload,"candidates.xml");
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
