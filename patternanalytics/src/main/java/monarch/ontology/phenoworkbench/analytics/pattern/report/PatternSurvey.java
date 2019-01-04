package monarch.ontology.phenoworkbench.analytics.pattern.report;

import monarch.ontology.phenoworkbench.analytics.inferenceanalysis.DefinitionSet;
import monarch.ontology.phenoworkbench.analytics.inferenceanalysis.OntologyStatsRunner;
import monarch.ontology.phenoworkbench.analytics.pattern.generation.*;
import monarch.ontology.phenoworkbench.util.*;
import monarch.ontology.phenoworkbench.util.Timer;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.dlsyntax.renderer.DLSyntaxObjectRenderer;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import java.io.File;
import java.util.*;


public class PatternSurvey {

    private RenderManager render = RenderManager.getInstance();
    private PatternGenerator patternGenerator = new PatternGenerator(render);

    private List<Map<String, String>> data_c = new ArrayList<>();
    private List<Map<String, String>> data_d = new ArrayList<>();
    private List<Map<String, String>> data_e = new ArrayList<>();
    private List<Map<String, String>> data_exp = new ArrayList<>();
    private List<Map<String, String>> data_sc = new ArrayList<>();
    private List<Map<String, String>> data_subanalysis = new ArrayList<>();
    private Set<Map<String, String>> data_l = new HashSet<>();
    private List<Map<String, String>> data_i = new ArrayList<>();
    private Map<OWLClass, Set<DefinedClass>> definedClasses = new HashMap<>();
    private Set<DefinedClass> allDefinedClasses = new HashSet<>();
    private Set<OWLClassExpression> nested = new HashSet<>();
    private Map<OWLEntity, OWLClassExpression> map_e = new HashMap<>();
    private Map<String, Set<OWLClass>> map_property_classes = new HashMap<>(); //for computing all most abstract common super classes


    private final String phenoclass;
    private final String uri;
    private final String id;
    private OWLDataFactory df = OWLManager.getOWLDataFactory();

    private PatternSurvey(String uri, String phenoclass, String id) {
        this.uri = uri;
        this.phenoclass = phenoclass;
        this.id = id;
    }

