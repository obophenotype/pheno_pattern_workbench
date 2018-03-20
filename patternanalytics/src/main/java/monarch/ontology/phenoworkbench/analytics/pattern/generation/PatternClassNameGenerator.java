package monarch.ontology.phenoworkbench.analytics.pattern.generation;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;

import java.util.HashMap;
import java.util.Map;

public class PatternClassNameGenerator {

    private static final OWLDataFactory df = OWLManager.getOWLDataFactory();
    private static int id = 1;
    private static final Map<OWLClassExpression,OWLClass> map = new HashMap<>();


    public static OWLClass generateNamedClassForExpression(OWLClassExpression exp) {
        if(!map.containsKey(exp)) {
            OWLClass cl = df.getOWLClass(IRI.create("http://ebi.ac.uk#DefinedClass" + id));
            id++;
            map.put(exp, cl);
        }
        return map.get(exp);
    }
}
