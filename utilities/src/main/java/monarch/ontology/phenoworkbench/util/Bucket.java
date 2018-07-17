package monarch.ontology.phenoworkbench.util;

import java.util.BitSet;
import java.util.Map;

public class Bucket {

    private final String description;
    private final Map<String,String> bucket;

    public Bucket(String description,Map<String,String> bucket) {
        this.description = description;
        this.bucket = bucket;
    }

    public String getLabel() {
        return description;
    }

    public Map<String, String> getBucket() {
        return bucket;
    }
}
