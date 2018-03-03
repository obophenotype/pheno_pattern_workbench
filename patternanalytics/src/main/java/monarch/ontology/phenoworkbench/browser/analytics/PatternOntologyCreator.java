package monarch.ontology.phenoworkbench.browser.analytics;

import monarch.ontology.phenoworkbench.browser.util.OntologyUtils;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;

import java.util.*;

public class PatternOntologyCreator {
    private final OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
    private final OWLDataProperty dp_patternDSC = df.getOWLDataProperty(IRI.create(PatternIRIs.EBIBASE + "patternDSC"));
    private final OWLDataProperty dp_patternIDSC = df.getOWLDataProperty(IRI.create(PatternIRIs.EBIBASE + "patternIDSC"));
    private final  OWLAnnotationProperty ap_patternDSC = df.getOWLAnnotationProperty(IRI.create(PatternIRIs.EBIBASE + "annoPatternDSC"));
    private final OWLAnnotationProperty ap_patternIDSC = df.getOWLAnnotationProperty(IRI.create(PatternIRIs.EBIBASE + "annoPatternIDSC"));
    private final  OWLAnnotationProperty ap_patternType = df.getOWLAnnotationProperty(IRI.create(PatternIRIs.EBIBASE + "patternType"));
    private final OWLAnnotationProperty ap_associatedPattern = df.getOWLAnnotationProperty(IRI.create(PatternIRIs.EBIBASE + "associatedPattern"));
    private final OWLClass clpatterntop = df.getOWLClass(IRI.create(PatternIRIs.EBIBASE + "Pattern"));


    public void addDefinedClassesForImpact(OWLOntology odef) {

        OWLOntologyManager m = odef.getOWLOntologyManager();
        OWLClass impact = df.getOWLClass(IRI.create(PatternIRIs.EBIBASE + "Impact"));
        OWLClass highimpactalldsc = df.getOWLClass(IRI.create(PatternIRIs.EBIBASE + "HighImpactDSCAll"));
        OWLClass moderateimpactalldsc = df.getOWLClass(IRI.create(PatternIRIs.EBIBASE + "ModerateImpactDSCAll"));
        OWLClass lowimpactalldsc = df.getOWLClass(IRI.create(PatternIRIs.EBIBASE + "LowImpactDSCAll"));
        OWLClass highimpactallidsc = df.getOWLClass(IRI.create(PatternIRIs.EBIBASE + "HighImpactIDSCAll"));
        OWLClass moderateimpactallidsc = df.getOWLClass(IRI.create(PatternIRIs.EBIBASE + "ModerateImpactIDSCAll"));
        OWLClass lowimpactallidsc = df.getOWLClass(IRI.create(PatternIRIs.EBIBASE + "LowImpactIDSCAll"));
        OWLClass lowimpactshared = df.getOWLClass(IRI.create(PatternIRIs.EBIBASE + "LowImpactShared"));
        OWLClass moderateimpactshared = df.getOWLClass(IRI.create(PatternIRIs.EBIBASE + "ModerateImpactShared"));
        OWLClass highimpactshared = df.getOWLClass(IRI.create(PatternIRIs.EBIBASE + "HighImpactShared"));
        m.addAxiom(odef, df.getOWLSubClassOfAxiom(highimpactalldsc, impact));
        m.addAxiom(odef, df.getOWLSubClassOfAxiom(moderateimpactalldsc, impact));
        m.addAxiom(odef, df.getOWLSubClassOfAxiom(lowimpactalldsc, impact));
        m.addAxiom(odef, df.getOWLSubClassOfAxiom(highimpactallidsc, impact));
        m.addAxiom(odef, df.getOWLSubClassOfAxiom(moderateimpactallidsc, impact));
        m.addAxiom(odef, df.getOWLSubClassOfAxiom(lowimpactallidsc, impact));
        m.addAxiom(odef, df.getOWLSubClassOfAxiom(lowimpactshared, impact));
        m.addAxiom(odef, df.getOWLSubClassOfAxiom(moderateimpactshared, impact));
        m.addAxiom(odef, df.getOWLSubClassOfAxiom(highimpactshared, impact));

        m.addAxiom(odef, df.getOWLEquivalentClassesAxiom(highimpactalldsc, (df.getOWLDataSomeValuesFrom(dp_patternDSC, df.getOWLDatatypeMinExclusiveRestriction(1000)))));
        m.addAxiom(odef, df.getOWLEquivalentClassesAxiom(moderateimpactalldsc, (df.getOWLDataSomeValuesFrom(dp_patternDSC, df.getOWLDatatypeMinExclusiveRestriction(250)))));
        m.addAxiom(odef, df.getOWLEquivalentClassesAxiom(lowimpactalldsc, (df.getOWLDataSomeValuesFrom(dp_patternDSC, df.getOWLDatatypeMinExclusiveRestriction(50)))));
        m.addAxiom(odef, df.getOWLEquivalentClassesAxiom(highimpactallidsc, (df.getOWLDataSomeValuesFrom(dp_patternIDSC, df.getOWLDatatypeMinExclusiveRestriction(1000)))));
        m.addAxiom(odef, df.getOWLEquivalentClassesAxiom(moderateimpactallidsc, (df.getOWLDataSomeValuesFrom(dp_patternIDSC, df.getOWLDatatypeMinExclusiveRestriction(250)))));
        m.addAxiom(odef, df.getOWLEquivalentClassesAxiom(lowimpactallidsc, (df.getOWLDataSomeValuesFrom(dp_patternIDSC, df.getOWLDatatypeMinExclusiveRestriction(50)))));

        addDefinedClassesShared(odef, m, lowimpactshared, 1);
        addDefinedClassesShared(odef, m, moderateimpactshared, 10);
        addDefinedClassesShared(odef, m, highimpactshared, 25);
    }

