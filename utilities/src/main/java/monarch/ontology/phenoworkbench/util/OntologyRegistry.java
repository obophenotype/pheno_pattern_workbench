package monarch.ontology.phenoworkbench.util;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class OntologyRegistry {
	
	private final Map<String,String> phenotypeontologies = new HashMap<>();
    private final Map<String,Set<String>> phenotypeclasses = new HashMap<>();
	
	public OntologyRegistry(File ontologies, File roots) {
        readOntologyFile(ontologies);
        if(roots.exists()&&roots.isFile()) {
            readOntologyClasses(roots);
        }
    }

    private void readOntologyFile(File f) {

        try {
            for(String line: FileUtils.readLines(f,"utf-8")) {
                String oid = line.split(",")[0].trim();
                String iri = line.split(",")[1].trim();
                addOntology(oid,iri);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readOntologyClasses(File f) {
        try {
            for(String line: FileUtils.readLines(f,"utf-8")) {
                String oid = line.split(",")[0].trim();
                String iri = line.split(",")[1].trim();
                addPhenotypeclass(oid,iri);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	
	public void addOntology(String oid,String iri) {
        phenotypeontologies.put(oid,iri);
	}

    public void addPhenotypeclass(String oid,String iri) {
        if(!phenotypeclasses.containsKey(oid)) {
            phenotypeclasses.put(oid,new HashSet<>());
        }
        phenotypeclasses.get(oid).add(iri);
    }

    public Set<OntologyEntry> getOntologies() {
	    Set<OntologyEntry> entries = new HashSet<>();
	    for(String oid:phenotypeontologies.keySet()) {
	        OntologyEntry e = new OntologyEntry(oid,phenotypeontologies.get(oid));
	        if(phenotypeclasses.containsKey(oid)) {
	            e.addRootClassesOfInterest(phenotypeclasses.get(oid));
            }
            entries.add(e);
        }
	    return entries;
    }


}
