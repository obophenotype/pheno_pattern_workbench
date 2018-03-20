package monarch.ontology.phenoworkbench.analytics.pattern.report;

import monarch.ontology.phenoworkbench.analytics.pattern.generation.PatternClass;
import monarch.ontology.phenoworkbench.analytics.pattern.impact.OntologyClassImpact;
import monarch.ontology.phenoworkbench.util.BranchLoader;
import monarch.ontology.phenoworkbench.analytics.pattern.generation.PatternGenerator;
import monarch.ontology.phenoworkbench.analytics.pattern.impact.DefinedClassImpactCalculator;
import monarch.ontology.phenoworkbench.analytics.pattern.generation.PatternOntologyCreator;
import monarch.ontology.phenoworkbench.analytics.pattern.generation.DefinedClass;
import monarch.ontology.phenoworkbench.util.*;
import monarch.ontology.phenoworkbench.util.Timer;
import org.apache.commons.io.FileUtils;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;


public class PatternExtractor {

    private final int SAMPLESIZE;
    private final UberOntology o;
    private final OntologyDebugReport report = new OntologyDebugReport();
    private final PatternOntologyCreator patternCreator = new PatternOntologyCreator();
    private BranchLoader branches = null;

    private Map<OWLObjectProperty, Map<OWLAxiom, Set<String>>> relationsToDefinitions = new HashMap<>();
    private Map<OWLClass, Map<OWLAxiom, Set<String>>> classesToDefinitions = new HashMap<>();
    private Map<OWLEntity, Map<String, Integer>> entityCounts = new HashMap<>();
    private Map<OWLAxiom, Set<String>> definitions = new HashMap<>();

    private final int REPORT_MIN_IDSC = 1000;

    //private final File pd;
    private final File branchfile;
    //private final Imports imports;
    private final boolean addsubclasses;

    public PatternExtractor(Set<String> pd, File branchfile, boolean imports, boolean addsubclasses, int samplesize) {
        this.branchfile = branchfile;
        Imports i = imports ? Imports.INCLUDED : Imports.EXCLUDED;
        o = new UberOntology(i,pd);
        this.addsubclasses = addsubclasses;
        this.SAMPLESIZE = samplesize;

    }

    public static void main(String[] args) throws IOException {
        File pd = new File(args[0]);
        File branches = new File(args[1]);
        boolean imports = args[2].contains("i");
        boolean addsubclasses = args[2].contains("s");
        File outdir = new File(args[3]);
        int samplesize = Integer.valueOf(args[4]);

        PatternExtractor p = new PatternExtractor(new HashSet<>(FileUtils.readLines(pd,"UTF-8")), branches, imports, addsubclasses, samplesize);
        p.run();
        p.printResults(outdir);
    }

    public void run() {
        OntologyUtils.p("Process Ontologies" + Timer.getSecondsElapsed("PatternExtractor::run"));
        OWLOntology uberontology = o.createNewUberOntology();
        this.branches = new BranchLoader(branchfile,uberontology);
        OntologyUtils.p("Create Reasoner" +Timer.getSecondsElapsed("PatternExtractor::run"));
        Reasoner rs = new Reasoner(uberontology);
        OWLReasoner r = rs.getOWLReasoner();
        OntologyUtils.p("Precompute unsatisfiable classes" +Timer.getSecondsElapsed("PatternExtractor::run"));
        branches.addUnsatisfiableClasses(r);
        if (addsubclasses) {
            OntologyUtils.p("Add subclasses to branches" +Timer.getSecondsElapsed("PatternExtractor::run"));
            branches.addSubclassesToBranches(r);
        }
        OntologyUtils.p("Process axioms" +Timer.getSecondsElapsed("PatternExtractor::run"));
        processAxioms();
    }

    private void processAxioms() {
        o.getAllAxioms().forEach(this::processAxiom);
    }

    private void processAxiom(OWLAxiom ax) {
        if (isRelevantAxiom(ax)) {
            indexDefinitions(ax);
            indexPropertiesToDefinitions(ax);
            indexCountEntities(ax);
            indexClassesToDefinitions(ax);
        }
    }

