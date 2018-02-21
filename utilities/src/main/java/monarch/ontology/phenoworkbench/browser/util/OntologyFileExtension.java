package monarch.ontology.phenoworkbench.browser.util;

import java.io.File;
import java.io.FilenameFilter;

public class OntologyFileExtension implements FilenameFilter {
    public boolean accept(File dir, String name) {
        return name.toLowerCase().endsWith(".owl") || name.toLowerCase().endsWith(".rdf");
    }
}
