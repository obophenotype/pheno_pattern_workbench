package monarch.ontology.phenoworkbench.browser.analytics;

import monarch.ontology.phenoworkbench.browser.util.OntologyUtils;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;

import java.util.HashMap;
import java.util.Map;

public class PatternClassNameGenerator {

    static OWLDataFactory df = OWLManager.getOWLDataFactory();
    static int id = 1;
    static Map<OWLClassExpression,OWLClass> map = new HashMap<>();


    public static OWLClass generateNamedClassForExpression(OWLClassExpression exp) {
        if(!map.containsKey(exp)) {
            OWLClass cl = df.getOWLClass(IRI.create("http://ebi.ac.uk#Pattern" + id));
            id++;
            map.put(exp, cl);
        }
        return map.get(exp);
    }
}