    private boolean isRelevantAxiom(OWLAxiom ax) {
        if (!(ax instanceof OWLEquivalentClassesAxiom)) {
            return false;
        }
        for (OWLClass c : branches.getAllClassesInBranches()) {
            if (ax.containsEntityInSignature(c)) {
                return true;
            }
        }
        return false;
    }

    private void indexDefinitions(OWLAxiom ax) {
        if (!definitions.containsKey(ax)) {
            definitions.put(ax, new HashSet<>());
        }
        definitions.get(ax).addAll(o.getAllOntologiesAcrossAxioms(ax));
    }

    private void indexClassesToDefinitions(OWLAxiom ax) {
        for (OWLClass c : ax.getClassesInSignature()) {
            if (!classesToDefinitions.containsKey(c)) {
                classesToDefinitions.put(c, new HashMap<>());
            }
            for (String oid : o.getAllOntologiesAcrossAxioms(ax)) {
                if (!classesToDefinitions.get(c).containsKey(ax)) {
                    classesToDefinitions.get(c).put(ax, new HashSet<>());
                }
                classesToDefinitions.get(c).get(ax).add(oid);
            }
        }
    }

    private void indexCountEntities(OWLAxiom ax) {
        for (OWLEntity e : ax.getSignature()) {
            if (!entityCounts.containsKey(e)) {
                entityCounts.put(e, new HashMap<>());
            }
            for (String oid : o.getAllOntologiesAcrossAxioms(ax)) {
                if (!entityCounts.get(e).containsKey(oid)) {
                    entityCounts.get(e).put(oid, 0);
                }
                entityCounts.get(e).put(oid, entityCounts.get(e).get(oid) + 1);
            }
        }
    }

    private void indexPropertiesToDefinitions(OWLAxiom ax) {
        for (OWLObjectProperty p : ax.getObjectPropertiesInSignature()) {
            if (!relationsToDefinitions.containsKey(p)) {
                relationsToDefinitions.put(p, new HashMap<>());
            }
            if (!relationsToDefinitions.get(p).containsKey(ax)) {
                relationsToDefinitions.get(p).put(ax, new HashSet<>());
            }
            relationsToDefinitions.get(p).get(ax).addAll(o.getAllOntologiesAcrossAxioms(ax));
        }
    }

