package monarch.ontology.phenoworkbench.analytics.pattern.report;

import monarch.ontology.phenoworkbench.analytics.inferenceanalysis.OntologyStatsRunner;
import monarch.ontology.phenoworkbench.analytics.pattern.generation.*;
import monarch.ontology.phenoworkbench.util.*;
import monarch.ontology.phenoworkbench.util.Timer;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import java.io.File;
import java.util.*;


public class PatternSurvey  {

    private RenderManager render = RenderManager.getInstance();
    private PatternGenerator patternGenerator = new PatternGenerator(render);

    private List<Map<String,String>> data_c = new ArrayList<>();
    private List<Map<String,String>> data_d = new ArrayList<>();
    private List<Map<String,String>> data_e = new ArrayList<>();
    private List<Map<String,String>> data_exp = new ArrayList<>();
    private Set<Map<String,String>> data_l = new HashSet<>();
    private List<Map<String,String>> data_i = new ArrayList<>();
    private Map<OWLClass,Set<DefinedClass>> definedClasses = new HashMap<>();
    private Set<DefinedClass> allDefinedClasses = new HashSet<>();
    private Set<OWLClassExpression> nested = new HashSet<>();
    private Map<OWLEntity,OWLClassExpression> map_e = new HashMap<>();


    private final String phenoclass;
    private final String uri;
    private final String id;
    private OWLOntology o = null;
    private OWLDataFactory df = OWLManager.getOWLDataFactory();

    private PatternSurvey(String uri, String phenoclass, String id) {
        this.uri = uri;
        this.phenoclass = phenoclass;
        this.id = id;
    }

    public void runAnalysis() {
        OntologyUtils.p("Process Ontologies" + Timer.getSecondsElapsed("PatternExtractor::runAnalysis"));

        try {
            o = OWLManager.createOWLOntologyManager().loadOntology(IRI.create(uri));
            render.addLabel(o);
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }
        OntologyUtils.p("Create Reasoner" +Timer.getSecondsElapsed("PatternExtractor::runAnalysis"));
        BranchLoader branches = new BranchLoader();
        Reasoner rs = new Reasoner(o);
        OWLReasoner r = rs.getOWLReasoner();
        OntologyUtils.p("Precompute unsatisfiable classes" +Timer.getSecondsElapsed("PatternExtractor::runAnalysis"));
        branches.loadBranches(Collections.singleton(phenoclass),o.getClassesInSignature(Imports.INCLUDED),true,r);
        OntologyUtils.p("Subclasses" +Timer.getSecondsElapsed("A"));

        OntologyUtils.p("Extract definitions" +Timer.getSecondsElapsed("A"));
        patternGenerator.extractDefinedClasses(o.getAxioms(Imports.INCLUDED), false).forEach(this::putD);

        //Is needed to extract grammars:
        PatternManager man = new PatternManager(allDefinedClasses, rs, patternGenerator, render);
        System.out.println(man.getAllDefinedClasses().size());

        OntologyUtils.p("Harvest Data" +Timer.getSecondsElapsed("A"));
        OntologyUtils.p( branches.getAllClassesInBranches().size());
        branches.getAllClassesInBranches().forEach(this::harvest);

        OntologyUtils.p("Extract Nested Expressions" +Timer.getSecondsElapsed("A"));
        extractAllNestedClassExpressionsInOntology();
        OntologyUtils.p("Nested:" + nested.size() +Timer.getSecondsElapsed("A"));

        OntologyUtils.p("Add nested Expressions" +Timer.getSecondsElapsed("A"));
        addNestedClassExpressionsAsDefinedClasses();

        OntologyUtils.p("Reasoning" +Timer.getSecondsElapsed("A"));
        OWLReasoner r_incl = new ElkReasonerFactory().createReasoner(o);

        Map<OWLEntity,String> newlabel = new HashMap<>();

        for(OWLClass c:  branches.getAllClassesInBranches()) {
            for(OWLClass c_super:r_incl.getSuperClasses(c,false).getFlattened()) {
                Map<String,String> rec = new HashMap<>();
                rec.put("o",uri);
                rec.put("entity",c.getIRI().toString());
                rec.put("c_super",c_super.getIRI().toString());
                rec.put("label",newlabel(c_super,newlabel));
                rec.put("gen",map_e.containsKey(c_super)+"");
                rec.put("type","sub");
                data_i.add(rec);
            }
            for(OWLClass c_super:r_incl.getEquivalentClasses(c)) {
                Map<String,String> rec = new HashMap<>();
                rec.put("o",uri);
                rec.put("entity",c.getIRI().toString());
                rec.put("c_super",c_super.getIRI().toString());
                rec.put("label",newlabel(c_super,newlabel));
                rec.put("gen",map_e.containsKey(c_super)+"");
                rec.put("type","eq");
                data_i.add(rec);
            }
        }

    }

