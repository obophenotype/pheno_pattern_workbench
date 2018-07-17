package monarch.ontology.phenoworkbench.util;

import org.semanticweb.owlapi.model.IRI;

import java.util.HashMap;
import java.util.Map;

public class IRIMapping {
    private final IRI i1;
    private final IRI i2;
    private final String main_similarity;
    private final double similarity;
    private final Map<String,Double> metrics = new HashMap<>();

    IRIMapping(IRI i1, IRI i2, String main_similarity, double similarity) {
     this.i1 = i1;
     this.i2 = i2;
     this.main_similarity = main_similarity;
     this.similarity = similarity;
    }

    public IRI getI1() {
        return i1;
    }

    public IRI getI2() {
        return i2;
    }

    public double getSimilarity() {
        return similarity;
    }

    public void addMetric(String name,double metric) {
        metrics.put(name, metric);
    }

    public Map<String,Double> getMetrics() {
        return metrics;
    }

    public String getSimilarityMeasureName() {
        return main_similarity;
    }
}
