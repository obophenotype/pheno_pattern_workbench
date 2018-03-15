package monarch.ontology.phenoworkbench.browser.analytics;

import monarch.ontology.phenoworkbench.browser.util.InferredOntologyGenerator;
import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owl.explanation.api.ExplanationGenerator;
import org.semanticweb.owl.explanation.api.ExplanationGeneratorFactory;
import org.semanticweb.owl.explanation.api.ExplanationManager;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.util.InferredAxiomGenerator;
import org.semanticweb.owlapi.util.InferredEquivalentClassAxiomGenerator;
import org.semanticweb.owlapi.util.InferredIndividualAxiomGenerator;
import org.semanticweb.owlapi.util.InferredSubClassAxiomGenerator;

import java.util.*;

public class Reasoner {
    private OWLReasoner r;
    private OWLReasoner tautologyreasoner;
    private final boolean realtautologies;

    private OWLOntology o;
    private OWLDataFactory df = OWLManager.getOWLDataFactory();
    private OWLReasonerFactory rf = new ElkReasonerFactory();

    private Set<OWLAxiom> inferredSubclassOfAxioms = new HashSet<>();
    private Set<OWLClass> unsatisfiableClasses = new HashSet<>();
    private final ExplanationGeneratorFactory<OWLAxiom> genFac = ExplanationManager.createExplanationGeneratorFactory(rf);
    private final ExplanationGenerator<OWLAxiom> gen;


    Reasoner(OWLOntology o)  {
        this(o,true);
    }

    Reasoner(OWLOntology o, boolean realtautologies) {
        r = rf.createReasoner(o);
        r.precomputeInferences(InferenceType.CLASS_HIERARCHY);
        unsatisfiableClasses.addAll(r.getUnsatisfiableClasses().getEntities());
        try {
            tautologyreasoner =new ReasonerFactory().createReasoner(OWLManager.createOWLOntologyManager().createOntology(IRI.create("ebi:test")));
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }
        this.realtautologies = realtautologies;
        this.o = o;
        this.gen = genFac.createExplanationGenerator(this.o);
    }

    public Set<OWLAxiom> getInferredSubclassOfAxioms() {
        if(inferredSubclassOfAxioms.isEmpty()) {
            createTransitiveClosureOfSubclassAxiom();
        }
        return inferredSubclassOfAxioms;
    }

    private void createTransitiveClosureOfSubclassAxiom() {
        Set<OWLClass> signature = new HashSet<>(o.getClassesInSignature());
        signature.add(df.getOWLThing());
        signature.add(df.getOWLNothing());
        r.flush();
        inferredSubclassOfAxioms.clear();
        int i = 0;
        int all = signature.size();

        for(OWLClass c_sub:signature) {
            i++;
            if (i % 1000 == 0) {
                System.out.println(i + "/" + all);
            }
            for (OWLClass c_super : r.getSuperClasses(c_sub, false).getFlattened()) {

                //g.addEdge(vertexmapping.get(a),vertexmapping.get(superClass));
                OWLSubClassOfAxiom sbcl = df.getOWLSubClassOfAxiom(c_sub, c_super);
                if (!isTautology(sbcl)) {
                    inferredSubclassOfAxioms.add(sbcl);
                }
            }
        }

    }

    public void createInferredHierarchy(OWLOntology o) {
        r.flush();
        List<InferredAxiomGenerator<? extends OWLAxiom>> generators = new ArrayList<>();
        generators.add(new InferredSubClassAxiomGenerator());
        //generators.add(new InferredClassAssertionAxiomGenerator());
        generators.add(new InferredEquivalentClassAxiomGenerator());
        //generators.add(new InferredDisjointClassesAxiomGenerator());

        List<InferredIndividualAxiomGenerator<? extends OWLIndividualAxiom>> individualAxioms =
                new ArrayList<>();
        generators.addAll(individualAxioms);

        InferredOntologyGenerator iog = new InferredOntologyGenerator(r, generators);
        iog.fillOntology(o.getOWLOntologyManager().getOWLDataFactory(), o);
        //inferredSubclassOfAxioms.addAll(o.getAxioms());
    }

    public OWLReasoner getOWLReasoner() {
        return r;
    }