    private void addNestedClassExpressionsAsDefinedClasses() {
        Set<OWLAxiom> newaxioms = new HashSet<>();

        int ct = 1;
        // Materialise all sub-expressions
        String base = "http://gen.owl#A";

        for(OWLClassExpression ce:nested) {
            OWLClass cgen = df.getOWLClass(IRI.create(base+ct));
            ct++;
            map_e.put(cgen,ce);
            newaxioms.add(df.getOWLEquivalentClassesAxiom(cgen,ce));
        }

        o.getOWLOntologyManager().addAxioms(o,newaxioms);
    }

    private void extractAllNestedClassExpressionsInOntology() {
        for(OWLAxiom ax : o.getAxioms(Imports.INCLUDED)) {
            for(OWLClassExpression ce:ax.getNestedClassExpressions()) {
                if(!ce.isClassExpressionLiteral()) {
                    nested.add(ce);
                }
            }
        }
    }

    private String newlabel(OWLClass c, Map<OWLEntity, String> newlabel) {
        if(!newlabel.containsKey(c)) {
            if(map_e.containsKey(c)) {
                String label = render.renderForMarkdown(map_e.get(c)).replaceAll("[^a-zA-Z0-9()]","").toLowerCase();
                newlabel.put(c,label);
            } else {
                newlabel.put(c,render.getLabel(c));
            }
        }
        return newlabel.get(c);
    }

    private void label(OWLEntity e) {
            for (String l : OntologyUtils.getLabels(e, o)) {
                Map<String, String> rec_l = new HashMap<>();
                rec_l.put("iri", e.getIRI().toString());
                rec_l.put("label", l);
                data_l.add(rec_l);
            }
    }

    private void putD(DefinedClass d) {
        if(!definedClasses.containsKey(d.getOWLClass())) {
            definedClasses.put(d.getOWLClass(),new HashSet<>());
        }
        definedClasses.get(d.getOWLClass()).add(d);
        allDefinedClasses.add(d);
    }

    private void harvest(OWLClass c) {
        label(c);
        String iri = c.getIRI().toString();
        Map<String,String> rec_c = new HashMap<>();
        rec_c.put("o",uri);
        rec_c.put("iri",iri);
        data_c.add(rec_c);
        if(definedClasses.containsKey(c)) {
            int rec_ct = 1;
            for (DefinedClass d : definedClasses.get(c)) {
                Map<String,String> rec_d = new HashMap<>();
                String id = iri+"_"+rec_ct;
                rec_d.put("id",id);
                rec_d.put("o",uri);
                rec_d.put("iri",c.getIRI().toString());
                rec_d.put("pattern",d.getPatternString());
                rec_d.put("grammar_sig",d.getGrammar().getGrammarSignature());
                rec_d.put("grammar",d.getGrammar().getOriginal());
                rec_d.put("eq", OntologyStatsRunner.isEQDefinition(d.getDefiniton())+"");
                data_d.add(rec_d);
                rec_ct++;
                for(OWLEntity e:d.getDefiniton().getSignature()) {
                    Map<String,String> rec_e = new HashMap<>();
                    rec_e.put("id",id);
                    rec_e.put("o",uri);
                    rec_e.put("entity",e.getIRI().toString());
                    rec_e.put("category","signature");
                    rec_e.put("type",e.getEntityType().getName());
                    data_e.add(rec_e);
                    label(e);
                }
                for(OWLClassExpression e:d.getDefiniton().getNestedClassExpressions()) {
                    Map<String,String> rec_e = new HashMap<>();
                    rec_e.put("id",id);
                    rec_e.put("o",uri);
                    rec_e.put("entity",render.renderForMarkdown(e));
                    rec_e.put("category","sub_expression");
                    rec_e.put("type",e.getClassExpressionType().getName());
                    if(e instanceof OWLObjectSomeValuesFrom) {
                        OWLObjectSomeValuesFrom es = (OWLObjectSomeValuesFrom) e;
                        if (!es.getProperty().isAnonymous()) {
                            for (OWLClass clazz : es.getFiller().getClassesInSignature()) {
                                Map<String, String> rec_exp = new HashMap<>();
                                rec_exp.put("id", id);
                                rec_exp.put("o", uri);
                                rec_exp.put("iri", clazz.getIRI().toString());
                                rec_exp.put("property",es.getProperty().asOWLObjectProperty().getIRI().toString());
                                data_exp.add(rec_exp);
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

        PatternSurvey p = new PatternSurvey(uri, phenoclass,id);
        p.runAnalysis();
        p.exportAll(out);
    }

    private void exportAll(File out) {
        String id = this.id.replaceAll("[^a-zA-z0-9_]","");
        Export.writeCSV(data_c, new File(out,"data_c_"+id+".csv"));
        Export.writeCSV(data_d, new File(out,"data_d_"+id+".csv"));
        Export.writeCSV(data_e, new File(out,"data_e_"+id+".csv"));
        List<Map<String,String>> labels = new ArrayList<>(data_l);
        Export.writeCSV(labels, new File(out,"data_l_"+id+".csv"));
        Export.writeCSV(data_i, new File(out,"data_i_"+id+".csv"));
        Export.writeCSV(data_exp, new File(out,"data_exp_"+id+".csv"));
    }

}
