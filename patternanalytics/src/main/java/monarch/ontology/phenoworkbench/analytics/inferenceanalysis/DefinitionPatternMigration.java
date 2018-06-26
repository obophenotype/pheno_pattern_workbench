package monarch.ontology.phenoworkbench.analytics.inferenceanalysis;

import monarch.ontology.phenoworkbench.analytics.pattern.generation.DefinedClass;
import monarch.ontology.phenoworkbench.analytics.pattern.generation.PatternGenerator;

import monarch.ontology.phenoworkbench.util.*;
import org.apache.commons.io.FileUtils;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.OWLXMLDocumentFormat;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import java.io.File;
import java.io.FileOutputStream;
import java.util.*;
import java.util.stream.Collectors;


public class DefinitionPatternMigration extends PhenoAnalysisRunner {

    private final Map<String, DefinitionSet> basicDefinitions = new HashMap<>();
    private final Map<String, Set<OWLClass>> phenotypes = new HashMap<>();
    private final File out;
    private final OWLDataFactory df = OWLManager.getOWLDataFactory();
    private final OWLClass phenotypeClass = df.getOWLClass(IRI.create("http://purl.obolibrary.org/obo/UPHENO_0001001"));
    private final Set<OWLClass> phenotypeClassesAllIncludingExcluded = new HashSet<>();

    public DefinitionPatternMigration(Set<OntologyEntry> pd, File out) {
        super(pd);
        this.out = out;
    }