    public void runAnalysis() {
        OntologyUtils.p("Process Ontologies" + Timer.getSecondsElapsed("PatternExtractor::runAnalysis"));
        try {
            OWLOntology o = OWLManager.createOWLOntologyManager().loadOntology(IRI.create(uri));
            render.addLabel(o);

            OntologyUtils.p("Create Reasoner" + Timer.getSecondsElapsed("PatternExtractor::runAnalysis"));
            BranchLoader branches = new BranchLoader();
            Reasoner rs = new Reasoner(o);

            OntologyUtils.p("Precompute unsatisfiable classes" + Timer.getSecondsElapsed("PatternExtractor::runAnalysis"));
            branches.loadBranches(Collections.singleton(phenoclass), o.getClassesInSignature(Imports.INCLUDED), true, rs.getOWLReasoner());
            OntologyUtils.p("Subclasses" + Timer.getSecondsElapsed("A"));

            OntologyUtils.p("Extract definitions" + Timer.getSecondsElapsed("A"));
            patternGenerator.extractDefinedClasses(o.getAxioms(Imports.INCLUDED), false).forEach(this::putD);

            //Is needed to extract grammars:
            PatternManager man = new PatternManager(allDefinedClasses, rs, patternGenerator, render);
            System.out.println(man.getAllDefinedClasses().size());

            OntologyUtils.p("Harvest Data" + Timer.getSecondsElapsed("A"));
            OntologyUtils.p(branches.getAllClassesInBranches().size());
            branches.getAllClassesInBranches().forEach(this::harvest);

            OntologyUtils.p("Extract Nested Expressions" + Timer.getSecondsElapsed("A"));
            extractAllNestedClassExpressionsInOntology(o);
            OntologyUtils.p("Nested:" + nested.size() + Timer.getSecondsElapsed("A"));

            OntologyUtils.p("Add nested Expressions" + Timer.getSecondsElapsed("A"));

            OWLOntology o_subconcept = OWLManager.createOWLOntologyManager().createOntology(o.getAxioms(Imports.INCLUDED));
            addNestedClassExpressionsAsDefinedClasses(o_subconcept);

            OntologyUtils.p("Reasoning" + Timer.getSecondsElapsed("A"));
            Reasoner r_incl = new Reasoner(o_subconcept);

            Map<OWLEntity, String> newlabel = new HashMap<>();

            for (OWLClass c : branches.getAllClassesInBranches()) {
                Set<OWLClass> allSuper=new HashSet<>(r_incl.getSuperClassesOf(c, false, true));
                Set<OWLClass> directSuper = new HashSet<>(r_incl.getSuperClassesOf(c, true, true));
                for (OWLClass c_super : allSuper) {
                    Map<String, String> rec = new HashMap<>();
                    rec.put("o", uri);
                    rec.put("entity", c.getIRI().toString());
                    rec.put("c_super", c_super.getIRI().toString());
                    rec.put("label", newlabel(c_super, newlabel));
                    rec.put("gen", map_e.containsKey(c_super) + "");
                    rec.put("direct", directSuper.contains(c_super) + "");
                    rec.put("type", "sub");
                    data_i.add(rec);
                }
                for (OWLClass c_super : r_incl.getEquivalentClasses(c)) {
                    Map<String, String> rec = new HashMap<>();
                    rec.put("o", uri);
                    rec.put("entity", c.getIRI().toString());
                    rec.put("c_super", c_super.getIRI().toString());
                    rec.put("label", newlabel(c_super, newlabel));
                    rec.put("gen", map_e.containsKey(c_super) + "");
                    rec.put("type", "eq");
                    rec.put("direct", "equivalent");
                    data_i.add(rec);
                }
            }

            OntologyUtils.p("Extracting labels" + Timer.getSecondsElapsed("A"));
            extractLabels(o);

            OntologyUtils.p("Extracting most commonly implied super classes " + Timer.getSecondsElapsed("A"));
            extractMostCommonlyImpliedSuperClasses(rs);

            // Subsubmption analysis
            Set<OWLClass> phenotypes = branches.getAllClassesInBranches();
            phenotypes.remove(df.getOWLClass(IRI.create(phenoclass))); //this is not a meaningful class for subsumption counting..


            // Reasoner rs holds everything (o)
            System.out.println("ALL: "+o.getAxioms(Imports.INCLUDED).size());
            OWLOntology o_subs = stripPhenotypesDefinitions(o, phenotypes).get();
            System.out.println("SUBS: "+o_subs.getAxioms().size());
            Reasoner r_subs = new Reasoner(o_subs);

            Set<Subsumption> asserted = new HashSet<>();
            OWLOntology o_defs = stripPhenotypesSubsumptions(o, phenotypes,asserted).get();
            System.out.println("DEFS: "+o_defs.getAxioms().size());
            Reasoner r_defs = new Reasoner(o_defs);


            OWLOntology o_bothstripped = stripPhenotypesDefinitions(o_defs, phenotypes).get();
            System.out.println("NEITHER: "+o_bothstripped.getAxioms().size());
            Reasoner r_bothstripped = new Reasoner(o_bothstripped);

            Set<Subsumption> neither_subs = new HashSet<>();
            //Compute all superclasses (for both reasoners, in case there is a reasoner bug.
            extractSuperClasses(phenotypes, r_bothstripped, neither_subs);

            Set<Subsumption> defis_subs = new HashSet<>();
            //Compute all superclasses (for both reasoners, in case there is a reasoner bug.
            extractSuperClasses(phenotypes, r_defs, defis_subs);

            Set<Subsumption> both_subs = new HashSet<>();
            //Compute all superclasses (for both reasoners, in case there is a reasoner bug.
            extractSuperClasses(phenotypes, rs, both_subs);

            Set<Subsumption> subs_subs = new HashSet<>();
            extractSuperClasses(phenotypes, r_subs, subs_subs);

            Set<Subsumption> all = new HashSet<>();
            all.addAll(defis_subs);
            all.addAll(subs_subs);
            all.addAll(both_subs);

            for (Subsumption s : all) {
                Map<String, String> rec = new HashMap<>();
                rec.put("sub", s.getSub_c().getIRI().toString());
                rec.put("super", s.getSuper_c().getIRI().toString());
                rec.put("asserted", asserted.contains(s) + "");
                String code = "";

                if (defis_subs.contains(s)) {
                    code+="d";
                }
                if (subs_subs.contains(s)) {
                    code+="s";
                }
                if(both_subs.contains(s)) {
                    code+="b";
                }
                if(neither_subs.contains(s)) {
                    code+="n";
                }
                rec.put("cat",code);

                data_subanalysis.add(rec);
            }

        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
            return;
        }
    }

