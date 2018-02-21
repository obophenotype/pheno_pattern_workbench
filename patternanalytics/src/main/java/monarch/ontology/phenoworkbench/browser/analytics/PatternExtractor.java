package monarch.ontology.phenoworkbench.browser.analytics;

import monarch.ontology.phenoworkbench.browser.util.*;
import org.apache.commons.io.FileUtils;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.OWLEntityRenamer;
import org.semanticweb.owlapi.util.OWLObjectDuplicator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;


public class PatternExtractor {

    monarch.ontology.phenoworkbench.browser.analytics.Timer timer = new monarch.ontology.phenoworkbench.browser.analytics.Timer();
    private final String EBIBASE = "http://ebi.ac.uk#";
    private final int SAMPLESIZE;
    private OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
    private RenderManager render = new RenderManager();
    private OntologyDebugReport report = new OntologyDebugReport();
    private BranchLoader branches = null;

    private Map<OWLObjectProperty, Map<OWLAxiom, Set<String>>> relationsToDefinitions = new HashMap<>();
    private Map<OWLClass, Map<OWLAxiom, Set<String>>> classesToDefinitions = new HashMap<>();
    private Map<OWLEntity, Map<String, Integer>> entityCounts = new HashMap<>();
    private Map<OWLAxiom, Set<String>> definitions = new HashMap<>();

    private final Map<String, Set<OWLAxiom>> allAxiomsAcrossOntologies = new HashMap<>();
    private final Map<OWLAxiom, Set<String>> allOntologiesAcrossAxioms = new HashMap<>();

    private final Map<String, Set<OWLEntity>> allSignaturesAcrossOntologies = new HashMap<>();
    private final Map<OWLEntity, Set<String>> allOntologiesAcrossSignature = new HashMap<>();



    private OWLClass clpatterntop = df.getOWLClass(IRI.create(EBIBASE + "Pattern"));
    private final int REPORT_MIN_IDSC = 1000;

    private final File pd;
    private final File branchfile;
    private final Imports imports;
    private final boolean addsubclasses;

    private PatternExtractor(File pd, File branchfile, boolean imports, boolean addsubclasses, int samplesize) {
        this.pd = pd;
        this.branchfile = branchfile;
        this.imports = imports ? Imports.INCLUDED : Imports.EXCLUDED;
        this.addsubclasses = addsubclasses;
        this.SAMPLESIZE = samplesize;

    }

    public static void main(String[] args) {
        File pd = new File(args[0]);
        File branches = new File(args[1]);
        boolean imports = args[2].contains("i");
        boolean addsubclasses = args[2].contains("s");
        File outdir = new File(args[3]);
        int samplesize = Integer.valueOf(args[4]);

        PatternExtractor p = new PatternExtractor(pd, branches, imports, addsubclasses, samplesize);

        p.run();
        p.printResults(outdir);
    }

    private void run() {
        OntologyUtils.p("Process Ontologies" + timer.getTimeElapsed());
        processOntologies();
        OWLOntology uberontology = getUberOntology();
        this.branches = new BranchLoader(branchfile,uberontology);
        OntologyUtils.p("Create Reasoner" + timer.getTimeElapsed());
        Reasoner rs = new Reasoner(uberontology);
        OWLReasoner r = rs.getOWLReasoner();
        OntologyUtils.p("Precompute unsatisfiable classes" + timer.getTimeElapsed());
        branches.addUnsatisfiableClasses(r);
        if (addsubclasses) {
            OntologyUtils.p("Add subclasses to branches" + timer.getTimeElapsed());
            branches.addSubclassesToBranches(r);
        }
        OntologyUtils.p("Process axioms" + timer.getTimeElapsed());
        processAxioms();
    }



    private void processOntologies() {
        for (File ofile : pd.listFiles(new OntologyFileExtension())) {
            OntologyUtils.p(ofile.getName());
            //if(ofile.getName().endsWith("hp.owl")||ofile.getName().endsWith("nbo.owl"))
            processOntology(imports, ofile);
        }
    }

