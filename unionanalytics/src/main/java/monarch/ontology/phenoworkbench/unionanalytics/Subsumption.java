package monarch.ontology.phenoworkbench.unionanalytics;

import org.semanticweb.owlapi.model.OWLClass;

import java.util.Objects;

public class Subsumption {
    private final OWLClass super_c;
    private final OWLClass sub_c;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Subsumption)) return false;
        Subsumption that = (Subsumption) o;
        return Objects.equals(super_c, that.super_c) &&
                Objects.equals(sub_c, that.sub_c);
    }

    @Override
    public int hashCode() {

        return Objects.hash(super_c, sub_c);
    }

    public Subsumption(OWLClass super_c, OWLClass sub_c) {
        this.super_c = super_c;
        this.sub_c = sub_c;

    }
}
