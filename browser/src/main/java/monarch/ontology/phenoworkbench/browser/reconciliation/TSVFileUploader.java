package monarch.ontology.phenoworkbench.browser.reconciliation;

import com.vaadin.ui.Upload.SucceededEvent;
import monarch.ontology.phenoworkbench.browser.basic.FileUploader;
import monarch.ontology.phenoworkbench.util.IRIMapping;
import monarch.ontology.phenoworkbench.util.OBOMappingFileParser;
import monarch.ontology.phenoworkbench.util.ReconciliationCandidateSet;

import java.io.*;
import java.util.List;

abstract class TSVFileUploader extends FileUploader {
	ReconciliationCandidateSet kb;

	TSVFileUploader(ReconciliationCandidateSet kb) {
		this.kb = kb;
	}

	public void uploadSucceeded(SucceededEvent event) {
		List<IRIMapping> list = OBOMappingFileParser.parseMappings(file);
		System.out.println("NR MAPPINGS: "+list.size());
		loadMappings(list);
	}

	protected abstract void loadMappings(List<IRIMapping> list);

};
