package monarch.ontology.phenoworkbench.browser.candident;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.ui.Upload.SucceededEvent;

import monarch.ontology.phenoworkbench.util.CandidateKB;
import monarch.ontology.phenoworkbench.browser.basic.FileUploader;

abstract class JSONFileUploader extends FileUploader {
	private static final long serialVersionUID = -4499489112622747653L;
	public File file;
	CandidateKB kb;

	JSONFileUploader(CandidateKB kb) {
		this.kb = kb;
	}

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

	public String prettyPrintJsonString(JsonNode jsonNode) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			Object json = mapper.readValue(jsonNode.toString(), Object.class);
			return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
		} catch (Exception e) {
			return "Sorry, pretty print didn't work";
		}
	}

	public void uploadSucceeded(SucceededEvent event) {
		final ObjectMapper mapper = new ObjectMapper(new JsonFactory());
		try {
			JsonFactory factory = mapper.getFactory();
			JsonParser parser = factory.createParser(file);
			JsonNode n = mapper.readTree(parser);
			parseCandidateJson(n);
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected abstract void parseCandidateJson(JsonNode n);

};