    private void extractSuperClasses(Set<OWLClass> phenotypes, Reasoner r_defs, Set<Subsumption> defis_subs) {
        phenotypes.forEach(p -> r_defs.getSuperClassesOf(p, false, false).stream().filter(phenotypes::contains).forEach(superClass -> defis_subs.add(new Subsumption(superClass,p))));
    }

    private Set<OWLSubClassOfAxiom> extractAssertedSubclassOfAxioms(OWLOntology o, Set<OWLClass> phenotypes, Set<Subsumption> asserted) {
        Set<OWLSubClassOfAxiom> axiomsSub = new HashSet<>();
        for(OWLAxiom ax:o.getAxioms(Imports.INCLUDED)) {
            if(ax instanceof OWLSubClassOfAxiom) {
                OWLSubClassOfAxiom sax = ((OWLSubClassOfAxiom)ax);
                processSubClassAxiom(asserted, axiomsSub,sax,phenotypes);

            } else if(ax instanceof OWLEquivalentClassesAxiom) {
                for(OWLSubClassOfAxiom sax:((OWLEquivalentClassesAxiom)ax).asOWLSubClassOfAxioms()) {
                    processSubClassAxiom(asserted,axiomsSub, sax,phenotypes);
                }
            }
        }
        return axiomsSub;
    }


    private void processSubClassAxiom(Set<Subsumption> sbcl,Set<OWLSubClassOfAxiom> sbclAx, OWLSubClassOfAxiom sax, Set<OWLClass> phenotypes) {

        // Both sub and superclass must be atomic
        if(sax.getSuperClass().isClassExpressionLiteral()&&sax.getSubClass().isClassExpressionLiteral()) {
            OWLClass rhs = sax.getSuperClass().asOWLClass();
            OWLClass lhs = sax.getSubClass().asOWLClass();

            // Both super and subclass must be phenotypes;
            if(phenotypes.contains(lhs)&&phenotypes.contains(rhs)) {
                sbclAx.add(sax);
                sbcl.add(new Subsumption(rhs.asOWLClass(),lhs.asOWLClass()));
            }


        }


    }

    private void extractMostCommonlyImpliedSuperClasses(Reasoner rs) {
        for (String rel : map_property_classes.keySet()) {
            Map<OWLClass, Set<OWLClass>> map_superclasses = new HashMap<>(); //for computing all most abstract common super classes
            Map<OWLClass, Integer> map_superclassesCount = new HashMap<>();

            // For all referenced classes, get their super classes
            for (OWLClass c : map_property_classes.get(rel)) {
                if (!map_superclasses.containsKey(c)) {
                    map_superclasses.put(c, new HashSet<>(rs.getSuperClassesOf(c, false, true)));
                }
            }

            for (OWLClass c : map_superclasses.keySet()) {
                for (OWLClass superclass : map_superclasses.get(c)) {
                    if (!map_superclassesCount.containsKey(superclass)) {
                        map_superclassesCount.put(superclass, 0);
                    }
                    map_superclassesCount.put(superclass, map_superclassesCount.get(superclass) + 1);
                }
            }

            for (OWLClass c : map_superclassesCount.keySet()) {
                Map<String, String> rec = new HashMap<>();
                rec.put("property", rel);
                rec.put("superclass", c.getIRI().toString());
                rec.put("count", map_superclassesCount.get(c) + "");
                data_sc.add(rec);
            }

        }
    }

