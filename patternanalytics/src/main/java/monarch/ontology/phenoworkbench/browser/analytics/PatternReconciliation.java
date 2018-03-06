package monarch.ontology.phenoworkbench.browser.analytics;

import monarch.ontology.phenoworkbench.browser.util.RenderManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;

import java.util.HashSet;
import java.util.Set;


public class PatternReconciliation {

    private final Pattern p1;
    private final Pattern p2;
    private final boolean logicallyEquivalent;
    private final boolean syntacticallyEquivalent;
    private final double reconciliationComplexity;
    private final double reconciliationEffect;
    private final LogicalDiff rightDiff;
    private final LogicalDiff leftDiff;

    public PatternReconciliation(Pattern p1, Pattern p2, RenderManager renderManager, Reasoner r) {
        this.p1 = p1;
        this.p2 = p2;
        this.logicallyEquivalent = r.equivalentClasses(p1.getOWLClass(),p2.getOWLClass());
        this.syntacticallyEquivalent = p1.getPatternString().equals(p2.getPatternString()) && !p1.getPatternString().equals("Not given");
        this.rightDiff = new LogicalDiff(p1.getDefiniton(),p2.getDefiniton(),renderManager);
        this.leftDiff = new LogicalDiff(p2.getDefiniton(),p1.getDefiniton(),renderManager);
        Set<OWLClassExpression> union = new HashSet<>();
        union.addAll(p1.getDefiniton().getNestedClassExpressions());
        union.addAll(p2.getDefiniton().getNestedClassExpressions());
        Set<OWLClassExpression> intersection = new HashSet<>();
        intersection.addAll(p1.getDefiniton().getNestedClassExpressions());
        intersection.retainAll(p2.getDefiniton().getNestedClassExpressions());
        this.reconciliationComplexity = java.lang.Math.round((1.0-((double)intersection.size()/(double)union.size()))*100.0)/100.0;
        Set<OWLClass> affectedclasses = new HashSet<>(r.getSubclassesOf(p1.getOWLClass(),false));
        affectedclasses.addAll(r.getSubclassesOf(p2.getOWLClass(),false));
        affectedclasses.removeAll(r.getUnsatisfiableClasses());
        this.reconciliationEffect = affectedclasses.size();
    }

    public double getReconciliationComplexity() {
       return reconciliationComplexity;
    }

    public double getReconciliationEffect() {
        return reconciliationEffect;
    }

    public OWLClassExpression getReconciledPattern() {
        return null;
    }

    public Pattern getP1() {
        return p1;
    }

    public Pattern getP2() {
        return p2;
    }

    public boolean isLogicallyEquivalent() {
        return logicallyEquivalent;
    }

    public boolean isSyntacticallyEquivalent() {
        return syntacticallyEquivalent;
    }

    public LogicalDiff getRightDiff() {
        return rightDiff;
    }

    public LogicalDiff getLeftDiff() {
        return leftDiff;
    }
}
