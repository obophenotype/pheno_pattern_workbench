package monarch.ontology.phenoworkbench.analytics.inferenceanalysis;

import monarch.ontology.phenoworkbench.analytics.pattern.generation.PatternGenerator;
import monarch.ontology.phenoworkbench.util.DefinedClass;
import monarch.ontology.phenoworkbench.util.OntologyEntry;
import monarch.ontology.phenoworkbench.util.OntologyRegistry;
import monarch.ontology.phenoworkbench.util.PhenoAnalysisRunner;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;


public class MultiInheritanceAnalysis extends PhenoAnalysisRunner {

    private final Map<String, DefinitionSet> basicDefinitions = new HashMap<>();
    private final Map<String, Set<OWLClass>> phenotypes = new HashMap<>();

    public SimpleDefinitionImpactOntologyCompare getComparison() {
        return p1;
    }

    private  SimpleDefinitionImpactOntologyCompare p1;
    private final OWLDataFactory df = OWLManager.getOWLDataFactory();
    private final Set<OWLClass> phenotypeClasses = new HashSet<>();
    private final boolean addSubclasses;

    public MultiInheritanceAnalysis(Set<OntologyEntry> pd, Set<OWLClass> phenotypeClasses, boolean addSubclasses) {
        super(pd);
        this.phenotypeClasses.addAll(phenotypeClasses);
        this.addSubclasses = addSubclasses;
    }

    public static void main(String[] args) {
        boolean imports = true;

        ClassLoader classLoader = MultiInheritanceAnalysis.class.getClassLoader();
        File os = new File(classLoader.getResource("ontologies").getFile());
        File roots = new File(classLoader.getResource("phenotypeclasses").getFile());
        //OntologyRegistry phenotypeontologies = new OntologyRegistry(os, roots);

        Set<OntologyEntry> entries = new HashSet<>(); //phenotypeontologies.getOntologies()
        OntologyEntry oe1 = new OntologyEntry("hp", "http://purl.obolibrary.org/obo/hp.owl");
        OntologyEntry oe2 = new OntologyEntry("mp", "http://purl.obolibrary.org/obo/mp.owl");
        OntologyEntry oe3 = new OntologyEntry("xpo", "http://purl.obolibrary.org/obo/xpo.owl");
        OntologyEntry oe4 = new OntologyEntry("go", "http://purl.obolibrary.org/obo/go.owl");
        oe1.addRootClassesOfInterest(Collections.singleton("http://purl.obolibrary.org/obo/HP_0000001"));
        oe2.addRootClassesOfInterest(Collections.singleton("http://purl.obolibrary.org/obo/MP_0000001"));
        oe3.addRootClassesOfInterest(Collections.singleton("http://purl.obolibrary.org/obo/XPO_00000000"));
        oe4.addRootClassesOfInterest(Collections.singleton("http://purl.obolibrary.org/obo/GO_0008150"));
        entries.add(oe1);
        entries.add(oe2);
        entries.add(oe3);
        entries.add(oe4);
        MultiInheritanceAnalysis p = new MultiInheritanceAnalysis(entries,new HashSet<>(),true);
        p.setImports(imports ? Imports.INCLUDED : Imports.EXCLUDED);
        p.runAnalysis();
    }


    @Override
    public void runAnalysis() {
        String process = "MultiInheritanceAnalysis::runAnalysis()";
        Map<String,Map<Integer,Integer>> multiInheritanceHistogramm = new HashMap<>();
       try {
            for (OntologyEntry oid : getO().getOntologyEntries()) {
                Map<Integer,Integer> counts = new HashMap<>();
                OWLOntology o = OWLManager.createOWLOntologyManager().createOntology(getO().getAxioms(oid.getOid()));
                OWLReasoner r = new ElkReasonerFactory().createReasoner(o);
                Set<OWLClass> subsRoot = new HashSet<>();
                for(String root:oid.getRoots())  {
                    OWLClass cl_rool = df.getOWLClass(IRI.create(root));
                    subsRoot.add(cl_rool);
                    subsRoot.addAll(r.getSubClasses(cl_rool,false).getFlattened());
                }
                subsRoot.remove(df.getOWLNothing());
                subsRoot.remove(df.getOWLThing());
                for(OWLClass s:subsRoot) {
                    Integer superClassesCount = r.getSuperClasses(s,true).getFlattened().size();
                    if(!counts.containsKey(superClassesCount)) {
                        counts.put(superClassesCount,0);
                    }
                    counts.put(superClassesCount,counts.get(superClassesCount)+1);
                }
                multiInheritanceHistogramm.put(oid.getOid(),counts);
            }

            for(String oid:multiInheritanceHistogramm.keySet()) {
                System.out.println(oid);
                List<Integer> keyset = new ArrayList<>(multiInheritanceHistogramm.get(oid).keySet());
                Collections.sort(keyset);
                for(Integer bin:keyset) {
                    System.out.println(bin+","+multiInheritanceHistogramm.get(oid).get(bin));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
