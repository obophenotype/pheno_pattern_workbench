package monarch.ontology.phenoworkbench.browser.basic;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Upload.SucceededListener;
import monarch.ontology.phenoworkbench.util.ReconciliationCandidateSet;

import java.io.*;

public abstract class FileUploader implements Receiver, SucceededListener {
	private static final long serialVersionUID = -4499489112622747653L;
	public File file;

	public OutputStream receiveUpload(String filename, String mimeType) {
		file = new File(filename);

		OutputStream os = null;
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			os = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return os;
	}

	public abstract void uploadSucceeded(SucceededEvent event);

};
