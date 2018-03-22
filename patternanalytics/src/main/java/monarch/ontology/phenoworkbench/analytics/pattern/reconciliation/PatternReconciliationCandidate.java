package monarch.ontology.phenoworkbench.analytics.pattern.reconciliation;

import monarch.ontology.phenoworkbench.analytics.pattern.generation.DefinedClass;
import monarch.ontology.phenoworkbench.analytics.pattern.generation.OntologyClass;
import monarch.ontology.phenoworkbench.analytics.pattern.generation.PatternClass;
import monarch.ontology.phenoworkbench.util.Reasoner;
import monarch.ontology.phenoworkbench.util.RenderManager;
import monarch.ontology.phenoworkbench.util.Timer;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.parameters.Imports;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;


public class PatternReconciliationCandidate {

    private final DefinedClass p1;
    private final DefinedClass p2;
    private Set<OntologyClass> commonAncestors;
    private final boolean logicallyEquivalent;
    private final boolean syntacticallyEquivalent;
    private final boolean grammarEquivalent;
    private final double reconciliationComplexity;
    private long reconciliationEffect = -1;
    private double jaccardSimiliarity = -1;
    private double subclassSimilarity = -1;
    private final LogicalDiff rightDiff;
    private final LogicalDiff leftDiff;
    private final String reconciliationclass;

    PatternReconciliationCandidate(DefinedClass p1, DefinedClass p2, RenderManager renderManager, Reasoner r) {
        this.p1 = p1;
        this.p2 = p2;
        this.logicallyEquivalent = r.equivalentClasses(p1.getOWLClass(),p2.getOWLClass());
        this.syntacticallyEquivalent = p1.getPatternString().equals(p2.getPatternString()) && !p1.getPatternString().equals("Not given");
        this.grammarEquivalent = p1.getGrammar().getGrammarSignature().equals(p2.getGrammar().getGrammarSignature()) && !p1.getGrammar().getGrammarSignature().equals("none");
        this.rightDiff = new LogicalDiff(p1.getDefiniton(),p2.getDefiniton(),renderManager);
        this.leftDiff = new LogicalDiff(p2.getDefiniton(),p1.getDefiniton(),renderManager);
        DecimalFormat df = new DecimalFormat("#.####");
        df.setRoundingMode(RoundingMode.HALF_UP);
        Set<OWLClassExpression> union = new HashSet<>();
        union.addAll(p1.getDefiniton().getNestedClassExpressions());
        union.addAll(p2.getDefiniton().getNestedClassExpressions());
        Set<OWLClassExpression> intersection = new HashSet<>();
        intersection.addAll(p1.getDefiniton().getNestedClassExpressions());
        intersection.retainAll(p2.getDefiniton().getNestedClassExpressions());

        this.reconciliationComplexity = round(1.0-((double)intersection.size()/(double)union.size()),2);
        Set<OWLClass> affectedclasses = new HashSet<>(r.getSubclassesOf(p1.getOWLClass(),false));
        affectedclasses.addAll(r.getSubclassesOf(p2.getOWLClass(),false));
        affectedclasses.removeAll(r.getUnsatisfiableClasses());
        int all_cl = r.getOWLReasoner().getRootOntology().getClassesInSignature(Imports.INCLUDED).size();
        List<String> grammars = new ArrayList();
        grammars.add(getP1().getGrammar().getGrammarSignature());
        grammars.add(getP2().getGrammar().getGrammarSignature());
        Collections.sort(grammars);
        reconciliationclass = String.join("",grammars);
    }

    private Set<OntologyClass> getMostSpecificAncestors() {
        Timer.start("PatternReconciliationCandidate::getMostSpecificAncestors()");
        Set<OntologyClass> ancestors = new HashSet<>();
        new HashSet<>();

        Timer.start("PatternReconciliationCandidate::getMostSpecificAncestors()::parents");
        Set<OntologyClass> commonParents = new HashSet(getP1().indirectParents());
        commonParents.retainAll(getP2().indirectParents());
        Timer.end("PatternReconciliationCandidate::getMostSpecificAncestors()::parents");

        Timer.start("PatternReconciliationCandidate::getMostSpecificAncestors()::owlclass");
        Set<OWLClass> commonParentsOWL = commonParents.stream().map(OntologyClass::getOWLClass).collect(Collectors.toSet());
        Timer.end("PatternReconciliationCandidate::getMostSpecificAncestors()::owlclass");

        Timer.start("PatternReconciliationCandidate::getMostSpecificAncestors()::commonparents");

        for (OntologyClass c : commonParents) {
            if (c.indirectChildren().stream().noneMatch(child -> commonParentsOWL.contains(child.getOWLClass()))) {
                ancestors.add(c);
            }
        }
        Timer.end("PatternReconciliationCandidate::getMostSpecificAncestors()::commonparents");

        Timer.end("PatternReconciliationCandidate::getMostSpecificAncestors()");
        return ancestors;
    }

    private double round(double val,int digits) {
        double f = (double)(10^digits);
        return Math.round (val * f) / f;
    }

    public double getReconciliationComplexity() {
       return reconciliationComplexity;
    }

    public long getReconciliationEffect() {
        return reconciliationEffect;
    }

    public DefinedClass getP1() {
        return p1;
    }

    public DefinedClass getP2() {
        return p2;
    }

    public boolean isLogicallyEquivalent() {
        return logicallyEquivalent;
    }

    public boolean isSyntacticallyEquivalent() {
        return syntacticallyEquivalent;
    }

    public boolean isGrammarEquivalent() {
        return grammarEquivalent;
    }

    public LogicalDiff getRightDiff() {
        return rightDiff;
    }

    public LogicalDiff getLeftDiff() {
        return leftDiff;
    }

    public void setJaccardSimiliarity(double jaccardSimiliarity) {
        this.jaccardSimiliarity = round(jaccardSimiliarity,2);
    }
    public void setSubclassSimilarity(double subclassSimilarity) {
        this.subclassSimilarity = round(subclassSimilarity,2);
    }

    public double getJaccardSimiliarity() {
        return jaccardSimiliarity;
    }

    public double getSubclassSimilarity() {
        return subclassSimilarity;
    }

    public PatternReconciliationCandidate getItself() {
        return this;
    }

    public Set<OntologyClass> getCommonAncestors() {
        if(commonAncestors==null){
            commonAncestors = getMostSpecificAncestors();
        }
        return commonAncestors;
    }

    public void setReconciliationEffect(long reconciliationEffect) {
        this.reconciliationEffect = reconciliationEffect;
    }

    public String getReconciliationclass() {
        return reconciliationclass;
    }
}
