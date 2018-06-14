package monarch.ontology.phenoworkbench.analytics.pattern.reconciliation;

import monarch.ontology.phenoworkbench.analytics.pattern.generation.OntologyClass;
import org.semanticweb.owlapi.model.OWLClass;

public class OntologyClassMatch {

    private final OntologyClass s1;
    private final OntologyClass s2;
    private double jacc_super = 0.0;
    private double jacc_substring = 0.0;
    private double jacc_bucketsim = 0.0;
    private double matchstrength = (getJacc_super() + getJacc_substring() + getJacc_bucketsim())/3;

    public OntologyClassMatch(OntologyClass c1, OntologyClass c2) {
        s1 = c2;
        s2 = c2;
    }

    public double getJacc_super() {
        return jacc_super;
    }

    public void setJacc_super(double jacc_super) {
        this.jacc_super = jacc_super;
    }

    public double getJacc_substring() {
        return jacc_substring;
    }

    public void setJacc_substring(double jacc_substring) {
        this.jacc_substring = jacc_substring;
    }

    public double getJacc_bucketsim() {
        return jacc_bucketsim;
    }

    public void setJacc_bucketsim(double jacc_bucketsim) {
        this.jacc_bucketsim = jacc_bucketsim;
    }

    public double getMatchstrength() {
        return matchstrength;
    }

    public void setMatchstrength(double matchstrength) {
        this.matchstrength = matchstrength;
    }

    public OntologyClass getS1() {
        return s1;
    }

    public OntologyClass getS2() {
        return s2;
    }
}