    private void extractLabels(OWLOntology o) {
        o.getSignature(Imports.INCLUDED).forEach(e -> label(e, o));
    }

    DLSyntaxObjectRenderer ren = new DLSyntaxObjectRenderer();

    private Optional<OWLOntology> stripPhenotypesDefinitions(OWLOntology o, Set<OWLClass> phenotypes) {

        Set<OWLAxiom> axioms = new HashSet<>();
        for (OWLAxiom ax : o.getAxioms(Imports.INCLUDED)) {
            if (ax instanceof OWLEquivalentClassesAxiom) {
                OWLEquivalentClassesAxiom eq = (OWLEquivalentClassesAxiom) ax;
                if (Collections.disjoint(eq.getNamedClasses(), phenotypes)) {
                    axioms.add(ax);
                }
            } else {
                axioms.add(ax);
            }
        }
        OWLOntology o_bare = null;
        try {
            o_bare = OWLManager.createOWLOntologyManager().createOntology(axioms);
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }
        return Optional.ofNullable(o_bare);
    }

    private Optional<OWLOntology> stripPhenotypesSubsumptions(OWLOntology o, Set<OWLClass> phenotypes, Set<Subsumption> asserted) {

        Set<OWLSubClassOfAxiom> axiomsSub = extractAssertedSubclassOfAxioms(o, phenotypes, asserted);
        Set<OWLAxiom> axioms = new HashSet<>(o.getAxioms(Imports.INCLUDED));
        axioms.removeAll(axiomsSub);

        OWLOntology o_bare = null;
        try {
            o_bare = OWLManager.createOWLOntologyManager().createOntology(axioms);
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }
        return Optional.ofNullable(o_bare);
    }

    private void addNestedClassExpressionsAsDefinedClasses(OWLOntology o) {
        Set<OWLAxiom> newaxioms = new HashSet<>();

        int ct = 1;
        // Materialise all sub-expressions
        String base = "http://gen.owl#A";

        for (OWLClassExpression ce : nested) {
            OWLClass cgen = df.getOWLClass(IRI.create(base + ct));
            ct++;
            map_e.put(cgen, ce);
            newaxioms.add(df.getOWLEquivalentClassesAxiom(cgen, ce));
        }

        o.getOWLOntologyManager().addAxioms(o, newaxioms);
    }

    private void extractAllNestedClassExpressionsInOntology(OWLOntology o) {
        for (OWLAxiom ax : o.getAxioms(Imports.INCLUDED)) {
            for (OWLClassExpression ce : ax.getNestedClassExpressions()) {
                if (!ce.isClassExpressionLiteral()) {
                    nested.add(ce);
                }
            }
        }
    }

    private String newlabel(OWLClass c, Map<OWLEntity, String> newlabel) {
        if (!newlabel.containsKey(c)) {
            if (map_e.containsKey(c)) {
                String label = render.renderForMarkdown(map_e.get(c)).replaceAll("[^a-zA-Z0-9()]", "").toLowerCase();
                newlabel.put(c, label);
            } else {
                newlabel.put(c, render.getLabel(c));
            }
        }
        return newlabel.get(c);
    }

    private void label(OWLEntity e, OWLOntology o) {
        for (String l : OntologyUtils.getLabelsRDFSIfExistsElseOther(e, o)) {
            Map<String, String> rec_l = new HashMap<>();
            rec_l.put("iri", e.getIRI().toString());
            rec_l.put("label", l);
            data_l.add(rec_l);
        }
    }

    private void putD(DefinedClass d) {
        if (!definedClasses.containsKey(d.getOWLClass())) {
            definedClasses.put(d.getOWLClass(), new HashSet<>());
        }
        definedClasses.get(d.getOWLClass()).add(d);
        allDefinedClasses.add(d);
    }