    public static void main(String[] args) {
        File out = new File(args[0]);
        boolean imports = args[1].contains("i");

        ClassLoader classLoader = DefinitionPatternMigration.class.getClassLoader();
        File os = new File(classLoader.getResource("ontologies").getFile());
        File roots = new File(classLoader.getResource("phenotypeclasses").getFile());
        OntologyRegistry phenotypeontologies = new OntologyRegistry(os, roots);

        Set<OntologyEntry> entries = new HashSet<>(); //phenotypeontologies.getOntologies()
        entries.add(new OntologyEntry("hp", "http://purl.obolibrary.org/obo/hp.owl"));
        DefinitionPatternMigration p = new DefinitionPatternMigration(entries, out);
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
                Set<OWLClass> phenotypeClasses = new HashSet<>(r.getSubClasses(phenotypeClass,false).getFlattened());
                phenotypeClasses.add(phenotypeClass);
                phenotypeClassesAllIncludingExcluded.addAll(phenotypeClasses);

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
                TowardsDefinitionTransformation towardsTransform = new TowardsDefinitionTransformation(getRenderManager(),patoterms,phenotypes.get(oid));
                PhenotypeDefinitionTransformer hasPartTransform = new HasPartToPhenotypeDefinitionTransformer(getRenderManager(),patoterms,phenotypes.get(oid));
                PhenotypeDefinitionTransformer hasQualityTransform = new HasQualityDefinitionTransformation(getRenderManager(),patoterms,phenotypes.get(oid));
                PhenotypeDefinitionTransformer inheresinPartofTransform = new InheresInPartOfDefinitionTransformation(getRenderManager(),patoterms,phenotypes.get(oid));

                String sub = "p";
                log(oid, sub);
                DefinitionSet base = basicDefinitions.get(oid);
                System.out.println(base.getDefinedClassDefinitions().size());
                log("Transforming has part and has quality..", sub);
                // has quality transform should always be performed last
                DefinitionSet hasPart = hasPartTransform.get(base);
                DefinitionSet hasQuality = hasQualityTransform.get(base);
                DefinitionSet towards = towardsTransform.get(base);
                //DefinitionSet inheresInPartOf = inheresinPartofTransform.get(base);

                DefinitionSet hasPartHasQuality = hasQualityTransform.get(hasPart);
                DefinitionSet hasPartTowards = hasPartTransform.get(towards);
                DefinitionSet hasPartHasQualityTowards = hasQualityTransform.get(hasPartTowards);


                log("Create O..", sub);
                Set<OWLAxiom> createNewAxioms = createDefinitionAxioms(towardsTransform.getNewEntitiesCreated());
                OWLOntology o = OWLManager.createOWLOntologyManager().createOntology(getO().getAxioms(oid));
                o.getOWLOntologyManager().addAxioms(o,createNewAxioms);
                stripDefinitions(o); //remove ALL definitions, including the ones that were blacklisted previously.
                o.getOWLOntologyManager().saveOntology(o, new OWLXMLDocumentFormat(), new FileOutputStream(new File("/ws/phenotyp_ontology_survey/hp_patternvariants/bare_"+oid+".owl")));

                log("Comparing..", sub);
                OntologyCompare p1 = new DefinitionImpactOntologyCompare(base, towards, new ElkReasonerFactory(), o, "base","towards",oid);
               // OntologyCompare p6 = new DefinitionImpactOntologyCompare(base, inheresInPartOf, new ElkReasonerFactory(), o, "base","inheresinpartof",oid);
                OntologyCompare p2 = new DefinitionImpactOntologyCompare(base, hasQuality, new ElkReasonerFactory(), o, "base","hasquality",oid);
                OntologyCompare p3 = new DefinitionImpactOntologyCompare(base, hasPart, new ElkReasonerFactory(), o,  "base","haspart",oid);
                OntologyCompare p4 = new DefinitionImpactOntologyCompare(hasPart, hasPartTowards, new ElkReasonerFactory(), o,  "haspart","hasparttowards",oid);
                OntologyCompare p5 = new DefinitionImpactOntologyCompare(hasPartHasQuality, hasPartHasQualityTowards, new ElkReasonerFactory(), o,  "hasparthasquality","hasparthasqualitytowards",oid);
                OntologyCompare p6 = new DefinitionImpactOntologyCompare(hasQuality, hasPartHasQuality, new ElkReasonerFactory(), o,  "hasquality","hasparthasquality",oid);
                log("Export..", sub);
                FileUtils.writeLines(new File(out, "axiom_diff_base_towards_" + oid + ".csv"), p1.getCsv());
                FileUtils.writeLines(new File(out, "axiom_diff_base_hasquality_" + oid + ".csv"), p2.getCsv());
                FileUtils.writeLines(new File(out, "axiom_diff_base_haspart_" + oid + ".csv"), p3.getCsv());
                FileUtils.writeLines(new File(out, "axiom_diff_haspart_hasparttowards_" + oid + ".csv"), p4.getCsv());
                FileUtils.writeLines(new File(out, "axiom_diff_hasparthasquality_hasparthasqualitytowards_" + oid + ".csv"), p5.getCsv());
                FileUtils.writeLines(new File(out, "axiom_diff_hasquality_hasparthasquality_" + oid + ".csv"), p6.getCsv());
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
                    if (phenotypeClassesAllIncludingExcluded.contains(ce)) {
                        axioms.add(ax);
                        break;
                    }
                }
            }
        }
        o.getOWLOntologyManager().removeAxioms(o,axioms);
    }

    private Set<OWLAxiom> createDefinitionAxioms(Map<OWLClass, OWLObjectProperty> newEntitiesCreated) {
        Set<OWLAxiom> axioms = new HashSet<>();
        OWLObjectProperty inheresinpartof = df.getOWLObjectProperty(IRI.create("http://purl.obolibrary.org/obo/RO_0002314"));
        OWLObjectProperty towards = df.getOWLObjectProperty(IRI.create("http://purl.obolibrary.org/obo/RO_0002503"));
        OWLObjectProperty qrel_base = df.getOWLObjectProperty(IRI.create("http://purl.obolibrary.org/obo/qrel_base"));
        for (OWLClass e : newEntitiesCreated.keySet()) {
            OWLObjectProperty p = newEntitiesCreated.get(e);
            if (p.getIRI().toString().contains("qrel")) {
                axioms.add(df.getOWLSubClassOfAxiom(df.getOWLObjectSomeValuesFrom(inheresinpartof,df.getOWLObjectSomeValuesFrom(p,df.getOWLThing())),e));
                axioms.add(df.getOWLSubObjectPropertyOfAxiom(p,qrel_base));
            }
        }
        List<OWLObjectProperty> chain = new ArrayList<>();
        chain.add(inheresinpartof);
        chain.add(qrel_base);
        axioms.add(df.getOWLSubPropertyChainOfAxiom(chain,towards));
        return axioms;
    }
}