    private void processOntology(Imports imports, File ofile) {
        String oid = ofile.getName();

        try {
            OWLOntology o = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(ofile);
            Set<OWLAxiom> axioms = new HashSet<>(o.getAxioms(imports));
            Set<OWLEntity> signature = new HashSet<>(o.getSignature(imports));
            allAxiomsAcrossOntologies.put(oid, axioms);
            allSignaturesAcrossOntologies.put(oid, signature);
            for (OWLAxiom ax : axioms) {
                if (!allOntologiesAcrossAxioms.containsKey(ax)) {
                    allOntologiesAcrossAxioms.put(ax, new HashSet<>());
                }
                allOntologiesAcrossAxioms.get(ax).add(oid);
            }
            for (OWLEntity ax : signature) {
                if (!allOntologiesAcrossSignature.containsKey(ax)) {
                    allOntologiesAcrossSignature.put(ax, new HashSet<>());
                }
                allOntologiesAcrossSignature.get(ax).add(oid);
            }
            render.addLabel(o);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private void processAxioms() {
        allOntologiesAcrossAxioms.keySet().forEach(this::processAxiom);
    }

    private void processAxiom(OWLAxiom ax) {
        if (isRelevantAxiom(ax)) {
            indexDefinitions(ax);
            indexPropertiesToDefinitions(ax);
            countEntities(ax);
            indexClassesToDefinitions(ax);
        }
    }

    private void indexDefinitions(OWLAxiom ax) {
        if (!definitions.containsKey(ax)) {
            definitions.put(ax, new HashSet<>());
        }
        definitions.get(ax).addAll(allOntologiesAcrossAxioms.get(ax));
    }

    private void indexClassesToDefinitions(OWLAxiom ax) {
        for (OWLClass c : ax.getClassesInSignature()) {
            if (!classesToDefinitions.containsKey(c)) {
                classesToDefinitions.put(c, new HashMap<>());
            }
            for (String oid : allOntologiesAcrossAxioms.get(ax)) {
                if (!classesToDefinitions.get(c).containsKey(ax)) {
                    classesToDefinitions.get(c).put(ax, new HashSet<>());
                }
                classesToDefinitions.get(c).get(ax).add(oid);
            }
        }
    }

    private void countEntities(OWLAxiom ax) {
        for (OWLEntity e : ax.getSignature()) {
            if (!entityCounts.containsKey(e)) {
                entityCounts.put(e, new HashMap<>());
            }
            for (String oid : allOntologiesAcrossAxioms.get(ax)) {
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
            relationsToDefinitions.get(p).get(ax).addAll(allOntologiesAcrossAxioms.get(ax));
        }
    }

    /*
    Prining report
     */
    private void printResults(File out) {
        OntologyUtils.p("Print Definition Impact" + timer.getTimeElapsed());
        printDefinitionImpact(out);
        OntologyUtils.p("Print Relation to Definition" + timer.getTimeElapsed());
        printRelationsToDefinition();
        OntologyUtils.p("Print Classes to Definition" + timer.getTimeElapsed());
        printClassesToDefinition();
        OntologyUtils.p("Print Entity Counts" + timer.getTimeElapsed());
        printEntityCounts(out);
        OntologyUtils.p("Print Definition Analysis" + timer.getTimeElapsed());
        printDefinitionAnalysis();
        try {
            FileUtils.writeLines(new File(out, "report_patternextractor.md"), report.getLines());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void printDefinitionImpact(File out) {
        report.addLine("");
        report.addLine("# Definitions Impact");
        report.addLine("Report only considers patterns with at least 100 indirect instances.");
        report.addLine("See generated dataset for complete view of the data.");
        report.addEmptyLine();
        report.addEmptyLine();

        try {
            OntologyUtils.p("##### Print definition impact");

            OntologyUtils.p("getUberOntology() " + timer.getTimeElapsed());
            OWLOntology o_union = getUberOntology();

            OntologyUtils.p("createReasoner()" + timer.getTimeElapsed());
            Reasoner rs = new Reasoner(o_union);
            OWLReasoner r = rs.getOWLReasoner();
            r.precomputeInferences(InferenceType.CLASS_HIERARCHY);
//            OntologyUtils.p(r.getReasonerVersion());

            OntologyUtils.p("generatePatternsFromDefinitios()" + timer.getTimeElapsed());
            Map<OWLClass, OWLClassExpression> namedPatterns = generatePatternsFromDefinitions(r);
            //Map<OWLClass, OWLClassExpression> namedPatternsSKELETON = generateHighLevelDefinitions(r);

            OntologyUtils.p("createEmptyOntologies()" + timer.getTimeElapsed());
            OWLOntology o_all_analysis = OWLManager.createOWLOntologyManager().createOntology(IRI.create(EBIBASE.replaceAll("#", "") + "/pattern_union_definitions_analysis.owl"));
            OWLOntology o_all_out = OWLManager.createOWLOntologyManager().createOntology(IRI.create(EBIBASE.replaceAll("#", "") + "/pattern_union_definitions.owl"));
            OWLOntology o_definitions = OWLManager.createOWLOntologyManager().createOntology(IRI.create(EBIBASE.replaceAll("#", "") + "/pattern_definitions.owl"));
            OWLOntology o_all_inferred_hierarchy = OWLManager.createOWLOntologyManager().createOntology(IRI.create(EBIBASE.replaceAll("#", "") + "/pattern_inferred_ch.owl"));
            OWLOntology o_definition_abox = OWLManager.createOWLOntologyManager().createOntology(IRI.create(EBIBASE.replaceAll("#", "") + "/pattern_definition_abox.owl"));


            OntologyUtils.p("addPatterns()" + timer.getTimeElapsed());

            Map<OWLClass, OWLNamedIndividual> classIndividualMap = addPatternsToOntology(namedPatterns, o_definitions);
            OntologyUtils.p("addPatternsSkeleton()" + timer.getTimeElapsed());

            OntologyUtils.p("addPatternsToOntology()" + timer.getTimeElapsed());
            o_all_analysis.getOWLOntologyManager().addAxioms(o_all_analysis, o_definitions.getAxioms());
            o_all_analysis.getOWLOntologyManager().addAxioms(o_all_analysis, o_union.getAxioms());

            OntologyUtils.p("recreateReasonerForOntologyWithPatterns()" + timer.getTimeElapsed());
            rs = new Reasoner(o_all_analysis);
            r = rs.getOWLReasoner();

            OntologyUtils.p("precompute()" + timer.getTimeElapsed());
            r.precomputeInferences(InferenceType.CLASS_HIERARCHY);

            OntologyUtils.p("createInferredHierarchy()" + timer.getTimeElapsed());
            rs.createInferredHierarchy(o_all_inferred_hierarchy);

            OntologyUtils.p("computeMetrics(): Patterncount: " + namedPatterns.keySet().size() + timer.getTimeElapsed());
            List<Map<String, String>> data = new ArrayList<>();
            List<OWLOntologyChange> aboxchanges = new ArrayList<>();
            List<OWLOntologyChange> defchanges = new ArrayList<>();

            long timedis = 0;
            long timedom = 0;
            int i = 0;

            for (OWLClass patternName : namedPatterns.keySet()) {
                i++;
                if(i % 5000 == 0) {
                    OntologyUtils.p("Compute Metrics: "+timedis/1000);
                    OntologyUtils.p("Write results: "+timedom/1000);
                }
                String def_mcr_syntax = render.renderManchester(namedPatterns.get(patternName));

                long s=System.currentTimeMillis();
                Map<String, Integer> m_dsc_byo = new HashMap<>();
                int m_dsc = computeMetrics(r, patternName, m_dsc_byo, true);
                Map<String, Integer> m_idsc_byo = new HashMap<>();
                int m_idsc = computeMetrics(r, patternName, m_idsc_byo, false);
                timedis+=(System.currentTimeMillis()-s);
                long s2=System.currentTimeMillis();
                addImpactAxioms(o_definitions, o_definition_abox, classIndividualMap, aboxchanges,defchanges, patternName, def_mcr_syntax, m_dsc_byo, m_dsc, m_idsc_byo, m_idsc);
                addImpactCSVRecord(data, patternName, def_mcr_syntax, m_dsc_byo, m_dsc, m_idsc_byo, m_idsc);
                writeImpactReport(namedPatterns, patternName, m_dsc_byo, m_dsc, m_idsc_byo, m_idsc);
                timedom+=(System.currentTimeMillis()-s2);
            }

            o_definition_abox.getOWLOntologyManager().applyChanges(aboxchanges);
            o_definitions.getOWLOntologyManager().applyChanges(defchanges);

            OntologyUtils.p("addAnnotations()" + timer.getTimeElapsed());
            addLabelsToPureDefinitionOntology(o_all_analysis, o_definitions);

            OntologyUtils.p("addDefinedClassesForImpact()" + timer.getTimeElapsed());
            addDefinedClassesForImpact(o_definition_abox);

            OntologyUtils.p("saveOntologies()" + timer.getTimeElapsed());

            File out_all_out = new File(out, "pattern_all.owl");
            File outodef_all = new File(out, "pattern_defs.owl");
            File outodef_inferred = new File(out, "pattern_all_inferred.owl");
            File outodef_o_definition_abox = new File(out, "pattern_defs_abox.owl");

            File metadata = new File(out, "pattern_metadata_all.csv");

            o_all_out.getOWLOntologyManager().addAxioms(o_all_out,o_definitions.getAxioms());
            o_all_out.getOWLOntologyManager().addAxioms(o_all_out,o_union.getAxioms());
            //o_all_out.getOWLOntologyManager().addAxioms(o_all_out,o_all_inferred_hierarchy.getAxioms());

            OntologyUtils.p("saveOntologies:o_all_out()" + timer.getTimeElapsed());
            o_all_out.getOWLOntologyManager().saveOntology(o_all_out, new RDFXMLDocumentFormat(), new FileOutputStream(out_all_out));
            OntologyUtils.p("saveOntologies:o_o_definitions()" + timer.getTimeElapsed());
            o_definitions.getOWLOntologyManager().saveOntology(o_definitions, new RDFXMLDocumentFormat(), new FileOutputStream(outodef_all));
            OntologyUtils.p("saveOntologies:o_o_all_inferred_hierarchy()" + timer.getTimeElapsed());
            o_all_inferred_hierarchy.getOWLOntologyManager().saveOntology(o_all_inferred_hierarchy, new RDFXMLDocumentFormat(), new FileOutputStream(outodef_inferred));

            OntologyUtils.p("saveOntologies:o_definition_abox()" + timer.getTimeElapsed());
            o_definition_abox.getOWLOntologyManager().saveOntology(o_definition_abox, new RDFXMLDocumentFormat(), new FileOutputStream(outodef_o_definition_abox));
            OntologyUtils.p("saveOntologies:csv_save()" + timer.getTimeElapsed());
            Export.writeCSV(data, metadata);
            OntologyUtils.p("saveOntologies:done()" + timer.getTimeElapsed());

        } catch (OWLOntologyCreationException | OWLOntologyStorageException | FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void writeImpactReport(Map<OWLClass, OWLClassExpression> namedPatterns, OWLClass patternName, Map<String, Integer> m_dsc_byo, int m_dsc, Map<String, Integer> m_idsc_byo, int m_idsc) {
        if (m_idsc > REPORT_MIN_IDSC) {
            report.addLine("* " + patternName.getIRI());
            report.addLine("  * " + render.renderForMarkdown(namedPatterns.get(patternName)));
            report.addLine("  * " + "Direct SubClasses overall: " + m_dsc);
            report.addLine("  * " + "Indirect SubClasses overall: " + m_idsc);
            for (String oid : m_dsc_byo.keySet()) {
                report.addLine("  * " + "Direct SubClasses " + oid + ": " + m_dsc_byo.get(oid));
            }
            for (String oid : m_idsc_byo.keySet()) {
                report.addLine("  * " + "Indirect SubClasses " + oid + ": " + m_idsc_byo.get(oid));
            }
            report.addEmptyLine();
        }
    }

    private void addImpactCSVRecord(List<Map<String, String>> data, OWLClass patternName, String def_mcr_syntax, Map<String, Integer> m_dsc_byo, int m_dsc, Map<String, Integer> m_idsc_byo, int m_idsc) {
        Map<String, String> rec = new HashMap<>();
        rec.put("definition", def_mcr_syntax);
        rec.put("definition_name", patternName.getIRI().toString());
        rec.put("m_dsc_union", m_dsc + "");
        for (String oid : m_dsc_byo.keySet()) {
            rec.put("m_dsc_" + oid, m_dsc_byo.get(oid) + "");
        }
        rec.put("m_idsc_union", m_idsc + "");
        for (String oid : m_idsc_byo.keySet()) {
            rec.put("m_idsc_" + oid, m_idsc_byo.get(oid) + "");
        }
        data.add(rec);
    }

    private void addImpactAxioms(OWLOntology o_definitions, OWLOntology o_definition_abox, Map<OWLClass, OWLNamedIndividual> classIndividualMap, List<OWLOntologyChange> aboxchanges,List<OWLOntologyChange> defchanges, OWLClass patternName, String def_mcr_syntax, Map<String, Integer> m_dsc_byo, int m_dsc, Map<String, Integer> m_idsc_byo, int m_idsc) {
        OWLNamedIndividual i = classIndividualMap.get(patternName);
        OWLAnnotation indivLabel = df.getOWLAnnotation(df.getRDFSLabel(), df.getOWLLiteral(def_mcr_syntax.replaceAll("[^A-Za-z0-9_]", "")));
        OWLAnnotation indivDefinition = df.getOWLAnnotation(df.getRDFSComment(), df.getOWLLiteral(def_mcr_syntax));
        OWLAnnotation annoPatternType = df.getOWLAnnotation(ap_patternType, clpatterntop.getIRI());
        OWLAnnotation annoAssocPattern = df.getOWLAnnotation(ap_associatedPattern, patternName.getIRI());
        aboxchanges.add(new AddAxiom(o_definition_abox, df.getOWLAnnotationAssertionAxiom(patternName.getIRI(), annoPatternType)));
        aboxchanges.add(new AddAxiom(o_definition_abox, df.getOWLAnnotationAssertionAxiom(i.getIRI(), annoAssocPattern)));
        aboxchanges.add(new AddAxiom(o_definition_abox, df.getOWLAnnotationAssertionAxiom(i.getIRI(), indivLabel)));
        aboxchanges.add(new AddAxiom(o_definition_abox, df.getOWLAnnotationAssertionAxiom(i.getIRI(), indivDefinition)));
        addImpactIndividualAssertionsToOntology(o_definition_abox,aboxchanges, m_dsc_byo, m_dsc, m_idsc_byo, m_idsc, i);
        addImpactClassAnnotationsToOntology(o_definitions,defchanges, m_dsc_byo, m_dsc, m_idsc_byo, m_idsc,patternName);
    }




    private void addLabelsToPureDefinitionOntology(OWLOntology o, OWLOntology odef) {
        for (OWLEntity e : odef.getSignature()) {
            Set<OWLAnnotationAssertionAxiom> axioms = new HashSet<>(o.getAnnotationAssertionAxioms(e.getIRI()));
            for (OWLAnnotationAssertionAxiom ax : axioms) {
                odef.getOWLOntologyManager().applyChange(new AddAxiom(odef, ax));
            }
        }
    }

    private void addDefinedClassesForImpact(OWLOntology odef) {
        String iriPatternDSC = EBIBASE + "patternDSC";
        String iriPatternIDSC = EBIBASE + "patternIDSC";

        OWLOntologyManager m = odef.getOWLOntologyManager();
        OWLClass impact = df.getOWLClass(IRI.create(EBIBASE + "Impact"));
        OWLClass highimpactalldsc = df.getOWLClass(IRI.create(EBIBASE + "HighImpactDSCAll"));
        OWLClass moderateimpactalldsc = df.getOWLClass(IRI.create(EBIBASE + "ModerateImpactDSCAll"));
        OWLClass lowimpactalldsc = df.getOWLClass(IRI.create(EBIBASE + "LowImpactDSCAll"));
        OWLClass highimpactallidsc = df.getOWLClass(IRI.create(EBIBASE + "HighImpactIDSCAll"));
        OWLClass moderateimpactallidsc = df.getOWLClass(IRI.create(EBIBASE + "ModerateImpactIDSCAll"));
        OWLClass lowimpactallidsc = df.getOWLClass(IRI.create(EBIBASE + "LowImpactIDSCAll"));
        OWLClass lowimpactshared = df.getOWLClass(IRI.create(EBIBASE + "LowImpactShared"));
        OWLClass moderateimpactshared = df.getOWLClass(IRI.create(EBIBASE + "ModerateImpactShared"));
        OWLClass highimpactshared = df.getOWLClass(IRI.create(EBIBASE + "HighImpactShared"));
        m.addAxiom(odef, df.getOWLSubClassOfAxiom(highimpactalldsc, impact));
        m.addAxiom(odef, df.getOWLSubClassOfAxiom(moderateimpactalldsc, impact));
        m.addAxiom(odef, df.getOWLSubClassOfAxiom(lowimpactalldsc, impact));
        m.addAxiom(odef, df.getOWLSubClassOfAxiom(highimpactallidsc, impact));
        m.addAxiom(odef, df.getOWLSubClassOfAxiom(moderateimpactallidsc, impact));
        m.addAxiom(odef, df.getOWLSubClassOfAxiom(lowimpactallidsc, impact));
        m.addAxiom(odef, df.getOWLSubClassOfAxiom(lowimpactshared, impact));
        m.addAxiom(odef, df.getOWLSubClassOfAxiom(moderateimpactshared, impact));
        m.addAxiom(odef, df.getOWLSubClassOfAxiom(highimpactshared, impact));

        m.addAxiom(odef, df.getOWLEquivalentClassesAxiom(highimpactalldsc, (df.getOWLDataSomeValuesFrom(df.getOWLDataProperty(IRI.create(iriPatternDSC)), df.getOWLDatatypeMinExclusiveRestriction(1000)))));
        m.addAxiom(odef, df.getOWLEquivalentClassesAxiom(moderateimpactalldsc, (df.getOWLDataSomeValuesFrom(df.getOWLDataProperty(IRI.create(iriPatternDSC)), df.getOWLDatatypeMinExclusiveRestriction(250)))));
        m.addAxiom(odef, df.getOWLEquivalentClassesAxiom(lowimpactalldsc, (df.getOWLDataSomeValuesFrom(df.getOWLDataProperty(IRI.create(iriPatternDSC)), df.getOWLDatatypeMinExclusiveRestriction(50)))));
        m.addAxiom(odef, df.getOWLEquivalentClassesAxiom(highimpactallidsc, (df.getOWLDataSomeValuesFrom(df.getOWLDataProperty(IRI.create(iriPatternIDSC)), df.getOWLDatatypeMinExclusiveRestriction(1000)))));
        m.addAxiom(odef, df.getOWLEquivalentClassesAxiom(moderateimpactallidsc, (df.getOWLDataSomeValuesFrom(df.getOWLDataProperty(IRI.create(iriPatternIDSC)), df.getOWLDatatypeMinExclusiveRestriction(250)))));
        m.addAxiom(odef, df.getOWLEquivalentClassesAxiom(lowimpactallidsc, (df.getOWLDataSomeValuesFrom(df.getOWLDataProperty(IRI.create(iriPatternIDSC)), df.getOWLDatatypeMinExclusiveRestriction(50)))));

        addDefinedClassesShared(odef, iriPatternDSC, iriPatternIDSC, m, lowimpactshared, 1);
        addDefinedClassesShared(odef, iriPatternDSC, iriPatternIDSC, m, moderateimpactshared, 10);
        addDefinedClassesShared(odef, iriPatternDSC, iriPatternIDSC, m, highimpactshared, 25);
    }

    private void addDefinedClassesShared(OWLOntology odef, String iriPatternDSC, String iriPatternIDSC, OWLOntologyManager m, OWLClass lowimpactshared, int i) {
        Set<OWLClassExpression> dtrestrictions = new HashSet<>();
        for (OWLDataProperty dp : odef.getDataPropertiesInSignature(Imports.INCLUDED)) {
            if (!dp.equals(df.getOWLDataProperty(IRI.create(iriPatternDSC))) && !dp.equals(df.getOWLDataProperty(IRI.create(iriPatternIDSC)))) {
                dtrestrictions.add(df.getOWLDataSomeValuesFrom(dp, df.getOWLDatatypeMinExclusiveRestriction(i)));
            }
        }
        m.addAxiom(odef, df.getOWLEquivalentClassesAxiom(lowimpactshared, df.getOWLObjectIntersectionOf(dtrestrictions)));
    }

    OWLDataProperty dp_patternDSC = df.getOWLDataProperty(IRI.create(EBIBASE + "patternDSC"));
    OWLDataProperty dp_patternIDSC = df.getOWLDataProperty(IRI.create(EBIBASE + "patternIDSC"));
    OWLAnnotationProperty ap_patternDSC = df.getOWLAnnotationProperty(IRI.create(EBIBASE + "annoPatternDSC"));
    OWLAnnotationProperty ap_patternIDSC = df.getOWLAnnotationProperty(IRI.create(EBIBASE + "annoPatternIDSC"));

    private void addImpactIndividualAssertionsToOntology(OWLOntology o, List<OWLOntologyChange> changes, Map<String, Integer> m2_directsubclassbyo, int m1, Map<String, Integer> m4_directsubclassbyo, int m3, OWLNamedIndividual i) {
        changes.add(new AddAxiom(o, df.getOWLDataPropertyAssertionAxiom(dp_patternDSC, i, df.getOWLLiteral(m1))));
        changes.add(new AddAxiom(o, df.getOWLDataPropertyAssertionAxiom(dp_patternIDSC, i, df.getOWLLiteral(m3))));

        for (String oid : m2_directsubclassbyo.keySet()) {
            changes.add(new AddAxiom(o, df.getOWLDataPropertyAssertionAxiom(df.getOWLDataProperty(IRI.create(EBIBASE + "patternDSC_" + oid)), i, df.getOWLLiteral(m2_directsubclassbyo.get(oid)))));
        }
        for (String oid : m4_directsubclassbyo.keySet()) {
            changes.add(new AddAxiom(o, df.getOWLDataPropertyAssertionAxiom(df.getOWLDataProperty(IRI.create(EBIBASE + "patternIDSC_" + oid)), i, df.getOWLLiteral(m4_directsubclassbyo.get(oid)))));
        }
    }

    private void addImpactClassAnnotationsToOntology(OWLOntology o, List<OWLOntologyChange> changes, Map<String, Integer> m2_directsubclassbyo, int m1, Map<String, Integer> m4_directsubclassbyo, int m3, OWLClass pattern) {
        changes.add(new AddAxiom(o, df.getOWLAnnotationAssertionAxiom(ap_patternDSC, pattern.getIRI(), df.getOWLLiteral(m1))));
        changes.add(new AddAxiom(o, df.getOWLAnnotationAssertionAxiom(ap_patternIDSC, pattern.getIRI(), df.getOWLLiteral(m3))));

        for (String oid : m2_directsubclassbyo.keySet()) {
            changes.add(new AddAxiom(o, df.getOWLAnnotationAssertionAxiom(df.getOWLAnnotationProperty(IRI.create(EBIBASE + "annoPatternDSC_" + oid)), pattern.getIRI(), df.getOWLLiteral(m2_directsubclassbyo.get(oid)))));
        }
        for (String oid : m4_directsubclassbyo.keySet()) {
            changes.add(new AddAxiom(o, df.getOWLAnnotationAssertionAxiom(df.getOWLAnnotationProperty(IRI.create(EBIBASE + "annoPatternIDSC_" + oid)), pattern.getIRI(), df.getOWLLiteral(m4_directsubclassbyo.get(oid)))));
        }
    }


    private int computeMetrics(OWLReasoner r, OWLClass namedExtractedDefinition, Map<String, Integer> m2_directsubclassbyo, boolean direct) {
        int m1_directsubclassoverall = 0;

        Set<OWLClass> subcls = new HashSet<>(r.getSubClasses(namedExtractedDefinition, direct).getFlattened());
        subcls.removeAll(branches.getUnsatisfiableClasses());
        // Only count classes in the branches that we are interested in! Else we might count ALL instances of a pattern.
        subcls.retainAll(branches.getAllClassesInBranches());

        for (OWLClass subclass : subcls) {
            // Metric 1: Number of subclasses overall
            m1_directsubclassoverall++;
            // Metric 2: Number of subclasses by ontology
            for (String oid : allSignaturesAcrossOntologies.keySet()) {
                if (allSignaturesAcrossOntologies.get(oid).contains(subclass)) {
                    if (!m2_directsubclassbyo.containsKey(oid)) {
                        m2_directsubclassbyo.put(oid, 0);
                    }
                    m2_directsubclassbyo.put(oid, m2_directsubclassbyo.get(oid) + 1);
                }
            }
        }
        return m1_directsubclassoverall;
    }


    OWLAnnotationProperty ap_patternType = df.getOWLAnnotationProperty(IRI.create(EBIBASE + "patternType"));
    OWLAnnotationProperty ap_associatedPattern = df.getOWLAnnotationProperty(IRI.create(EBIBASE + "associatedPattern"));

    private Map<OWLClass, OWLNamedIndividual> addPatternsToOntology(Map<OWLClass, OWLClassExpression> defs, OWLOntology o) {
        Map<OWLClass, OWLNamedIndividual> classIndividualMap = new HashMap<>();
        long s=System.currentTimeMillis();
        long timing_b = 0;
        long timing_c = 0;

        List<OWLOntologyChange> changes = new ArrayList<>();
        for (OWLClass defclass : defs.keySet()) {
            long st2 = System.currentTimeMillis();
            OWLNamedIndividual i = df.getOWLNamedIndividual(IRI.create(EBIBASE + "i_" + defclass.getIRI().getRemainder().or(UUID.randomUUID() + "")));
            classIndividualMap.put(defclass, i);
            timing_b+=(System.currentTimeMillis()-st2);
            long st3 = System.currentTimeMillis();
            changes.add(new AddAxiom(o, df.getOWLEquivalentClassesAxiom(defclass, defs.get(defclass))));
            timing_c+=(System.currentTimeMillis()-st3);
        }
        o.getOWLOntologyManager().applyChanges(changes);
        OntologyUtils.p("Add patterns to ontology: "+(System.currentTimeMillis()-s)/1000+" sec");
        OntologyUtils.p("Create Individual: "+((double)timing_b/(double)1000)+" sec");
        OntologyUtils.p("Create Definition: "+((double)timing_c/(double)1000)+" sec");
        return classIndividualMap;
    }

    private Map<OWLClass, OWLClassExpression> generatePatternsFromDefinitions(OWLReasoner r) throws OWLOntologyCreationException {
        return generateNamedClassesForExpressions(generateDefinitionPatterns(getDefinitionAxioms(), r));
    }

    private Map<OWLClass, OWLClassExpression> generateHighLevelDefinitions(OWLReasoner r) throws OWLOntologyCreationException {
        return generateNamedClassesForExpressions(generateHighLevelDefinitionPatterns(getDefinitionAxioms(), r));
    }

    private Set<OWLClassExpression> generateHighLevelDefinitionPatterns(Set<OWLEquivalentClassesAxiom> allDefinitions, OWLReasoner r) throws OWLOntologyCreationException {
        OntologyUtils.p("generateHighLevelDefinitionPatterns. Definitons: " + allDefinitions.size() + timer.getTimeElapsed());
        List<OWLClassExpression> generatedDefinitions = new ArrayList<>();

        int all = allDefinitions.size();
        int i = 0;
        long timespent_all = 0;

        for (OWLEquivalentClassesAxiom ax : allDefinitions) {
            long start = System.currentTimeMillis();
            i++;
            if(i % 100 == 0) {
                OntologyUtils.p("Processing definition "+i+"/"+all);
            }
            for (OWLClassExpression ce : ax.getClassExpressionsAsList()) {

                if (!ce.isClassExpressionLiteral()) {
                    //OntologyUtils.p("########CE:"+render.renderManchester(ce));

                    Map<IRI,IRI> replace = new HashMap<>();
                    for (OWLClass c : ce.getClassesInSignature()) {
                        replace.put(c.getIRI(),df.getOWLThing().getIRI());
                    }
                    OWLObjectDuplicator replacer = new OWLObjectDuplicator(m.getOWLDataFactory(), replace);
                    OWLClassExpression ceout = replacer.duplicateObject(ce);
                    generatedDefinitions.add(ceout);
                }
            }
            timespent_all +=(System.currentTimeMillis()-start);
        }

        OntologyUtils.p("Generated: " + generatedDefinitions.size());
        OntologyUtils.p("All: "+timespent_all/1000);
        return new HashSet<>(generatedDefinitions);
    }

    private Map<OWLClass, OWLClassExpression> generateNamedClassesForExpressions(Set<OWLClassExpression> allGeneratedExpressions) {
        OntologyUtils.p("generateNamedClassesForExpressions" + allGeneratedExpressions.size());

        Map<OWLClass, OWLClassExpression> defs = new HashMap<>();
        int id = 1;
        for (OWLClassExpression ce : allGeneratedExpressions) {
            OWLClass clpattern = df.getOWLClass(IRI.create("http://ebi.ac.uk#Pattern" + id));
            defs.put(clpattern, ce);
            id++;
        }
        return defs;
    }

    private Set<OWLEquivalentClassesAxiom> defs_sample = new HashSet<>();
    private Map<OWLClass,Set<OWLClass>> superClasses = new HashMap<>();
    private long timespent_all = 0;

    private Set<OWLClassExpression> generateDefinitionPatterns(Set<OWLEquivalentClassesAxiom> allDefinitions, OWLReasoner r) throws OWLOntologyCreationException {
        OntologyUtils.p("generateDefinitionPatterns. Definitons: " + allDefinitions.size() + timer.getTimeElapsed());
        Set<OWLClassExpression> generatedDefinitions = new HashSet<>();
        List<OWLClassExpression> existing_definitions = new ArrayList<>();

        OntologyUtils.p("generateDefinitionPatterns. Sampling: " + SAMPLESIZE);


        createSample(allDefinitions, defs_sample);
        int i = 0;
        int all = defs_sample.size();

        for (OWLEquivalentClassesAxiom ax : defs_sample) {
            long start = System.currentTimeMillis();
            i++;
            if(i % 1000 == 0) {
                OntologyUtils.p("Pattern: "+i+"/"+all);
                OntologyUtils.p("All: "+timespent_all/1000);
            }
            for (OWLClassExpression ce : ax.getClassExpressionsAsList()) {

                if (!ce.isClassExpressionLiteral()) {
                    existing_definitions.add(ce);
                    OWLClassExpression ceout = replaceUnsatisfiableClassesWithOWLThing(r, ce);
                    constructPatternsRecursively(ceout,r,generatedDefinitions);
                }
            }
            timespent_all +=(System.currentTimeMillis()-start);
        }

        Set<OWLClassExpression> exist = new HashSet<>(existing_definitions);
        OntologyUtils.p("Generated: " + generatedDefinitions.size());
        filterGeneratedDefinitions(generatedDefinitions,exist);

        OntologyUtils.p("Generated After Removing existing: " + generatedDefinitions.size());
        OntologyUtils.p("All: "+timespent_all/1000);
        return generatedDefinitions;
    }

    private OWLClassExpression replaceUnsatisfiableClassesWithOWLThing(OWLReasoner r, OWLClassExpression ce) {
        Map<IRI, IRI> iriMap = new HashMap<>();
        for(OWLClass c:ce.getClassesInSignature()) {
            if(!r.isSatisfiable(c)) {
                iriMap.put(c.getIRI(), df.getOWLThing().getIRI());
            }
        }
        OWLObjectDuplicator replacer = new OWLObjectDuplicator(m.getOWLDataFactory(), iriMap);
        return replacer.duplicateObject(ce);
    }

    private void filterGeneratedDefinitions(Set<OWLClassExpression> generatedDefinitions, Set<OWLClassExpression> exising) {
        Set<OWLClassExpression> remove = new HashSet<>();
        remove.addAll(exising);
        for(OWLClassExpression ce:generatedDefinitions) {
            if (containsDisjunctionWithOWLThing(ce)) {
               remove.add(ce);
            } else if(!containsDomainClasses(ce)){
                remove.add(ce);
            }
        }
        generatedDefinitions.removeAll(remove);
    }


    private void constructPatternsRecursively(OWLClassExpression ce, OWLReasoner r,Set<OWLClassExpression> patterns) {
        //OntologyUtils.p("##############");
       // OntologyUtils.p("Pattern: "+ patterns.size());
        patterns.add(ce);
        Set<OWLClassExpression> generated = generateAbstractions(ce, r);
        for(OWLClassExpression ceg:generated) {
            if(!patterns.contains(ceg)) {
                constructPatternsRecursively(ceg, r, patterns);
            }
        }
    }

    private Set<OWLClassExpression> generateAbstractions(OWLClassExpression ce, OWLReasoner r) {
        Set<OWLClassExpression> generated = new HashSet<>();
        Set<OWLClass> sig = ce.getClassesInSignature();
        for (OWLClass c : sig) {
            for (OWLClass superC : fetchSuperClasses(r, c)) {
                generated.add(replaceClassInClassExpression(ce, c, superC));
            }
        }
        return generated;
    }

    private boolean containsDomainClasses(OWLClassExpression rewritten) {
        //return  !Collections.disjoint(rewritten.getClassesInSignature(), allClassesInBranches);
        return rewritten.getClassesInSignature().stream().anyMatch(branches.getAllClassesInBranches()::contains);
    }

    private boolean containsDisjunctionWithOWLThing(OWLClassExpression rewritten) {
        for(OWLClassExpression ce:rewritten.getNestedClassExpressions()) {
            if(ce instanceof OWLObjectUnionOf) {
                if(ce.asDisjunctSet().contains(df.getOWLThing())) {
                    return true;
                }
            }
        }
        return false;
    }

    private Set<OWLClass> fetchSuperClasses(OWLReasoner r, OWLClass c) {
        if(superClasses.containsKey(c)) {
            return superClasses.get(c);
        } else {
            Set<OWLClass> superclasses = new HashSet<>();
            if(!c.equals(df.getOWLThing())) {
                superclasses.addAll(r.getSuperClasses(c, true).getFlattened());
                if(superclasses.isEmpty()) {
                    superclasses.add(df.getOWLThing());
                }
                superclasses.remove(c);
            }
            //superclasses.removeAll(allClassesInBranches);
            superClasses.put(c, superclasses);
            return superclasses;
        }
    }

    private void createSample(Set<OWLEquivalentClassesAxiom> allDefinitions, Set<OWLEquivalentClassesAxiom> defs_sample) {
        if(allDefinitions.size()>SAMPLESIZE) {
            List<OWLEquivalentClassesAxiom> alldefs = new ArrayList<>();
            alldefs.addAll(allDefinitions);
            Collections.shuffle(alldefs);
            defs_sample.addAll(alldefs.subList(0, SAMPLESIZE));
        } else {
            defs_sample.addAll(allDefinitions);
        }
    }

    private Set<OWLEquivalentClassesAxiom> getDefinitionAxioms() {
        OntologyUtils.p("getDefinitionAxioms" + timer.getTimeElapsed());
        Set<OWLEquivalentClassesAxiom> allDefinitions = new HashSet<>();
        for (OWLAxiom owlAxiom : definitions.keySet()) {
            if (owlAxiom instanceof OWLEquivalentClassesAxiom) {
                allDefinitions.add((OWLEquivalentClassesAxiom) owlAxiom);
            }
        }
        return allDefinitions;
    }

    OWLClass cl_tmp = df.getOWLClass(IRI.create("http://tmp.owl#A"));
    OWLOntologyManager m = OWLManager.createOWLOntologyManager();
    Map<OWLClassExpression,Map<OWLClass,Map<OWLClass,OWLClassExpression>>> cache = new HashMap<>();


    private OWLClassExpression replaceClassInClassExpression(OWLClassExpression ce, OWLClass c, OWLClass superC) {
        if(cache.containsKey(ce)) {
            if(cache.get(ce).containsKey(c)) {
                if(cache.get(ce).get(c).containsKey(superC)) {
                    return cache.get(ce).get(c).get(superC);
                }
            } else {
                cache.get(ce).put(c,new HashMap<>());
            }
        } else {
            cache.put(ce,new HashMap<>());
            cache.get(ce).put(c,new HashMap<>());
        }
        Map<IRI, IRI> iriMap = new HashMap<>();
        iriMap.put(c.getIRI(), superC.getIRI()); // one or more of these at once
        OWLObjectDuplicator replacer = new OWLObjectDuplicator(m.getOWLDataFactory(), iriMap);
        OWLClassExpression ceout = replacer.duplicateObject(ce);
        cache.get(ce).get(c).put(superC,ceout);
        return ceout;
    }

    private OWLClassExpression replaceClassInAxiomOLD(OWLClassExpression ce, OWLClass c, OWLClass superC) {
        try {
            OWLOntology o = OWLManager.createOWLOntologyManager().createOntology();

            OWLAxiom ax = df.getOWLEquivalentClassesAxiom(ce, cl_tmp);
            o.getOWLOntologyManager().addAxiom(o, ax);

            /*

Map<IRI, IRI> iriMap = new HashMap();
iriMap.put(c.getIRI(), superC.getIRI()); // one or more of these at once
 OWLObjectDuplicator replacer = new OWLObjectDuplicator(iriMap, manager);
List<OWLAxiom> newAxioms =
axioms.map(replacer::duplicateObject).collect(Collectors.toList());
This version has only one manager, only one duplicator created, and no
ontologies at all.
 */
            OWLEntityRenamer renamer = new OWLEntityRenamer(o.getOWLOntologyManager(), o.getImportsClosure());
            o.getOWLOntologyManager().applyChanges(renamer.changeIRI(c, superC.getIRI()));
            for (OWLAxiom nax : o.getAxioms()) {
                for (OWLClassExpression cout : ((OWLEquivalentClassesAxiom) nax).getClassExpressionsAsList()) {
                    if (!cout.isClassExpressionLiteral()) {
                        return cout;
                    }
                }

            }
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void printRelationsToDefinition() {
        report.addEmptyLine();
        report.addLine("# Prominent relations used across definitions");
        report.addEmptyLine();
        Map<String, Set<OWLEntity>> labelmapping = new HashMap<>();
        HashMap<String, Integer> countRel = new HashMap<>();
        Map<String, Set<String>> ontRels = new HashMap<>();
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

        TreeMap<Object, Integer> sortedMap = OntologyUtils.sortMapByValue(countRel);
        for (Object o : sortedMap.keySet()) {
            String label = (String) o;

            report.addLine("");
            report.addLine("## " + label + " (label)");
            report.addLine("* Number of distinct definitions using this label: " + sortedMap.get(o));
            report.addLine("* Ontologies make use of this relation: " + ontRels.get(label));
            report.addLine("* Relations used for this label: " + labelmapping.get(label));

            for (OWLEntity e : labelmapping.get(label)) {
                report.addLine("### " + e.getIRI());
                for (OWLAxiom ax : relationsToDefinitions.get(e).keySet()) {
                    report.addLine("* " + render.renderForMarkdown(ax));
                    for (String oid : relationsToDefinitions.get(e).get(ax)) {
                        report.addLine("  * " + oid);
                    }
                }
            }
        }

    }

    private void printClassesToDefinition() {
        report.addLine("");
        report.addLine("# Prominent classes used across definitions");
        report.addLine("Omitting classes that only occur in one definition");
        for (OWLClass e : classesToDefinitions.keySet()) {
            Integer defs = classesToDefinitions.get(e).keySet().size();
            if (defs > 1) {
                report.addLine("");
                report.addLine("## " + getLabel(e));
                Set<String> onts = new HashSet<>();
                classesToDefinitions.get(e).keySet().forEach(ax -> onts.addAll(classesToDefinitions.get(e).get(ax)));
                report.addLine("* Ontologies make use of this relation: " + onts);
                report.addLine("### " + e.getIRI());
                for (OWLAxiom ax : classesToDefinitions.get(e).keySet()) {
                    report.addLine("* " + render.renderForMarkdown(ax));
                    for (String oid : classesToDefinitions.get(e).get(ax)) {
                        report.addLine("  * " + oid);
                    }
                }
            }
        }
    }

    private void printDefinitionAnalysis() {
        report.addLine("");
        report.addLine("# Definition Analysis: Grammar and constructs");
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
        report.addLine("# Entity counts");
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
                report.addLine("");
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
        return render.getLabel(k);
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

    private OWLOntology getUberOntology() {
        try {
            OWLOntology o = OWLManager.createOWLOntologyManager().createOntology(IRI.create(EBIBASE.replaceAll("#","")+"UnionOntology"));
            allAxiomsAcrossOntologies.keySet().forEach(oid -> o.getOWLOntologyManager().addAxioms(o, allAxiomsAcrossOntologies.get(oid)));
            return o;
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }
        return null;
    }
}
