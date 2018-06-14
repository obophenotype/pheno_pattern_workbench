package monarch.ontology.phenoworkbench.util;

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
        return Objects.equals(getSuper_c(), that.getSuper_c()) &&
                Objects.equals(getSub_c(), that.getSub_c());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getSuper_c(), getSub_c());
    }

    public Subsumption(OWLClass super_c, OWLClass sub_c) {
        this.super_c = super_c;
        this.sub_c = sub_c;

    }

    public OWLClass getSuper_c() {
        return super_c;
    }

    public OWLClass getSub_c() {
        return sub_c;
    }
}
