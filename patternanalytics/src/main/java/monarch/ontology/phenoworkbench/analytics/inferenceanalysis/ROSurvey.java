package monarch.ontology.phenoworkbench.analytics.inferenceanalysis;

import monarch.ontology.phenoworkbench.util.*;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.dlsyntax.renderer.DLSyntaxObjectRenderer;
import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class ROSurvey {

    public static OWLDataFactory df = OWLManager.getOWLDataFactory();
    public static RenderManager renderManager = RenderManager.getInstance();
    public static Set<PropertyUsage> usages = new HashSet<>();


    public static void main(String[] args) throws OWLOntologyCreationException {
        //args = new String[3];
        //args[0] = "/Volumes/EBI/tmp/ro/ro.owl";
        //args[1] = "/Volumes/EBI/tmp/ro/ontologies/agroportal_merged_cl.owl";
        //args[2] = "/data/rosurvey/";
        File f_ro = new File(args[0]);
        File f_o = new File(args[1]);
        File out = new File(args[2]);
        log("Loading RO");
        OWLOntology ro = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(f_ro);
        log("Loading " + f_o.getName());
        OWLOntology o = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(f_o);

        renderManager.addLabel(ro);
        renderManager.addLabel(o);
        OWLReasoner r = new ElkReasonerFactory().createReasoner(o);

        Set<OWLObjectProperty> ro_relations = computeRORelations(ro);
        ro_relations.retainAll(o.getObjectPropertiesInSignature());
        List<Map<String, String>> data_axiom = new ArrayList<>();
        List<Map<String, String>> data_expressions = new ArrayList<>();
        List<Map<String, String>> data_property = new ArrayList<>();
        OWLObjectRenderer ren = new DLSyntaxObjectRenderer();
        for (OWLObjectProperty p : ro_relations) {
            Map<String, String> rec = new HashMap<>();
            rec.put("p", p.getIRI().toString());
            rec.put("p_label",renderManager.getLabel(p));
            rec.put("o", f_o.getAbsolutePath());
            rec.put("domain", tubeSep(getPropertyDomains(p, o), ren));
            rec.put("range", tubeSep(getPropertyRange(p, o), ren));
            data_property.add(rec);
        }
        for (OWLAxiom ax : o.getAxioms(Imports.EXCLUDED)) {
            String axiomcode = ax.hashCode() + "";
            String axiomtype = ax.getAxiomType().getName();
            for (OWLObjectProperty p : ro_relations) {
                if (ax.containsEntityInSignature(p)) {
                    Map<String, String> rec = new HashMap<>();
                    rec.put("axiomid", axiomcode);
                    rec.put("axiomtype", axiomtype);
                    rec.put("o", f_o.getAbsolutePath());
                    rec.put("p", p.getIRI().toString());
                    Set<OWLObjectProperty> allp = ax.getObjectPropertiesInSignature();
                    allp.removeAll(ro_relations);
                    allp.remove(df.getOWLBottomObjectProperty());
                    allp.remove(df.getOWLTopObjectProperty());
                    rec.put("p", p.getIRI().toString());
                    rec.put("otherp",allp.size()+"");
                    data_axiom.add(rec);
                    if (ax instanceof OWLSubObjectPropertyOfAxiom) {
                        rec.put("subject", ren.render(((OWLSubObjectPropertyOfAxiom) ax).getSubProperty()));
                        rec.put("object", ren.render(((OWLSubObjectPropertyOfAxiom) ax).getSuperProperty()));
                    } else if (ax instanceof OWLEquivalentObjectPropertiesAxiom) {
                        String object = "";
                        for (OWLObjectPropertyExpression sax : ((OWLEquivalentObjectPropertiesAxiom) ax).getPropertiesMinus(p)) {
                            object += ren.render(sax) + "|";
                        }
                        if (object.endsWith("|")) {
                            object = object.substring(0, object.length() - 1);
                        }
                        rec.put("subject", "");
                        rec.put("object", object.trim());
                    } else if (ax instanceof OWLObjectPropertyAssertionAxiom) {
                        OWLObjectPropertyAssertionAxiom axa = (OWLObjectPropertyAssertionAxiom) ax;
                        OWLIndividual subject = axa.getSubject();
                        OWLIndividual object = axa.getObject();
                        rec.put("subject", ren.render(subject));
                        rec.put("object", ren.render(object));
                        extractDomainsAndRangesFromObjectPropertyAssertion(f_o, r, axiomtype, p, subject, object);
                    } else {
                        rec.put("subject", "");
                        rec.put("object", "");
                    }
                    if(ax instanceof OWLSubClassOfAxiom) {
                        OWLSubClassOfAxiom sax = (OWLSubClassOfAxiom)ax;
                        extractDomainsAndRangesFromSubclassAxiom(f_o, r, axiomtype, p, sax);
                    } else if(ax instanceof OWLEquivalentClassesAxiom) {
                        OWLEquivalentClassesAxiom sax = (OWLEquivalentClassesAxiom)ax;
                        for(OWLSubClassOfAxiom sex:sax.asOWLSubClassOfAxioms()) {
                            extractDomainsAndRangesFromSubclassAxiom(f_o, r, axiomtype, p, sex);
                        }
                    } else if(ax instanceof OWLObjectPropertyDomainAxiom) {
                        OWLObjectPropertyDomainAxiom dax = (OWLObjectPropertyDomainAxiom)ax;
                        extractDomainFromDomainAxiom(f_o, r, p, dax);
                    } else if(ax instanceof OWLClassAssertionAxiom) {
                        OWLClassAssertionAxiom dax = (OWLClassAssertionAxiom)ax;
                        extractDomainsAndRangesFromClassAssertionAxiom(f_o, r, p, dax);
                    } else if(ax instanceof OWLObjectPropertyRangeAxiom) {
                        OWLObjectPropertyRangeAxiom dax = (OWLObjectPropertyRangeAxiom)ax;
                        extractDomainFromRangeAxiom(f_o, r, p, dax);
                    }
                    for (OWLClassExpression ce : ax.getNestedClassExpressions()) {
                        if (ce.containsEntityInSignature(p)) {
                            Map<String, String> rec_exp = new HashMap<>();
                            rec_exp.put("axiomid", axiomcode);
                            rec_exp.put("expressiontype", ce.getClassExpressionType().getName());
                            rec_exp.put("o", f_o.getAbsolutePath());
                            rec_exp.put("p", p.getIRI().toString());

                            data_expressions.add(rec_exp);
                            //all axioms except for domain and range axioms are considered (and object property assertion, which wont ever appear in any case)
                            if(!(ax instanceof OWLObjectPropertyDomainAxiom)&&!(ax instanceof OWLObjectPropertyDomainAxiom)&&!(ax instanceof OWLObjectPropertyAssertionAxiom)) {
                                if (ce instanceof OWLNaryBooleanClassExpression) {
                                    OWLNaryBooleanClassExpression nce = (OWLNaryBooleanClassExpression) ce;
                                    extractDomainsAndRangesFromClassExpression(f_o, r, axiomtype, p, nce);
                                }
                            }

                        }
                    }
                }
            }
        }
        List<Map<String, String>> data_domainrange = new ArrayList<>();
        usages.forEach(u->data_domainrange.add(u.getData()));
        Export.writeCSV(data_axiom, new File(out, "rosurvey_axiomdata_" + f_o.getName() + ".csv"));
        Export.writeCSV(data_expressions, new File(out, "rosurvey_expressiondata_" + f_o.getName() + ".csv"));
        Export.writeCSV(data_property, new File(out, "rosurvey_propertydata_" + f_o.getName() + ".csv"));
        Export.writeCSV(data_domainrange, new File(out, "rosurvey_domainrangedata_" + f_o.getName() + ".csv"));

    }

    /*
    r:R some C (domain is all types of r, range is C plus superclasses)
     */
    private static void extractDomainsAndRangesFromClassAssertionAxiom(File f_o, OWLReasoner r, OWLObjectProperty p, OWLClassAssertionAxiom dax) {
        Set<OWLClassExpression> s_assert = new HashSet<>();
        Set<OWLClassExpression> o_assert = new HashSet<>();
        s_assert.addAll(r.getTypes(dax.getIndividual().asOWLNamedIndividual(),true).getFlattened());
        o_assert.add(dax.getClassExpression());
        Set<OWLClass> s_inferred = new HashSet<>();
        for(OWLClassExpression s:s_assert) {
            s_inferred.addAll(r.getSuperClasses(s,false).getFlattened());
            if(!s.isAnonymous()) {
                s_inferred.add(s.asOWLClass());
            }
        }
        Set<OWLClass> o_inferred = new HashSet<>();
        for(OWLClassExpression ob:o_assert) {
            o_inferred.addAll(r.getSuperClasses(ob,false).getFlattened());
            if(!ob.isAnonymous()) {
                o_inferred.add(ob.asOWLClass());
            }
        }
        for (OWLClass cis : s_inferred) {
            for (OWLClass cio :o_inferred) {
                usages.add(new PropertyUsage(f_o.getAbsolutePath(), p, "ClassAssertion", cio, cis));
            }
        }
    }

    private static void extractDomainFromRangeAxiom(File f_o, OWLReasoner r, OWLObjectProperty p, OWLObjectPropertyRangeAxiom dax) {
        Set<OWLClassExpression> s_assert = new HashSet<>();
        Set<OWLClassExpression> o_assert = new HashSet<>();
        o_assert.add(dax.getRange());
        s_assert.add(df.getOWLThing());
        Set<OWLClass> s_inferred = new HashSet<>();
        Set<OWLClass> o_inferred = new HashSet<>();
        for(OWLClassExpression s:s_assert) {
            s_inferred.addAll(r.getSuperClasses(s,false).getFlattened());
            if(!s.isAnonymous()) {
                s_inferred.add(s.asOWLClass());
            }
        }
        for(OWLClassExpression ob:o_assert) {
            o_inferred.addAll(r.getSuperClasses(ob,false).getFlattened());
            if(!ob.isAnonymous()) {
                o_inferred.add(ob.asOWLClass());
            }
        }
        for (OWLClass cis : s_inferred) {
            for (OWLClass cio :o_inferred) {
                usages.add(new PropertyUsage(f_o.getAbsolutePath(), p, "RangeAxiom", cio, cis));
            }
        }
    }

    private static void extractDomainFromDomainAxiom(File f_o, OWLReasoner r, OWLObjectProperty p, OWLObjectPropertyDomainAxiom dax) {
        Set<OWLClassExpression> s_assert = new HashSet<>();
        Set<OWLClassExpression> o_assert = new HashSet<>();
        s_assert.add(dax.getDomain());
        o_assert.add(df.getOWLThing());
        Set<OWLClass> s_inferred = new HashSet<>();
        Set<OWLClass> o_inferred = new HashSet<>();
        for(OWLClassExpression s:s_assert) {
            s_inferred.addAll(r.getSuperClasses(s,false).getFlattened());
            if(!s.isAnonymous()) {
                s_inferred.add(s.asOWLClass());
            }
        }
        for(OWLClassExpression ob:o_assert) {
            o_inferred.addAll(r.getSuperClasses(ob,false).getFlattened());
            if(!ob.isAnonymous()) {
                o_inferred.add(ob.asOWLClass());
            }
        }
        for (OWLClass cis : s_inferred) {
            for (OWLClass cio :o_inferred) {
                usages.add(new PropertyUsage(f_o.getAbsolutePath(), p, "DomainAxiom", cio, cis));
            }
        }
    }

    /*
        X and R some C
        X or R some C (Domain superclasses of X, range superclasses of C)
        type: InNaryClassExpression
     */
    private static void extractDomainsAndRangesFromClassExpression(File f_o, OWLReasoner r, String axiomtype, OWLObjectProperty p, OWLNaryBooleanClassExpression nce) {
        Set<OWLClassExpression> s_assert = new HashSet<>();
        Set<OWLClassExpression> o_assert = new HashSet<>();
        for(OWLClassExpression nceinner:nce.getOperandsAsList()) {
            //Only if I get an existential restriction as an operand of the n-ary expression will anything be recorded
            if (nceinner instanceof OWLObjectSomeValuesFrom) {
                OWLObjectSomeValuesFrom osvf = (OWLObjectSomeValuesFrom) nceinner;
                if (osvf.getProperty().equals(p)) {
                    o_assert.add(osvf.getFiller());
                    s_assert.addAll(nce.getOperandsAsList());
                }
            }
        }
        Set<OWLClass> s_inferred = new HashSet<>();
        for(OWLClassExpression s:s_assert) {
            s_inferred.addAll(r.getSuperClasses(s,false).getFlattened());
            if(!s.isAnonymous()) {
                s_inferred.add(s.asOWLClass());
            }
        }
        Set<OWLClass> o_inferred = new HashSet<>();
        for(OWLClassExpression ob:o_assert) {
            o_inferred.addAll(r.getSuperClasses(ob,false).getFlattened());
            if(!ob.isAnonymous()) {
                o_inferred.add(ob.asOWLClass());
            }
        }
        for (OWLClass cis : s_inferred) {
            for (OWLClass cio :o_inferred) {
                usages.add(new PropertyUsage(f_o.getAbsolutePath(), p, "InNaryClassExpression", cio, cis));
            }
        }
    }

    /*
        <a,R,c> : Domain of R is types of a, Range is types of C
        type: ObjectPropertyAssertion
    */
    private static void extractDomainsAndRangesFromObjectPropertyAssertion(File f_o, OWLReasoner r, String axiomtype, OWLObjectProperty p, OWLIndividual subject, OWLIndividual object) {
        if (subject.isNamed() && object.isNamed()) {
            for (OWLClass cis : r.getTypes(subject.asOWLNamedIndividual(), false).getFlattened()) {
                for (OWLClass cio : r.getTypes(object.asOWLNamedIndividual(), false).getFlattened()) {
                    usages.add(new PropertyUsage(f_o.getAbsolutePath(), p, "ObjectPropertyAssertion", cio, cis));
                }
            }
        }
    }

    /*
        A Sub R some C : Domain of R is A, Range is C
        R some C Sub A : Domain of R is owl:Thing, Range is C
        type: SubClassAxiom
     */
    private static void extractDomainsAndRangesFromSubclassAxiom(File f_o, OWLReasoner r, String axiomtype, OWLObjectProperty p, OWLSubClassOfAxiom sax) {
        OWLClassExpression sub = sax.getSubClass();
        OWLClassExpression supr = sax.getSuperClass();
        Set<OWLClassExpression> s_assert = new HashSet<>();
        Set<OWLClassExpression> o_assert = new HashSet<>();
        if(sub instanceof OWLObjectSomeValuesFrom) {
            OWLObjectSomeValuesFrom osvf = (OWLObjectSomeValuesFrom) sub;
            if (osvf.getProperty().equals(p)) {
                o_assert.add(osvf.getFiller());
                s_assert.add(supr);
            }
        }
        if(supr instanceof OWLObjectSomeValuesFrom) {
            OWLObjectSomeValuesFrom osvf = (OWLObjectSomeValuesFrom) supr;
            if (osvf.getProperty().equals(p)) {
                o_assert.add(osvf.getFiller());
                s_assert.add(df.getOWLThing());
            }
        }
        Set<OWLClass> s_inferred = new HashSet<>();
        Set<OWLClass> o_inferred = new HashSet<>();
        for(OWLClassExpression s:s_assert) {
            s_inferred.addAll(r.getSuperClasses(s,false).getFlattened());
            if(!s.isAnonymous()) {
                s_inferred.add(s.asOWLClass());
            }
        }
        for(OWLClassExpression ob:o_assert) {
            o_inferred.addAll(r.getSuperClasses(ob,false).getFlattened());
            if(!ob.isAnonymous()) {
                o_inferred.add(ob.asOWLClass());
            }
        }
        for (OWLClass cis : s_inferred) {
            for (OWLClass cio :o_inferred) {
                usages.add(new PropertyUsage(f_o.getAbsolutePath(), p, "SubOrEquivalenceClass", cio, cis));
            }
        }
    }

    private static String tubeSep(Set<OWLClassExpression> ces, OWLObjectRenderer ren) {
        if (ces.isEmpty()) {
            return "";
        }
        String domain = "";
        for (OWLClassExpression ce : ces) {
            domain += ren.render(ce) + "|";
        }
        domain = domain.substring(0, domain.length() - 1);
        return domain;
    }

    private static Set<OWLObjectProperty> computeRORelations(OWLOntology ro) {
        Set<OWLObjectProperty> properties = new HashSet<>();
        for (OWLObjectProperty p : ro.getObjectPropertiesInSignature(Imports.EXCLUDED)) {
            if (p.getIRI().toString().contains("RO_") | p.getIRI().toString().contains("BFO_") | p.getIRI().toString().contains("R0_")) {
                properties.add(p);
            } else {
                log(p);
            }
        }
        return properties;
    }

    private static void log(Object o) {
        System.out.println(o);
    }

    public static Set<OWLClassExpression> getPropertyDomains(OWLObjectProperty p, OWLOntology o) {
        Set<OWLClassExpression> ces = new HashSet<>();
        //return r.getObjectPropertyDomains(p,true).getFlattened(), ren, p);
        for (OWLAxiom ax : o.getAxioms(AxiomType.OBJECT_PROPERTY_DOMAIN).stream().collect(Collectors.toSet())) {
            if (ax instanceof OWLObjectPropertyDomainAxiom) {
                OWLObjectPropertyDomainAxiom dax = (OWLObjectPropertyDomainAxiom) ax;
                if (dax.getProperty().equals(p)) {
                    ces.add(dax.getDomain());
                }
            }
        }
        return ces;
    }

    public static Set<OWLClassExpression> getPropertyRange(OWLObjectProperty p, OWLOntology o) {
        Set<OWLClassExpression> ces = new HashSet<>();
        //return r.getObjectPropertyDomains(p,true).getFlattened(), ren, p);
        for (OWLAxiom ax : o.getAxioms(AxiomType.OBJECT_PROPERTY_RANGE).stream().collect(Collectors.toSet())) {
            if (ax instanceof OWLObjectPropertyRangeAxiom) {
                OWLObjectPropertyRangeAxiom dax = (OWLObjectPropertyRangeAxiom) ax;
                if (dax.getProperty().equals(p)) {
                    ces.add(dax.getRange());
                }
            }
        }
        return ces;
    }


}