    private boolean isTautology(OWLAxiom ax) {
        if(ax instanceof OWLSubClassOfAxiom) {
            if(realtautologies) {
                return tautologyreasoner.isEntailed(ax);
            } else {
                if(((OWLSubClassOfAxiom)ax).getSuperClass().equals(df.getOWLThing())) {
                    return true;
                }
            }

        }
        return false;
    }

    /*
    TODO
    This method tries to approximate the notion of "asserted subsumptions". Many other interpretations are possible.
    We need a definition that is as close to asserted as possible, therefore, we consider (Letters only names)
    A = B -> A sub B, B sub A
    A sub B -> A sub B
    A sub (B and C) -> A sub B, A sub C

    We ignore, for example:
    A or B sub C -> A sub C, B sub C
    A or B sub C and D
    because it wont be clear if a one of the resulting axioms is redundant, that all are.
     */
    public Set<OWLAxiom> getAssertedSubclassOfAxioms() {
        return getAssertedSubclassOfAxioms(new HashSet<>());
    }

    public Set<OWLAxiom> getAssertedSubclassOfAxioms(Set<OWLClass> branch) {
        Set<OWLAxiom> sbcl = new HashSet<>();
        for(OWLAxiom ax:o.getAxioms(Imports.EXCLUDED)) {
            if(ax instanceof OWLSubClassOfAxiom) {
                OWLSubClassOfAxiom sax = ((OWLSubClassOfAxiom)ax);
                processSubClassAxiom(sbcl, sax,branch);

            } else if(ax instanceof OWLEquivalentClassesAxiom) {
                for(OWLSubClassOfAxiom sax:((OWLEquivalentClassesAxiom)ax).asOWLSubClassOfAxioms()) {
                    processSubClassAxiom(sbcl, sax,branch);
                }
            }
        }
        return sbcl;
    }

    private void processSubClassAxiom(Set<OWLAxiom> sbcl, OWLSubClassOfAxiom sax, Set<OWLClass> branch) {
        OWLClassExpression rhs = sax.getSuperClass();
        OWLClassExpression lhs = sax.getSubClass();

        // If there is a branch, and the subclass is not in that branch, do not add the axiom
        if(!branch.isEmpty()) {
            if(lhs.isClassExpressionLiteral()) {
                if(!branch.contains(lhs.asOWLClass())) {
                    return;
                }
            }
        }

        if(rhs.isClassExpressionLiteral()) {
            if(lhs.isClassExpressionLiteral()) {
                // A Sub B
                sbcl.add(sax);
            }
        } else if(rhs instanceof OWLObjectIntersectionOf) {
            if(lhs.isClassExpressionLiteral()) {
                for(OWLClassExpression ce:(((OWLObjectIntersectionOf)rhs).getOperands())){
                    if(ce.isClassExpressionLiteral()) {
                        sbcl.add(df.getOWLSubClassOfAxiom(sax.getSubClass(),ce));
                    }
                }
            }
        }
    }

    public Set<OWLClass> getUnsatisfiableClasses() {
        return unsatisfiableClasses;
    }

    public Collection<? extends OWLClass> getSubclassesOf(OWLClass c, boolean direct) {
        Set<OWLClass> sbcl = r.getSubClasses(c,direct).getFlattened();
        sbcl.remove(c);
        sbcl.remove(df.getOWLNothing());
        return sbcl;
    }

    public Collection<? extends OWLClass> getSuperClassesOf(OWLClass c, boolean direct) {
        Set<OWLClass> sbcl = r.getSuperClasses(c,direct).getFlattened();
        sbcl.remove(c);
        sbcl.remove(df.getOWLThing());
        return sbcl;
    }


    public boolean equivalentClasses(OWLClass c1, OWLClass c2) {
        return r.getEquivalentClasses(c1).contains(c2);
    }

    public Explanation getExplanation(OWLClass c_sub, OWLClass c_super) {
        OWLAxiom entailment = df.getOWLSubClassOfAxiom(c_sub, c_super);
        Set<org.semanticweb.owl.explanation.api.Explanation<OWLAxiom>> expl = gen.getExplanations(entailment, 1);
        if(expl.isEmpty()) {
            return new Explanation(new HashSet<>());
        }
        return new Explanation(expl.stream().findFirst().get().getAxioms());
    }
}
