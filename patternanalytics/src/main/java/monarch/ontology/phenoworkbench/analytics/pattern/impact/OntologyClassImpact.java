package monarch.ontology.phenoworkbench.analytics.pattern.impact;

import monarch.ontology.phenoworkbench.util.OntologyClass;

import java.util.HashMap;
import java.util.Map;

public class OntologyClassImpact {
    private final Map<String, Integer> directImpactByO = new HashMap<>();
    private final Map<String, Integer> indirectImpactByO = new HashMap<>();
    private int directImpact = 0;
    private int indirectImpact = 0;
    private final OntologyClass pattern;

    OntologyClassImpact(OntologyClass pattern) {
        this.pattern = pattern;
    }

    private void incrementDirectImpactByO(String oid) {
        if (!directImpactByO.containsKey(oid)) {
            directImpactByO.put(oid, 0);
        }
        directImpactByO.put(oid, directImpactByO.get(oid) + 1);
    }

    private void incrementIndirectImpactByO(String oid) {
        if (!indirectImpactByO.containsKey(oid)) {
            indirectImpactByO.put(oid, 0);
        }
        indirectImpactByO.put(oid, indirectImpactByO.get(oid) + 1);
    }

    void incrementImpactByO(String oid, boolean direct) {
        if(direct) {
            incrementDirectImpactByO(oid);
        } else {
            incrementIndirectImpactByO(oid);
        }
    }

    void incrementImpact(boolean direct) {
        if(direct) {
            directImpact++;
        } else {
            indirectImpact++;
        }
    }


    public int getDirectImpact() {
        return directImpact;
    }

    public int getIndirectImpact() {
        return indirectImpact;
    }

    public Map<String,Integer> getDirectImpactByO() {
        return directImpactByO;
    }

    public Map<String,Integer> getIndirectImpactByO() {
        return indirectImpactByO;
    }

    public OntologyClass getPattern() {
        return pattern;
    }
}