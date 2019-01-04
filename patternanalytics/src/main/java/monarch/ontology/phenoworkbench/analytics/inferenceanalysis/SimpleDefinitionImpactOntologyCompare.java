package monarch.ontology.phenoworkbench.analytics.inferenceanalysis;

import monarch.ontology.phenoworkbench.util.DefinedClass;
import monarch.ontology.phenoworkbench.util.Reasoner;
import monarch.ontology.phenoworkbench.util.Subsumption;
import monarch.ontology.phenoworkbench.util.Timer;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.OWLXMLDocumentFormat;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.*;

public class SimpleDefinitionImpactOntologyCompare extends OntologyCompare {

    private final OWLDataFactory df = OWLManager.getOWLDataFactory();
    private final List<String> csv = new ArrayList<>();
    private final DefinitionSet base;
    private Reasoner r_bare;



    Map<Subsumption,String> sub_bare_not_base = null;
    Map<Subsumption,String> sub_base_not_bare = null;

    public SimpleDefinitionImpactOntologyCompare(DefinitionSet base, OWLOntology o_bare, String baseId, String compId, String oid) {
        super();
        this.base = base;
        String process = "impactcompare";
        log("Comparing: "+oid,process);
        log("String defs", process);
            try {
                log("Create ontologies",process);

                OWLOntology o_base = copy(o_bare);

                addDefinitions(o_base,base);

                log("Creating reasoners..",process);

                r_bare = new Reasoner(o_bare);
                Reasoner r_base = new Reasoner(o_base);


                log("Obtaining subsumptions",process);

                DefinitionSet bare = new DefinitionSet();

                sub_bare_not_base = diff(r_bare,r_base,base.getDefinedClasses(),bare,base);
                sub_base_not_bare = diff(r_base,r_bare,base.getDefinedClasses(),base,bare);

                log("Computing diffs, sub_bare_not_base: "+sub_bare_not_base.size(),process);
                log("Computing diffs, sub_base_not_bare: "+sub_base_not_bare.size(),process);

                log("Extracting csv",process);

                getCsv().add("sub,super,type,cat,o,comp,yes,no");
                extractCSV(oid, sub_bare_not_base, "sub_bare_not_base",baseId+"_"+compId,"bare",baseId);
                extractCSV(oid, sub_base_not_bare, "sub_base_not_bare",baseId+"_"+compId,baseId,"bare");

            } catch (OWLOntologyCreationException e) {
                e.printStackTrace();
            }

    }

    private void log(String s, String process) {
        System.out.println(s+" "+Timer.getSecondsElapsed(process));
    }

    private void extractCSV(String oid, Map<Subsumption,String> sub_bare_not_base, String cat, String comp, String yes, String no) {
        for(Subsumption s:sub_bare_not_base.keySet()) {
            getCsv().add(s.getSub_c().getIRI().toString()+","+s.getSuper_c().getIRI().toString()+"," + sub_bare_not_base.get(s)+","+cat+","+oid+","+comp+","+yes+","+no);
        }
    }

    private Map<Subsumption,String> diff(Reasoner r1, Reasoner r2,Set<OWLClass> phenotypes, DefinitionSet d1, DefinitionSet d2) {
        Map<Subsumption,String> diff = new HashMap<>();
        for(OWLClass phenotype:phenotypes) {
            for(OWLClass c_sub:r1.getSubclassesOf(phenotype,true,true)) {
                if(!r2.getSubclassesOf(phenotype,false,true).contains(c_sub)){
                    diff.put(new Subsumption(phenotype,c_sub),inferenceClassification(c_sub,phenotype,d1,d2,r1,r2));
                }
            }
            for(OWLClass c_super:r1.getSuperClassesOf(phenotype,true,true)) {
                if(!r2.getSuperClassesOf(phenotype,false,true).contains(c_super)){
                    diff.put(new Subsumption(c_super,phenotype),inferenceClassification(phenotype,c_super,d1,d2,r1,r2));
                }
            }
        }
        return diff;
    }

    private String inferenceClassification(OWLClass c_sub, OWLClass c_super, DefinitionSet d1, DefinitionSet d2, Reasoner r1, Reasoner r2) {

        if(d1.containsDefinedClass(c_sub)) {
            DefinedClass sub1 = d1.getDefinedClass(c_sub).get();
            OWLClassExpression def1_sub = sub1.getDefiniton();
            if(d2.containsDefinedClass(c_sub)) {
                DefinedClass sub2 = d2.getDefinedClass(c_sub).get();
                OWLClassExpression def2_sub = sub2.getDefiniton();
                if(def1_sub instanceof OWLObjectSomeValuesFrom) {
                    OWLObjectSomeValuesFrom def1svf = (OWLObjectSomeValuesFrom)def1_sub;
                    if(def2_sub instanceof OWLObjectIntersectionOf&&isHasPartPattern(def1svf)) { // approximation of QinE pattern
                        if(isContainsHasPartExpression(def1svf.getFiller())) {
                            return "nested_haspart";
                        } else if(isContainsPhenotype(def1svf.getFiller())) {
                            return "nested_phenotype";
                        }
                    }
                }
                if(isAsubB(def1_sub,PhenoEntities.c_quality,r1)) {

                }

            }
            if(d1.containsDefinedClass(c_super)) {
                DefinedClass super1 = d1.getDefinedClass(c_super).get();
                OWLClassExpression def1_super = super1.getDefiniton();
               if(isAsubB(def1_super,def1_sub,r_bare)&&isAsubB(c_sub,c_super,r_bare)&&!isAsubB(c_super,c_sub,r_bare)) {
                   return "subclass_more_general_def";
               }

            }

            //transitive has part

        }
        if(isPATO(c_super)) {
            return "subclass_of_pato";
        }

        return "unclassified";
    }

    private boolean isAsubB(OWLClassExpression a, OWLClassExpression b, Reasoner r) {
        return r.isEntailed(df.getOWLSubClassOfAxiom(a,b)).orElse(false);
    }


    private boolean isPATO(OWLClass owlClass) {
        return owlClass.getIRI().toString().contains("PATO_");
    }

    private boolean isContainsPhenotype(OWLClassExpression filler) {
        for(OWLClass c:filler.getClassesInSignature()) {
            if(this.base.containsDefinedClass(c)) {
                return true;
            }
        }
        return false;
    }

    private boolean isContainsHasPartExpression(OWLClassExpression ce) {
        return ce.getClassesInSignature().contains(PhenoEntities.op_haspart);
    }

    private void addDefinitions(OWLOntology o, DefinitionSet base) {
        Set<OWLAxiom> axioms = new HashSet<>();
        base.getDefinitions().forEach(d->axioms.add(df.getOWLEquivalentClassesAxiom(d.getDefiniton(),d.getOWLClass())));
        o.getOWLOntologyManager().addAxioms(o,axioms);
    }

    private OWLOntology copy(OWLOntology o_bare) throws OWLOntologyCreationException {
        return OWLManager.createOWLOntologyManager().createOntology(o_bare.getAxioms(Imports.INCLUDED));
    }

    public List<String> getCsv() {
        return csv;
    }

    public Map<Subsumption, String> getSub_bare_not_base() {
        return sub_bare_not_base;
    }

    public Map<Subsumption, String> getSub_base_not_bare() {
        return sub_base_not_bare;
    }

    public boolean isHasPartPattern(OWLObjectSomeValuesFrom def1) {
        return !def1.getProperty().isAnonymous()&&def1.getProperty().asOWLObjectProperty().equals(PhenoEntities.op_haspart);
    }
}
