package monarch.ontology.phenoworkbench.browser.unionanalytics;

import monarch.ontology.phenoworkbench.browser.util.RenderManager;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owl.explanation.api.Explanation;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import java.util.*;

public class ExplantionAnalyserImpl implements ExplanationAnalyser {
    private final Explanation<OWLAxiom> ex;
    private final RenderManager render;
    private final Set<OWLEntity> keyentities;
    private final Map<OWLClass, OWLClassExpression> generated = new HashMap<>();
    private final Set<OWLClass> unsatisfiable = new HashSet<>();

    public ExplantionAnalyserImpl(Explanation<OWLAxiom> ex, Set<OWLEntity> keyentities, RenderManager render) {
        this.ex = ex;
        this.render = render;
        this.keyentities = keyentities;
    }

    @Override
    public List<String> getReport(HashMap<OWLAxiom, Integer> countaxiomsinannotations, Map<OWLAxiom, Set<IRI>> axiomsInOntologies, String s) {
        List<String> sb = new ArrayList();
        try {
            OWLOntologyManager man = OWLManager.createOWLOntologyManager();
            OWLDataFactory df = man.getOWLDataFactory();
            OWLOntology o = man.createOntology(normaliseAxioms(ex.getAxioms()));


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
            unsatisfiable.addAll(r.getUnsatisfiableClasses().getEntitiesMinus(OWLManager.getOWLDataFactory().getOWLNothing()));

            sb.add(s+"* Axioms that impose constraints that might affect satisfiability");
            for (OWLAxiom ax : ex.getAxioms()) {
                if (potentiallyPainfulAxiom(ax)) {
                    sb.add(s+"  * "+renderAxiomForMarkdown(ax));
                }
            }
            sb.add(s+"* Class Hierarchy of Explanation");
            for (OWLClass sub : r.getSubClasses(df.getOWLThing(), true).getFlattened()) {
                if(unsatisfiable.contains(sub)||sub.equals(df.getOWLNothing())) {
                    continue;
                }
                Set<OWLClass> utop = r.getSubClasses(sub,true).getFlattened();
                utop.remove(df.getOWLNothing());
                if(utop.isEmpty()) {
                    continue;
                }

                String repeated = new String(new char[3]).replace("\0", "  ");
                sb.add(repeated+  " * " + render.renderTreeEntity(sub,keyentities,generated,unsatisfiable));
                render.renderTreeForMarkdown(sub, r, sb, 4,keyentities,generated,unsatisfiable);
            }

            sb.add(s+"* Other unsatisfiable classes in explanation: ");

            unsatisfiable.forEach(c->sb.add(s+"  * "+render.renderTreeEntity(c,keyentities,generated,unsatisfiable)));

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
        boolean pain = true;
        if (ax instanceof OWLSubClassOfAxiom) {
            OWLSubClassOfAxiom sbcl = (OWLSubClassOfAxiom) ax;
            boolean harmless = true;
            harmless = isHarmless(sbcl.getSubClass(), harmless);
            if (harmless) { // Check only if still deemed harmless.
                harmless = isHarmless(sbcl.getSubClass(), harmless);
            }
            return !harmless;
        } else if (ax instanceof OWLEquivalentClassesAxiom) {
            boolean harmless = true;
            for (OWLClassExpression ce : ((OWLEquivalentClassesAxiom) ax).getClassExpressionsAsList()) {
                harmless = isHarmless(ce, harmless);
            }
            return !harmless;
        }
        return pain;
    }

    private boolean isHarmless(OWLClassExpression classExpression, boolean harmless) {
        for (OWLClassExpression ce : classExpression.getNestedClassExpressions()) {
            if (ce instanceof OWLClass) {
            } else if (ce instanceof OWLObjectSomeValuesFrom) {
            } else if (ce instanceof OWLObjectIntersectionOf) {
            } else {
                harmless = false;
                break;
            }
        }
        return harmless;
    }



}