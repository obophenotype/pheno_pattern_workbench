package monarch.ontology.phenoworkbench.browser.candident;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

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

import monarch.ontology.phenoworkbench.analytics.pattern.reconciliation.Bucket;
import monarch.ontology.phenoworkbench.analytics.pattern.reconciliation.CandidateIdentifierApp;
import monarch.ontology.phenoworkbench.browser.basic.LabelManager;

class BucketGridPanel extends VerticalLayout {

	private static final long serialVersionUID = 9012194278942710675L;

	private final BucketGrid grid;
	private final Button bt_save_bucket = new Button("Download");
	private final Upload upload;
	private final CandidateKB kb;

	BucketGridPanel(CandidateKB kb, CandidateIdentifierApp app) {
		this.grid = new BucketGrid(kb);
		this.kb = kb;

		JSONFileUploader receiver = new JSONFileUploader(kb, app) {

			private static final long serialVersionUID = -2745475701397147980L;

			@Override
			protected void parseCandidateJson(JsonNode n) {
				{
					System.out.println(prettyPrintJsonString(n));
					for (JsonNode candidate : n) {
						Map<String, String> searches = new HashMap<>();
						String label = candidate.path("description").asText();
						System.out.println("Loading bucket " + label);
						for (JsonNode c : candidate.path("searches")) {
							String oid = c.path("oid").asText();
							String search = c.path("search").asText();
							searches.put(oid, search);
						}
						if (!searches.isEmpty()) {
							Bucket c = new Bucket(label, searches);
							kb.addBucket(c);
						}
					}
				}
			}
		};
		upload = new Upload(null, receiver);
		upload.addSucceededListener(receiver);
		bt_save_bucket.addClickListener(e -> doExport());
		setSizeFull();
		setMargin(false);

		addComponent(layoutHeader());
		addComponent(grid);
	}

	private void doExport() {
		downloadExportFile(kb.exportBuckets().getBytes());
	}

	private void downloadExportFile(byte[] toDownload) {
		// TODO this is unnecessarily duplicate from CandidateGridPanel
		StreamResource.StreamSource source = new StreamResource.StreamSource() {
			private static final long serialVersionUID = 21828054412044862L;

			@Override
			public InputStream getStream() {
				return new ByteArrayInputStream(toDownload);
			}
		};

		StreamResource resource = new StreamResource(source, "buckets.xml") {
			private static final long serialVersionUID = -552993349680185987L;
			DownloadStream downloadStream;

			@Override
			public DownloadStream getStream() {
				if (downloadStream == null)
					downloadStream = super.getStream();
				return downloadStream;
			}
		};
		resource.getStream().setParameter("Content-Disposition", "attachment;filename=\"buckets.xml\"");
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
		vl.addComponent(LabelManager.htmlLabel("<strong>Buckets</strong>"));
		bt_save_bucket.setWidth("120px");
		vl.addComponent(upload);
		vl.addComponent(bt_save_bucket);
		return vl;
	}

}