    /*
    Prining report
     */
    public void printResults(File out) {
        report.addLine("# DefinedClass Analysis Results");
        report.addLine("* Ontology ids: ");
        Map<String,String> map_oid_name = o.getMap_oid_name();
        for(String oid:map_oid_name.keySet()) {
            report.addLine("  * "+oid+":"+map_oid_name.get(oid));
        }
        OntologyUtils.p("Print Definition OntologyClassImpact" +Timer.getSecondsElapsed("PatternExtractor::run"));
        printDefinitionImpact(out);
        OntologyUtils.p("Print Relation to Definition" +Timer.getSecondsElapsed("PatternExtractor::run"));
        printRelationsToDefinition();
        OntologyUtils.p("Print Classes to Definition" +Timer.getSecondsElapsed("PatternExtractor::run"));
        printClassesToDefinition();
        OntologyUtils.p("Print Entity Counts" +Timer.getSecondsElapsed("PatternExtractor::run"));
        printEntityCounts(out);
        OntologyUtils.p("Print Definition Analysis" +Timer.getSecondsElapsed("PatternExtractor::run"));
        printDefinitionAnalysis();

        try {
            FileUtils.writeLines(new File(out, "report_patternextractor.md"), report.getLines());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void printDefinitionImpact(File out) {
        report.addLine("");
        report.addLine("## Definitions OntologyClassImpact");
        report.addLine("Report only considers patterns with at least 100 indirect instances.");
        report.addLine("See generated dataset for complete view of the data.");
        report.addEmptyLine();
        report.addEmptyLine();

        try {
            OntologyUtils.p("##### Print definition impact");

            OntologyUtils.p("getUberOntology() " +Timer.getSecondsElapsed("PatternExtractor::run"));
            OWLOntology o_union = o.createNewUberOntology();

            OntologyUtils.p("createReasoner()" +Timer.getSecondsElapsed("PatternExtractor::run"));
            Reasoner rs = new Reasoner(o_union);
            OWLReasoner r = rs.getOWLReasoner();
            r.precomputeInferences(InferenceType.CLASS_HIERARCHY);

            OntologyUtils.p("generatePatternsFromDefinitios()" +Timer.getSecondsElapsed("PatternExtractor::run"));
            PatternGenerator patternGen = new PatternGenerator(o.getRender());
            Set<PatternClass> generatedDefinedClasses = patternGen.generateDefinitionPatterns(definitions.keySet(), r,SAMPLESIZE);
            //Map<OWLClass, OWLClassExpression> namedPatternsSKELETON = generateNamedClassesForExpressions(patternGen.generateThingPatterns(getDefinitionAxioms()));

            OntologyUtils.p("createEmptyOntologies()" +Timer.getSecondsElapsed("PatternExtractor::run"));
            String base = BaseIRIs.EBIBASE.replaceAll("#", "");
            OWLOntology o_all_analysis = OWLManager.createOWLOntologyManager().createOntology(IRI.create( base+ "/pattern_union_definitions_analysis.owl"));
            OWLOntology o_definitions = OWLManager.createOWLOntologyManager().createOntology(IRI.create(base + "/pattern_definitions.owl"));
            OWLOntology o_definition_abox = OWLManager.createOWLOntologyManager().createOntology(IRI.create(base + "/pattern_definition_abox.owl"));

            OntologyUtils.p("addPatterns()" +Timer.getSecondsElapsed("PatternExtractor::run"));

            Map<OWLClass, OWLNamedIndividual> classIndividualMap = patternCreator.addPatternsToOntology(generatedDefinedClasses, o_definitions);
            OntologyUtils.p("addPatternsSkeleton()" +Timer.getSecondsElapsed("PatternExtractor::run"));

            OntologyUtils.p("addPatternsToOntology()" +Timer.getSecondsElapsed("PatternExtractor::run"));
            o_all_analysis.getOWLOntologyManager().addAxioms(o_all_analysis, o_definitions.getAxioms());
            o_all_analysis.getOWLOntologyManager().addAxioms(o_all_analysis, o_union.getAxioms());

            OntologyUtils.p("recreateReasonerForOntologyWithPatterns()" +Timer.getSecondsElapsed("PatternExtractor::run"));
            rs = new Reasoner(o_all_analysis);
            r = rs.getOWLReasoner();

            OntologyUtils.p("precompute()" +Timer.getSecondsElapsed("PatternExtractor::run"));
            r.precomputeInferences(InferenceType.CLASS_HIERARCHY);

            OntologyUtils.p("createInferredHierarchy()" +Timer.getSecondsElapsed("PatternExtractor::run"));

            OWLOntology o_all_inferred_hierarchy = OWLManager.createOWLOntologyManager().createOntology(IRI.create(base + "/pattern_inferred_ch.owl"));
            rs.createInferredHierarchy(o_all_inferred_hierarchy);

            OntologyUtils.p("exportImpact(): Patterncount: " + generatedDefinedClasses.size() +Timer.getSecondsElapsed("PatternExtractor::run"));
            List<Map<String, String>> csv = new ArrayList<>();
            exportImpact(generatedDefinedClasses, o_definitions, o_definition_abox, classIndividualMap,csv);

            OntologyUtils.p("addAnnotations()" +Timer.getSecondsElapsed("PatternExtractor::run"));
            patternCreator.copyAnnotationAssertionAxioms(o_all_analysis, o_definitions);

            OntologyUtils.p("addDefinedClassesForImpact()" +Timer.getSecondsElapsed("PatternExtractor::run"));
            patternCreator.addDefinedClassesForImpact(o_definition_abox);

            OntologyUtils.p("saveOntologies()" +Timer.getSecondsElapsed("PatternExtractor::run"));

            File out_all_out = new File(out, "pattern_all.owl");
            File outodef_all = new File(out, "pattern_defs.owl");
            File outodef_inferred = new File(out, "pattern_all_inferred.owl");
            File outodef_o_definition_abox = new File(out, "pattern_defs_abox.owl");

            File metadata = new File(out, "pattern_metadata_all.csv");

            OWLOntology o_all_out = OWLManager.createOWLOntologyManager().createOntology(IRI.create(base + "/pattern_union_definitions.owl"));
            o_all_out.getOWLOntologyManager().addAxioms(o_all_out,o_definitions.getAxioms());
            o_all_out.getOWLOntologyManager().addAxioms(o_all_out,o_union.getAxioms());
            //o_all_out.getOWLOntologyManager().addAxioms(o_all_out,o_all_inferred_hierarchy.getAxioms());

            OntologyUtils.p("saveOntologies:o_all_out()" +Timer.getSecondsElapsed("PatternExtractor::run"));
            o_all_out.getOWLOntologyManager().saveOntology(o_all_out, new RDFXMLDocumentFormat(), new FileOutputStream(out_all_out));
            OntologyUtils.p("saveOntologies:o_o_definitions()" +Timer.getSecondsElapsed("PatternExtractor::run"));
            o_definitions.getOWLOntologyManager().saveOntology(o_definitions, new RDFXMLDocumentFormat(), new FileOutputStream(outodef_all));
            OntologyUtils.p("saveOntologies:o_o_all_inferred_hierarchy()" +Timer.getSecondsElapsed("PatternExtractor::run"));
            o_all_inferred_hierarchy.getOWLOntologyManager().saveOntology(o_all_inferred_hierarchy, new RDFXMLDocumentFormat(), new FileOutputStream(outodef_inferred));

            OntologyUtils.p("saveOntologies:o_definition_abox()" +Timer.getSecondsElapsed("PatternExtractor::run"));
            o_definition_abox.getOWLOntologyManager().saveOntology(o_definition_abox, new RDFXMLDocumentFormat(), new FileOutputStream(outodef_o_definition_abox));
            OntologyUtils.p("saveOntologies:csv_save()" +Timer.getSecondsElapsed("PatternExtractor::run"));
            Export.writeCSV(csv, metadata);
            OntologyUtils.p("saveOntologies:done()" +Timer.getSecondsElapsed("PatternExtractor::run"));

        } catch (OWLOntologyCreationException | OWLOntologyStorageException | FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void exportImpact(Set<PatternClass> namedDefinedClasses, OWLOntology o_definitions, OWLOntology o_definition_abox, Map<OWLClass, OWLNamedIndividual> classIndividualMap, List<Map<String, String>> csv) {
        List<OWLOntologyChange> aboxchanges = new ArrayList<>();
        List<OWLOntologyChange> defchanges = new ArrayList<>();

        final DefinedClassImpactCalculator definedClassImpactCalculator = new DefinedClassImpactCalculator(o,branches.getUnsatisfiableClasses(),branches.getAllClassesInBranches());
        definedClassImpactCalculator.precomputeImpactMap(namedDefinedClasses);
        for (DefinedClass definedClass : namedDefinedClasses) {
            OWLClass patternName = definedClass.getOWLClass();
            OntologyClassImpact impact = definedClassImpactCalculator.getImpact(definedClass).get();
            System.out.println(impact);
            String def_mcr_md = o.getRender().renderForMarkdown(definedClass.getDefiniton());
            patternCreator.addImpactAxiomsForABox(patternName, classIndividualMap.get(patternName), o_definition_abox, aboxchanges,  def_mcr_md, impact);
            patternCreator.addImpactAxiomsForTBox(patternName,o_definitions,defchanges, impact);
            addImpactCSVRecord(csv, patternName, def_mcr_md, impact);
            writeImpactReport(patternName, impact,def_mcr_md);
        }

        o_definition_abox.getOWLOntologyManager().applyChanges(aboxchanges);
        o_definitions.getOWLOntologyManager().applyChanges(defchanges);
    }

    private void writeImpactReport(OWLClass patternName, OntologyClassImpact impact, String patternStringForMarkdown) {
        if (impact.getIndirectImpact() > REPORT_MIN_IDSC) {
            report.addLine("* " + patternName.getIRI());
            report.addLine("  * " + patternStringForMarkdown);
            report.addLine("  * " + "Direct SubClasses overall: " + impact.getDirectImpact());
            report.addLine("  * " + "Indirect SubClasses overall: " + impact.getIndirectImpact());
            Map<String,Integer> m_dsc_byo = impact.getDirectImpactByO();
            for (String oid : m_dsc_byo.keySet()) {
                report.addLine("  * " + "Direct SubClasses " + oid + ": " + m_dsc_byo.get(oid));
            }
            Map<String,Integer> m_idsc_byo = impact.getIndirectImpactByO();
            for (String oid : m_idsc_byo.keySet()) {
                report.addLine("  * " + "Indirect SubClasses " + oid + ": " + m_idsc_byo.get(oid));
            }
            report.addEmptyLine();
        }
    }

    private void addImpactCSVRecord(List<Map<String, String>> data, OWLClass patternName, String def_mcr_syntax, OntologyClassImpact impact) {
        Map<String, String> rec = new HashMap<>();
        rec.put("definition", def_mcr_syntax);
        rec.put("definition_name", patternName.getIRI().toString());
        rec.put("m_dsc_union", impact.getDirectImpact() + "");
        Map<String,Integer> m_dsc_byo = impact.getDirectImpactByO();
        for (String oid : m_dsc_byo.keySet()) {
            rec.put("m_dsc_" + oid, m_dsc_byo.get(oid) + "");
        }
        rec.put("m_idsc_union", impact.getIndirectImpact() + "");
        Map<String,Integer> m_idsc_byo = impact.getDirectImpactByO();
        for (String oid : m_idsc_byo.keySet()) {
            rec.put("m_idsc_" + oid, m_idsc_byo.get(oid) + "");
        }
        data.add(rec);
    }

    private void printRelationsToDefinition() {
        report.addEmptyLine();
        report.addLine("## Prominent relations used across definitions");
        report.addEmptyLine();
        Map<String, Set<OWLEntity>> labelmapping = new HashMap<>();
        HashMap<String, Integer> countRel = new HashMap<>();
        Map<String, Set<String>> ontRels = new HashMap<>();

        prepareLabelMapping(labelmapping, ontRels);
        countDefinitionsByLabel(labelmapping, countRel);

        TreeMap<Object, Integer> sortedMap = OntologyUtils.sortMapByValue(countRel);
        for (Object o : sortedMap.keySet()) {
            String label = (String) o;

            report.addLine("");
            report.addLine("### " + label + " (label)");
            report.addLine("* Number of distinct definitions using this label: " + countRel.get(o));
            report.addLine("* Ontologies make use of this relation: " + ontRels.get(label));
            report.addLine("* Relations used for this label: " + labelmapping.get(label));

            for (OWLEntity e : labelmapping.get(label)) {
                report.addLine("  * " + e.getIRI());
                for (OWLAxiom ax : relationsToDefinitions.get(e).keySet()) {
                    report.addLine("    * " + this.o.getRender().renderForMarkdown(ax)+" "+relationsToDefinitions.get(e).get(ax));
                }
            }
        }

    }

    private void prepareLabelMapping(Map<String, Set<OWLEntity>> labelmapping, Map<String, Set<String>> ontRels) {
        for (OWLObjectProperty p : relationsToDefinitions.keySet()) {
            String label = getLabel(p);
            if (!labelmapping.containsKey(label)) {
                labelmapping.put(label, new HashSet<>());
            }
            labelmapping.get(label).add(p);
            if (!ontRels.containsKey(label)) {
                ontRels.put(label, new HashSet<>());
            }
            for (OWLAxiom ax : relationsToDefinitions.get(p).keySet()) {
                ontRels.get(label).addAll(relationsToDefinitions.get(p).get(ax));
            }

        }
    }

    private void countDefinitionsByLabel(Map<String, Set<OWLEntity>> labelmapping, HashMap<String, Integer> countRel) {
        for (String l : labelmapping.keySet()) {
            Set<OWLAxiom> axioms = new HashSet<>();
            for (OWLEntity e : labelmapping.get(l)) {
                axioms.addAll(relationsToDefinitions.get(e).keySet());
            }
            if (!countRel.containsKey(l)) {
                countRel.put(l, 0);
            }
            countRel.put(l, axioms.size());
        }
    }

    private void printClassesToDefinition() {
        report.addLine("");
        report.addLine("## Prominent classes used across definitions");
        report.addLine("* Omitting classes that only occur in one definition");
        for (OWLClass e : classesToDefinitions.keySet()) {
            Integer defs = classesToDefinitions.get(e).keySet().size();
            if (defs > 1) {
                report.addLine("");
                report.addLine("### " + getLabel(e));
                Set<String> onts = new HashSet<>();
                classesToDefinitions.get(e).keySet().forEach(ax -> onts.addAll(classesToDefinitions.get(e).get(ax)));
                report.addLine("* Ontologies make use of this relation: " + onts);
                report.addLine("* Definitions by relation:");
                report.addLine("  * " + e.getIRI());
                for (OWLAxiom ax : classesToDefinitions.get(e).keySet()) {
                    report.addLine("    * " + this.o.getRender().renderForMarkdown(ax)+" "+classesToDefinitions.get(e).get(ax));
                }
            }
        }
    }

    private void printDefinitionAnalysis() {
        report.addLine("");
        report.addLine("## Definition Analysis: Grammar and constructs");
        Map<ClassExpressionType, Integer> types = new HashMap<>();

        for (OWLAxiom ax : definitions.keySet()) {
            if (ax instanceof OWLEquivalentClassesAxiom) {
                for (OWLClassExpression e : ((OWLEquivalentClassesAxiom) ax).getClassExpressionsAsList()) {
                    countClassExpressionTypes(types, e);
                }
            }
        }

        types.keySet().forEach(t -> report.addLine("* " + t + ": " + types.get(t)));
        report.addLine("* Number of definitions across all ontologies: " + definitions.keySet().size());
    }

    private void countClassExpressionTypes(Map<ClassExpressionType, Integer> types, OWLClassExpression e) {
        for (OWLClassExpression cs : e.getNestedClassExpressions()) {
            if (!types.containsKey(cs.getClassExpressionType())) {
                types.put(cs.getClassExpressionType(), 0);
            }
            types.put(cs.getClassExpressionType(), types.get(cs.getClassExpressionType()) + 1);
        }
    }

    private void printEntityCounts(File outdir) {
        report.addLine("");
        report.addLine("## Entity counts");
        report.addLine("Omitting entities that occur only once.");
        HashMap<OWLEntity, Integer> map = new HashMap<>();
        for (OWLEntity e : entityCounts.keySet()) {
            for (String oid : entityCounts.get(e).keySet()) {
                if (!map.containsKey(e)) {
                    map.put(e, 0);
                }
                map.put(e, map.get(e) + entityCounts.get(e).get(oid));
            }
        }
        TreeMap<Object, Integer> sortedMap = OntologyUtils.sortMapByValue(map);

        List<Map<String, String>> data = new ArrayList<>();
        for (Map.Entry<Object, Integer> entry : sortedMap.entrySet()) {
            OWLEntity e = (OWLEntity) entry.getKey();
            Integer v = entry.getValue();
            if (v > 1) {
                report.addLine("* " + e.getIRI().toString() + " (" + getLabel(e) + ")");
                entityCounts.get(e).keySet().forEach(k -> report.addLine("   * " + k + ": " + entityCounts.get(e).get(k)));
            }
            entityCounts.get(e).keySet().forEach(k -> data.add(createEntityCountRecord((OWLEntity) e, k)));
        }
        Export.writeCSV(data, new File(outdir, "entitycounts.csv"));
    }

    private Map<String, String> createEntityCountRecord(OWLEntity e, String k) {
        Map<String, String> rec = new HashMap<>();
        rec.put("oid", k);
        rec.put("entity", e.getIRI().toString());
        rec.put("entity_label", getLabel(e));
        rec.put("entity_class", e.getEntityType().getName());
        rec.put("mentioned", entityCounts.get(e).get(k) + "");
        return rec;
    }


    /*
    Utility methods
     */

    private String getLabel(OWLEntity k) {
        return o.getRender().getLabel(k);
    }

    public List<String> getReportLines() {
        return report.getLines();
    }
}
