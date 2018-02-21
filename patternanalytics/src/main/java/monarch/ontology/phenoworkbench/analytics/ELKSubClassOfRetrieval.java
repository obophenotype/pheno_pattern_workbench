package monarch.ontology.phenoworkbench.analytics;

public class ELKSubClassOfRetrieval {
    public static void main(String[] args) {
        /*OWLReasoner r = new ElkReasonerFactory().createReasoner(o);
        r.precomputeInferences(InferenceType.CLASS_HIERARCHY);
        List<OWLClass> classes = new ArrayList<>(); // <-- CONTAINS MANY DUPLICATES

        // V1:
        for(OWLClass c:classes) {
            System.out.println(r.getSubClasses(c,false));
        }

        // V2:
        Map<OWLClass,Set<OWLClass>> makeshiftcache = new HashMap<>();
        for(OWLClass c:classes) {
            if(!makeshiftcache.containsKey(c)) {
                makeshiftcache.put(c,r.getSubClasses(c,false).getFlattened());
            }
            System.out.println(makeshiftcache.get(c));
        }
        */
    }
}
