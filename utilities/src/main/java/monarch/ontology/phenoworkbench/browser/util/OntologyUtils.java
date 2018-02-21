package monarch.ontology.phenoworkbench.browser.util;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.semanticweb.owlapi.util.OWLObjectTransformer;

import java.util.*;

public class OntologyUtils {

    private static OWLDataFactory df = OWLManager.getOWLDataFactory();

    public static void p(Object label) {
        System.out.println(label);
    }

    public static Set<String> getLabels(OWLEntity c, OWLOntology o) {
        Set<String> labels = new HashSet<>();
        for(OWLOntology i:o.getImportsClosure()) {
            for (OWLAnnotation a : EntitySearcher.getAnnotations(c, i, df.getRDFSLabel())) {
                OWLAnnotationValue value = a.getValue();
                if (value instanceof OWLLiteral) {
                    String val = ((OWLLiteral) value).getLiteral();
                    labels.add(val);
                }
            }
        }
        return labels;
    }

    public static TreeMap<Object, Integer> sortMapByValue(HashMap<? extends Object, Integer> map) {
        Comparator<Object> comparator = new OWLEntityMapSorter(map);
        //TreeMap is a map sorted by its keys.
        //The comparator is used to sort the TreeMap by keys.
        TreeMap<Object, Integer> result = new TreeMap<>(comparator);
        result.putAll(map);
        return result;
    }

    public static Set<OWLClass> getSubClasses(Set<OWLClass> roots, OWLReasoner r) {
        Set<OWLClass> subs = new HashSet<>();
        for(OWLEntity e:roots) {
            if(e instanceof OWLClass) {
                subs.addAll(r.getSubClasses((OWLClass)e,false).getFlattened());
            }
        }
        return subs;
    }

    public static String getRandomLabelIfAny(OWLEntity e,OWLOntology o) {
        for(String l:getLabels(e,o)) {
            return l;
        }
        return e.getIRI().getRemainder().or(e.getIRI().toString());
    }


    public static void replaceAllClassExpressions(OWLOntology o, Map<OWLClassExpression, OWLClassExpression> replacements) {
        OWLObjectTransformer<OWLClassExpression> replacer = new OWLObjectTransformer<>((x) -> true,
                (input) -> {
                    OWLClassExpression l = replacements.get(input);
                    if (l == null) {
                        return input;
                    }
                    return l;
                }, df, OWLClassExpression.class);

        List<OWLOntologyChange> results = replacer.change(o);
        o.getOWLOntologyManager().applyChanges(results);
    }


}
