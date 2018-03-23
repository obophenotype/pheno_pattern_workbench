package monarch.ontology.phenoworkbench.util;

import org.semanticweb.owlapi.model.IRI;

public class IRIMapping {
    private final IRI i1;
    private final IRI i2;
    private final double jackard;
    private final double sbcl;

    IRIMapping(IRI i1, IRI i2, double jackard, double sbcl) {
     this.i1 = i1;
     this.i2 = i2;
     this.jackard = jackard;
     this.sbcl = sbcl;
    }

    public IRI getI1() {
        return i1;
    }

    public IRI getI2() {
        return i2;
    }

    public double getJackard() {
        return jackard;
    }

    public double getSbcl() {
        return sbcl;
    }
}
