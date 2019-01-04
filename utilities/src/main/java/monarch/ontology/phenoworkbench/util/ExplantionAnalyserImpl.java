package monarch.ontology.phenoworkbench.util;

import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasoner;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;

import java.util.*;

public class ExplantionAnalyserImpl implements ExplanationAnalyser {
    private final Explanation ex;
    private final RenderManager render;
    private final Set<OWLEntity> keyentities;
    private final Map<OWLClass, OWLClassExpression> generated = new HashMap<>();
    private final Set<OWLClass> unsatisfiable = new HashSet<>();

    public ExplantionAnalyserImpl(Explanation ex, Set<OWLEntity> keyentities, RenderManager render) {
        this.ex = ex;
        this.render = render;
        this.keyentities = keyentities;
    }

    @Override
    public List<String> getReport(int indendationlevel) {
        List<String> sb = new ArrayList();
        try {
            OWLOntologyManager man = OWLManager.createOWLOntologyManager();
            OWLDataFactory df = man.getOWLDataFactory();
            OWLOntology o = man.createOntology(ex.getAxioms());


            String base = "http://tmp.owl#";


            Set<OWLClassExpression> anonymous = new HashSet<>();
            o.getLogicalAxioms().forEach(a -> anonymous.addAll(a.getNestedClassExpressions()));
            int ct = 0;
            for (OWLClassExpression ce : anonymous) {

                if (ce.isAnonymous()) {
                    OWLClass cnew = df.getOWLClass(IRI.create(base + "X" + ct));
                    man.addAxiom(o, df.getOWLEquivalentClassesAxiom(cnew, ce));
                    generated.put(cnew, ce);
                    ct++;
                }
            }

            OWLReasoner r = new ElkReasonerFactory().createReasoner(o);
            OWLReasoner r_struct = new StructuralReasonerFactory().createReasoner(o);
            r_struct.precomputeInferences(InferenceType.CLASS_HIERARCHY);
            unsatisfiable.addAll(r.getUnsatisfiableClasses().getEntitiesMinus(OWLManager.getOWLDataFactory().getOWLNothing()));

            String prefixed_whitespace = new String(new char[indendationlevel]).replace("\0", "  ");

            sb.add(prefixed_whitespace+"* Axioms that impose constraints that might affect satisfiability");
            for (OWLAxiom ax : ex.getAxioms()) {
                if (potentiallyPainfulAxiom(ax)) {
                    sb.add(prefixed_whitespace+"  * "+renderAxiomForMarkdown(ax));
                }
            }
            sb.add(prefixed_whitespace+"* Class Hierarchy of Explanation");
            for (OWLClass sub : r.getSubClasses(df.getOWLThing(), true).getFlattened()) {
                if(sub.equals(df.getOWLNothing())) {
                    continue;
                }
                /*Set<OWLClass> utop = r_struct.getSubClasses(sub,true).getFlattened();
                utop.remove(df.getOWLNothing());
                utop.remove(keyentities);
                if(utop.isEmpty()) {
                    continue;
                }*/


                sb.add(new String(new char[indendationlevel+1]).replace("\0", "  ")+  "  * " + render.renderTreeEntity(sub,keyentities,generated,unsatisfiable));
                render.renderTreeForMarkdown(sub, r_struct, sb, indendationlevel+3,keyentities,generated,unsatisfiable);
            }

            sb.add(prefixed_whitespace+"* Other unsatisfiable classes in explanation: ");

            unsatisfiable.forEach(c->sb.add(prefixed_whitespace+"  * "+render.renderTreeEntity(c,keyentities,generated,unsatisfiable)));

            //sb.append(LINEBREAK);
            //sb.append("Named anonymous expressions" + LINEBREAK);
            //generated.keySet().forEach(k -> sb.append(render.getLabel(k) + "= " + render
            //       .render(generated.get(k)) + "\n"));

        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }
        //OntologyUtils.p(sb);
        return sb;
    }

    public List<String> getRenderedAxiomList() {
        List<String> axiomList = new ArrayList<>();
        for(OWLAxiom ax : ex.getAxioms()) {
            axiomList.add(render.renderForMarkdown(ax));
        }
        return axiomList;
    }

    private String renderAxiomForMarkdown(OWLObject ax) {
        return render.renderManchester(ax).replaceAll("(\n|\r\n|\r)","  \n");
    }

    private Set<OWLAxiom> normaliseAxioms(Set<OWLAxiom> axioms) {
        // Convert disjoint and equivalent class axioms into subclass axioms
        Set<OWLAxiom> normalised = new HashSet<>();
        for (OWLAxiom ax : axioms) {
            if (ax instanceof OWLEquivalentClassesAxiom) {
                normalised.addAll(((OWLEquivalentClassesAxiom) ax).asOWLSubClassOfAxioms());
            } else if (ax instanceof OWLDisjointClassesAxiom) {
                normalised.addAll(((OWLDisjointClassesAxiom) ax).asOWLSubClassOfAxioms());
            } else if (ax instanceof OWLDisjointUnionAxiom) {
                //TODO is this correct?
                normalised.addAll(((OWLDisjointUnionAxiom) ax).getOWLDisjointClassesAxiom().asOWLSubClassOfAxioms());
                normalised.addAll(((OWLDisjointUnionAxiom) ax).getOWLEquivalentClassesAxiom().asOWLSubClassOfAxioms());
            } else {
                normalised.add(ax);
            }

        }
        return normalised;
    }

    private boolean potentiallyPainfulAxiom(OWLAxiom ax) {
        if (ax instanceof OWLSubClassOfAxiom) {
            OWLSubClassOfAxiom sbcl = (OWLSubClassOfAxiom) ax;
            return !isHarmless(sbcl.getSubClass())||!isHarmless(sbcl.getSuperClass());
        } else if (ax instanceof OWLEquivalentClassesAxiom) {
            for (OWLClassExpression ce : ((OWLEquivalentClassesAxiom) ax).getClassExpressionsAsList()) {
                if(!isHarmless(ce)) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    private boolean isHarmless(OWLClassExpression classExpression) {
        for (OWLClassExpression ce : classExpression.getNestedClassExpressions()) {
            if (ce instanceof OWLClass) {
            } else if (ce instanceof OWLObjectSomeValuesFrom) {
            } else if (ce instanceof OWLObjectIntersectionOf) {
            } else {
               return false;
            }
        }
        return true;
    }



}