    private void addDefinedClassesShared(OWLOntology odef, OWLOntologyManager m, OWLClass lowimpactshared, int i) {
        Set<OWLClassExpression> dtrestrictions = new HashSet<>();
        for (OWLDataProperty dp : odef.getDataPropertiesInSignature(Imports.INCLUDED)) {
            if (!dp.equals(dp_patternDSC) && !dp.equals(dp_patternIDSC)) {
                dtrestrictions.add(df.getOWLDataSomeValuesFrom(dp, df.getOWLDatatypeMinExclusiveRestriction(i)));
            }
        }
        m.addAxiom(odef, df.getOWLEquivalentClassesAxiom(lowimpactshared, df.getOWLObjectIntersectionOf(dtrestrictions)));
    }

    public void copyAnnotationAssertionAxioms(OWLOntology from, OWLOntology to) {
        for (OWLEntity e : to.getSignature()) {
            Set<OWLAnnotationAssertionAxiom> axioms = new HashSet<>(from.getAnnotationAssertionAxioms(e.getIRI()));
            for (OWLAnnotationAssertionAxiom ax : axioms) {
                to.getOWLOntologyManager().applyChange(new AddAxiom(to, ax));
            }
        }
    }


    private void addImpactIndividualAssertionsToOntology(OWLOntology o, List<OWLOntologyChange> changes, PatternImpact.Impact impact, OWLNamedIndividual i) {

        changes.add(new AddAxiom(o, df.getOWLDataPropertyAssertionAxiom(dp_patternDSC, i, df.getOWLLiteral(impact.getDirectImpact()))));
        changes.add(new AddAxiom(o, df.getOWLDataPropertyAssertionAxiom(dp_patternIDSC, i, df.getOWLLiteral(impact.getIndirectImpact()))));

        Map<String,Integer> m2_directsubclassbyo = impact.getDirectImpactByO();
        for (String oid : m2_directsubclassbyo.keySet()) {
            changes.add(new AddAxiom(o, df.getOWLDataPropertyAssertionAxiom(df.getOWLDataProperty(IRI.create(PatternIRIs.EBIBASE + "patternDSC_" + oid)), i, df.getOWLLiteral(m2_directsubclassbyo.get(oid)))));
        }
        Map<String,Integer> m4_directsubclassbyo = impact.getIndirectImpactByO();
        for (String oid : m4_directsubclassbyo.keySet()) {
            changes.add(new AddAxiom(o, df.getOWLDataPropertyAssertionAxiom(df.getOWLDataProperty(IRI.create(PatternIRIs.EBIBASE + "patternIDSC_" + oid)), i, df.getOWLLiteral(m4_directsubclassbyo.get(oid)))));
        }
    }

