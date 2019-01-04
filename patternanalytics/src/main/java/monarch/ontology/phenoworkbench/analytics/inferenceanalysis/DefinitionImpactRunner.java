package monarch.ontology.phenoworkbench.analytics.inferenceanalysis;

import monarch.ontology.phenoworkbench.analytics.pattern.generation.PatternGenerator;
import monarch.ontology.phenoworkbench.util.DefinedClass;
import monarch.ontology.phenoworkbench.util.OntologyEntry;
import monarch.ontology.phenoworkbench.util.OntologyRegistry;
import monarch.ontology.phenoworkbench.util.PhenoAnalysisRunner;
import org.apache.commons.io.FileUtils;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;


public class DefinitionImpactRunner extends PhenoAnalysisRunner {

    private final Map<String, DefinitionSet> basicDefinitions = new HashMap<>();
    private final Map<String, Set<OWLClass>> phenotypes = new HashMap<>();

    public SimpleDefinitionImpactOntologyCompare getComparison() {
        return p1;
    }

    private  SimpleDefinitionImpactOntologyCompare p1;
    private final OWLDataFactory df = OWLManager.getOWLDataFactory();
    private final Set<OWLClass> phenotypeClasses = new HashSet<>();
    private final boolean addSubclasses;

    public DefinitionImpactRunner(Set<OntologyEntry> pd, Set<OWLClass> phenotypeClasses, boolean addSubclasses) {
        super(pd);
        this.phenotypeClasses.addAll(phenotypeClasses);
        this.addSubclasses = addSubclasses;
    }

    public static void main(String[] args) {
        File out = new File(args[0]);
        boolean imports = args[1].contains("i");

        ClassLoader classLoader = DefinitionImpactRunner.class.getClassLoader();
        File os = new File(classLoader.getResource("ontologies").getFile());
        File roots = new File(classLoader.getResource("phenotypeclasses").getFile());
        OntologyRegistry phenotypeontologies = new OntologyRegistry(os, roots);

        Set<OntologyEntry> entries = new HashSet<>(); //phenotypeontologies.getOntologies()
        entries.add(new OntologyEntry("hp", "http://purl.obolibrary.org/obo/hp.owl"));
        DefinitionImpactRunner p = new DefinitionImpactRunner(entries,new HashSet<>(),true);
        p.setImports(imports ? Imports.INCLUDED : Imports.EXCLUDED);
        p.runAnalysis();
    }


