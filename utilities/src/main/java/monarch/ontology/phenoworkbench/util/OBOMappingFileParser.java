package monarch.ontology.phenoworkbench.util;

import org.apache.commons.io.FileUtils;
import org.semanticweb.owlapi.model.IRI;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class OBOMappingFileParser {

    public static List<IRIMapping> parseMappings(File mappinFile) {

        List<IRIMapping> mappings = new ArrayList<>();
        if (mappinFile.isFile()) {
            try {
                List<String> lines = FileUtils.readLines(mappinFile, Charset.forName("utf-8"));
                for (String line : lines) {
                    String[] explosion = line.split("\t");
                    IRI iri1 = getIRI(explosion[0]);
                    //String logo1 = explosion[1];
                    IRI iri2 = getIRI(explosion[2]);
                    //String logo2 = explosion[3];
                    Double v1 = Double.valueOf(explosion[4]);
                    Double v2 = Double.valueOf(explosion[5]);
                    mappings.add(new IRIMapping(iri1, iri2, v1, v2));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return mappings;
    }

    private static IRI getIRI(String s) {
        String v = s;
        if (v.contains(":")) {
            String prefix = v.substring(0, v.indexOf(":"));
            v = v.replace(prefix + ":", "http://purl.obolibrary.org/obo/" + prefix + "_");
        }
        return IRI.create(v);
    }

}