    public void addImpactAxiomsForABox(OWLClass patternName, OWLNamedIndividual i,OWLOntology o_definition_abox, List<OWLOntologyChange> aboxchanges, String def_mcr_syntax, PatternImpact.Impact impact) {
        OWLAnnotation indivLabel = df.getOWLAnnotation(df.getRDFSLabel(), df.getOWLLiteral(def_mcr_syntax.replaceAll("[^A-Za-z0-9_]", "")));
        OWLAnnotation indivDefinition = df.getOWLAnnotation(df.getRDFSComment(), df.getOWLLiteral(def_mcr_syntax));
        OWLAnnotation annoPatternType = df.getOWLAnnotation(ap_patternType, clpatterntop.getIRI());
        OWLAnnotation annoAssocPattern = df.getOWLAnnotation(ap_associatedPattern, patternName.getIRI());
        aboxchanges.add(new AddAxiom(o_definition_abox, df.getOWLAnnotationAssertionAxiom(patternName.getIRI(), annoPatternType)));
        aboxchanges.add(new AddAxiom(o_definition_abox, df.getOWLAnnotationAssertionAxiom(i.getIRI(), annoAssocPattern)));
        aboxchanges.add(new AddAxiom(o_definition_abox, df.getOWLAnnotationAssertionAxiom(i.getIRI(), indivLabel)));
        aboxchanges.add(new AddAxiom(o_definition_abox, df.getOWLAnnotationAssertionAxiom(i.getIRI(), indivDefinition)));
        addImpactIndividualAssertionsToOntology(o_definition_abox, aboxchanges, impact, i);
    }

    public void addImpactAxiomsForTBox(OWLClass pattern,  OWLNamedIndividual i,OWLOntology o, List<OWLOntologyChange> changes, PatternImpact.Impact impact) {
        changes.add(new AddAxiom(o, df.getOWLAnnotationAssertionAxiom(ap_patternDSC, pattern.getIRI(), df.getOWLLiteral(impact.getDirectImpact()))));
        changes.add(new AddAxiom(o, df.getOWLAnnotationAssertionAxiom(ap_patternIDSC, pattern.getIRI(), df.getOWLLiteral(impact.getIndirectImpact()))));

        Map<String,Integer> m2_directsubclassbyo = impact.getDirectImpactByO();
        for (String oid : m2_directsubclassbyo.keySet()) {
            changes.add(new AddAxiom(o, df.getOWLAnnotationAssertionAxiom(df.getOWLAnnotationProperty(IRI.create(PatternIRIs.EBIBASE + "annoPatternDSC_" + oid)), pattern.getIRI(), df.getOWLLiteral(m2_directsubclassbyo.get(oid)))));
        }
        Map<String,Integer> m4_directsubclassbyo = impact.getIndirectImpactByO();
        for (String oid : m4_directsubclassbyo.keySet()) {
            changes.add(new AddAxiom(o, df.getOWLAnnotationAssertionAxiom(df.getOWLAnnotationProperty(IRI.create(PatternIRIs.EBIBASE + "annoPatternIDSC_" + oid)), pattern.getIRI(), df.getOWLLiteral(m4_directsubclassbyo.get(oid)))));
        }
    }

    public Map<OWLClass, OWLNamedIndividual> addPatternsToOntology(Set<Pattern> patterns, OWLOntology o) {
        Map<OWLClass, OWLNamedIndividual> classIndividualMap = new HashMap<>();
        long s=System.currentTimeMillis();
        long timing_b = 0;
        long timing_c = 0;

        List<OWLOntologyChange> changes = new ArrayList<>();
        for (Pattern p : patterns) {
            OWLClass defclass = p.getOWLClass();
            long st2 = System.currentTimeMillis();
            OWLNamedIndividual i = df.getOWLNamedIndividual(IRI.create(PatternIRIs.EBIBASE + "i_" + defclass.getIRI().getRemainder().or(UUID.randomUUID() + "")));
            classIndividualMap.put(defclass, i);
            timing_b+=(System.currentTimeMillis()-st2);
            long st3 = System.currentTimeMillis();
            changes.add(new AddAxiom(o, df.getOWLEquivalentClassesAxiom(defclass, p.getDefiniton())));
            timing_c+=(System.currentTimeMillis()-st3);
        }
        o.getOWLOntologyManager().applyChanges(changes);
        OntologyUtils.p("Add patterns to ontology: "+(System.currentTimeMillis()-s)/1000+" sec");
        OntologyUtils.p("Create Individual: "+((double)timing_b/(double)1000)+" sec");
        OntologyUtils.p("Create Definition: "+((double)timing_c/(double)1000)+" sec");
        return classIndividualMap;
    }
}