    private void harvest(OWLClass c) {
        String iri = c.getIRI().toString();
        Map<String, String> rec_c = new HashMap<>();
        rec_c.put("o", uri);
        rec_c.put("iri", iri);
        data_c.add(rec_c);
        if (definedClasses.containsKey(c)) {
            int rec_ct = 1;
            for (DefinedClass d : definedClasses.get(c)) {
                Map<String, String> rec_d = new HashMap<>();
                String id = iri + "_" + rec_ct;
                rec_d.put("id", id);
                rec_d.put("o", uri);
                rec_d.put("iri", c.getIRI().toString());
                rec_d.put("pattern", d.getPatternString());
                rec_d.put("grammar_sig", d.getGrammar().getGrammarSignature());
                rec_d.put("grammar", d.getGrammar().getOriginal());
                rec_d.put("eq", OntologyStatsRunner.isEQDefinition(d.getDefiniton()) + "");
                data_d.add(rec_d);
                rec_ct++;
                for (OWLEntity e : d.getDefiniton().getSignature()) {
                    Map<String, String> rec_e = new HashMap<>();
                    rec_e.put("id", id);
                    rec_e.put("o", uri);
                    rec_e.put("entity", e.getIRI().toString());
                    rec_e.put("category", "signature");
                    rec_e.put("type", e.getEntityType().getName());
                    data_e.add(rec_e);
                }
                for (OWLClassExpression e : d.getDefiniton().getNestedClassExpressions()) {
                    Map<String, String> rec_e = new HashMap<>();
                    rec_e.put("id", id);
                    rec_e.put("o", uri);
                    rec_e.put("entity", render.renderForMarkdown(e));
                    rec_e.put("category", "sub_expression");
                    rec_e.put("type", e.getClassExpressionType().getName());
                    if (e instanceof OWLObjectSomeValuesFrom) {
                        OWLObjectSomeValuesFrom es = (OWLObjectSomeValuesFrom) e;
                        if (!es.getProperty().isAnonymous()) {
                            for (OWLEntity entity : es.getFiller().getSignature()) {
                                Map<String, String> rec_exp = new HashMap<>();
                                rec_exp.put("id", id);
                                rec_exp.put("o", uri);
                                rec_exp.put("iri", entity.getIRI().toString());
                                rec_exp.put("etype",entity.getEntityType().getName());
                                String prop = es.getProperty().asOWLObjectProperty().getIRI().toString();
                                rec_exp.put("property", prop);
                                data_exp.add(rec_exp);
                                if(entity instanceof OWLClass) {
                                    if (!map_property_classes.containsKey(prop)) {
                                        map_property_classes.put(prop, new HashSet<>());
                                    }
                                    map_property_classes.get(prop).add((OWLClass)entity);
                                }
                            }
                        }
                    }
                    data_e.add(rec_e);
                }
            }
        }
    }

    public static void main(String[] args) {

        String uri = args[0];
        String phenoclass = args[1];
        String id = args[2];
        File out = new File(args[3]);

        PatternSurvey p = new PatternSurvey(uri, phenoclass, id);
        p.runAnalysis();
        p.exportAll(out);
    }

    private void exportAll(File out) {
        String id = this.id.replaceAll("[^a-zA-z0-9_]", "");
        Export.writeCSV(data_c, new File(out, "data_c_" + id + ".csv"));
        Export.writeCSV(data_d, new File(out, "data_d_" + id + ".csv"));
        Export.writeCSV(data_e, new File(out, "data_e_" + id + ".csv"));
        List<Map<String, String>> labels = new ArrayList<>(data_l);
        Export.writeCSV(labels, new File(out, "data_l_" + id + ".csv"));
        Export.writeCSV(data_i, new File(out, "data_i_" + id + ".csv"));
        Export.writeCSV(data_exp, new File(out, "data_exp_" + id + ".csv"));
        Export.writeCSV(data_sc, new File(out, "data_sc_" + id + ".csv"));
        Export.writeCSV(data_subanalysis, new File(out, "data_suba_" + id + ".csv"));
    }

}