    @Override
    public void runAnalysis() {
        String process = "DefinitionPatternMigration::runAnalysis()";
        Set<OWLClass> patoterms = new HashSet<>();
        try {
            patoterms.addAll(OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(IRI.create("http://purl.obolibrary.org/obo/pato.owl")).getClassesInSignature(Imports.INCLUDED));
            patoterms.add(df.getOWLClass(IRI.create("http://purl.obolibrary.org/obo/BFO_0000019")));
            log("HACK: Including BFO:QUALITY AS PERMISSIBLE PHENOTYPE QUALITY (should NOT happend and is being fixed)");
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }
        try {
            log("Initialising pattern generator..", process);
            PatternGenerator patternGenerator = new PatternGenerator(getRenderManager());

            log("Create definition sets", process);
            for (OntologyEntry oid : getO().getOntologyEntries()) {
                DefinitionSet defs = new DefinitionSet();
                OWLReasoner r = new ElkReasonerFactory().createReasoner(OWLManager.createOWLOntologyManager().createOntology(getO().getAxioms(oid.getOid())));
                Set<DefinedClass> definedClasses = patternGenerator.extractDefinedClasses(getO().getAxioms(oid.getOid()), true);
                //Set<OWLClass> phenotypeClasses = new HashSet<>(r.getSubClasses(phenotypeClass,false).getFlattened());
                //phenotypeClasses.add(phenotypeClass);
                if(addSubclasses) {
                    Set<OWLClass> subs = new HashSet<>();
                    phenotypeClasses.forEach(c->subs.addAll(r.getSubClasses(c,false).getFlattened()));
                    phenotypeClasses.addAll(subs);
                }
                excludeEquivalentClasses(r, phenotypeClasses);

                phenotypes.put(oid.getOid(),phenotypeClasses);
                Set<DefinedClass> definedPhenotypeClasses = new HashSet<>();
                definedClasses.forEach(d->{if(phenotypeClasses.contains(d.getOWLClass())) {definedPhenotypeClasses.add(d);}});
                defs.setDefinitions(definedPhenotypeClasses);

                basicDefinitions.put(oid.getOid(), defs);


                //analyseEqualClasses(definedPhenotypeClasses);


            }

            log("Comparing..", process);

            for (String oid : basicDefinitions.keySet()) {

                String sub = "p";
                log(oid, sub);
                DefinitionSet base = basicDefinitions.get(oid);
                System.out.println(base.getDefinedClassDefinitions().size());
               log("Create O..", sub);
                OWLOntology o = OWLManager.createOWLOntologyManager().createOntology(getO().getAxioms(oid));
                stripDefinitions(o); //remove ALL definitions, including the ones that were blacklisted previously.
                log("Comparing..", sub);

                p1 = new SimpleDefinitionImpactOntologyCompare(base, o, "base","bare",oid);


            }

            log("Done..", "DefinitionPatternMigration::runAnalysis()");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void analyseEqualClasses(Set<DefinedClass> definedPhenotypeClasses) {


        Map<OWLClassExpression,Set<OWLClass>> equal_definitions = new HashMap<>();

        for(DefinedClass d:definedPhenotypeClasses) {
            if(!equal_definitions.containsKey(d.getDefiniton())) {
                equal_definitions.put(d.getDefiniton(),new HashSet<>());
            }
            equal_definitions.get(d.getDefiniton()).add(d.getOWLClass());
        }

        Map<OWLClassExpression,Set<OWLClass>> eds = equal_definitions.entrySet().stream().filter(e->e.getValue().size()>1).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        eds.entrySet().forEach(e->System.out.println("&"+getRenderManager().renderForMarkdown(e.getKey())+" "+e.getValue()));
        System.out.println(eds.size());
    }

    private void excludeEquivalentClasses(OWLReasoner r, Set<OWLClass> phenotypeClasses) {
        log("PICKING SINGLE EQUIVALENT CLASS FROM SET OF EQUIVALENT CLASSES");
        Set<Set<OWLClass>> equivalentclasses = new HashSet<>();
        Set<OWLClass> exclude = new HashSet<>();
        for(OWLClass phenoc:phenotypeClasses) {
            Node<OWLClass> eq = r.getEquivalentClasses(phenoc);
            Set<OWLClass> classes = eq.getEntities();
            if(classes.size()>1) {
                equivalentclasses.add(classes);
                exclude.addAll(classes);
                exclude.remove(eq.getRepresentativeElement());
            }
        }
        for(Set<OWLClass> classes:equivalentclasses) {
            System.out.println("-----start-----");
            classes.forEach(c->System.out.println(getRenderManager().render(c)+" ("+c+")"));
            System.out.println("---------------");
        }
        phenotypeClasses.removeAll(exclude);
    }

    private void stripDefinitions(OWLOntology o) {
        Set<OWLAxiom> axioms = new HashSet<>();
        for(OWLAxiom ax:o.getAxioms(Imports.INCLUDED)) {
            if(ax instanceof OWLEquivalentClassesAxiom) {
                OWLEquivalentClassesAxiom eq = (OWLEquivalentClassesAxiom) ax;

                for (OWLClass ce : eq.getNamedClasses()) {
                    if (phenotypeClasses.contains(ce)) {
                        axioms.add(ax);
                        break;
                    }
                }
            }
        }
        o.getOWLOntologyManager().removeAxioms(o,axioms);
    }

}
