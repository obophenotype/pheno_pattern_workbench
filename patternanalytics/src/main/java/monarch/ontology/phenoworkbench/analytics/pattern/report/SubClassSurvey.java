package monarch.ontology.phenoworkbench.analytics.pattern.report;

import monarch.ontology.phenoworkbench.util.*;
import monarch.ontology.phenoworkbench.util.Timer;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import java.io.File;
import java.util.*;


public class SubClassSurvey {

    private RenderManager render = new RenderManager();
    private List<Map<String,String>> data_i = new ArrayList<>();
    private Set<OWLClassExpression> nested = new HashSet<>();
    private Map<OWLEntity,OWLClassExpression> map_e = new HashMap<>();


    private final String phenoclass1;
    private final String uri1;
    private final String id1;
    private final String phenoclass2;
    private final String uri2;
    private final String id2;
    private OWLOntology o = null;
    private OWLDataFactory df = OWLManager.getOWLDataFactory();


    private SubClassSurvey(String uri1, String uri2, String phenoclass1, String phenoclass2, String id1, String id2) {
        this.uri1 = uri1;
        this.uri2 = uri2;
        this.phenoclass1 = phenoclass1;
        this.phenoclass2 = phenoclass2;
        this.id1 = id1;
        this.id2 = id2;
    }

    public void runAnalysis() {
        OntologyUtils.p("Process Ontologies" + Timer.getSecondsElapsed("PatternExtractor::runAnalysis"));
        BranchLoader branches = new BranchLoader();
        OWLOntology o1;
        OWLOntology o2;
        try {
            o = OWLManager.createOWLOntologyManager().createOntology();
            o1 = OWLManager.createOWLOntologyManager().loadOntology(IRI.create(uri1));
            o2 = OWLManager.createOWLOntologyManager().loadOntology(IRI.create(uri2));
            render.addLabel(o1);
            render.addLabel(o2);
            o.getOWLOntologyManager().addAxioms(o,o1.getAxioms(Imports.INCLUDED));
            o.getOWLOntologyManager().addAxioms(o,o2.getAxioms(Imports.INCLUDED));
            OWLReasoner r1 = new Reasoner(o1).getOWLReasoner();
            OWLReasoner r2 = new Reasoner(o2).getOWLReasoner();
            branches.loadBranches(Collections.singleton(phenoclass1),o1.getClassesInSignature(Imports.INCLUDED),true,r1);
            branches.loadBranches(Collections.singleton(phenoclass2),o2.getClassesInSignature(Imports.INCLUDED),true,r2);

        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }

        OntologyUtils.p("Extract Nested Expressions" +Timer.getSecondsElapsed("A"));
        extractAllNestedClassExpressionsInOntology();
        OntologyUtils.p("Nested:" + nested.size() +Timer.getSecondsElapsed("A"));

        OntologyUtils.p("Add nested Expressions" +Timer.getSecondsElapsed("A"));
        addNestedClassExpressionsAsDefinedClasses();

        OntologyUtils.p("Reasoning" +Timer.getSecondsElapsed("A"));
        OWLReasoner r_incl = new ElkReasonerFactory().createReasoner(o);
        branches.addUnsatisfiableClasses(r_incl);
        Map<OWLEntity,String> newlabel = new HashMap<>();

        for(OWLClass c:  branches.getAllClassesInBranches()) {
            for(OWLClass c_super:r_incl.getSuperClasses(c,false).getFlattened()) {
                Map<String,String> rec = new HashMap<>();
                rec.put("o1",uri1);
                rec.put("o2",uri2);
                rec.put("entity",c.getIRI().toString());
                rec.put("c_super",c_super.getIRI().toString());
                rec.put("label",newlabel(c_super,newlabel));
                rec.put("gen",map_e.containsKey(c_super)+"");
                rec.put("type","sub");
                data_i.add(rec);
            }
            for(OWLClass c_super:r_incl.getEquivalentClasses(c)) {
                Map<String,String> rec = new HashMap<>();
                rec.put("o1",uri1);
                rec.put("o2",uri2);
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

    public static void main(String[] args) {

        String uri1 = args[0];
        String uri2 = args[1];
        String phenoclass1 = args[2];
        String phenoclass2 = args[3];
        String id1 = args[4];
        String id2 = args[5];
        File out = new File(args[6]);

        SubClassSurvey p = new SubClassSurvey(uri1,uri2,phenoclass1,phenoclass2,id1,id2);
        p.runAnalysis();
        p.exportAll(out);
    }

    private void exportAll(File out) {
        String id1 = this.id1.replaceAll("[^a-zA-z0-9_]","");
        String id2 = this.id2.replaceAll("[^a-zA-z0-9_]","");
        Export.writeCSV(data_i, new File(out,"data_ci_"+id1+"_"+id2+".csv"));
    }

}
