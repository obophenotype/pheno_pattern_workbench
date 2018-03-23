package monarch.ontology.phenoworkbench.analytics.pattern.reconciliation;

import monarch.ontology.phenoworkbench.analytics.pattern.generation.DefinedClass;
import monarch.ontology.phenoworkbench.analytics.pattern.generation.OntologyClass;
import monarch.ontology.phenoworkbench.util.Reasoner;
import monarch.ontology.phenoworkbench.util.RenderManager;
import monarch.ontology.phenoworkbench.util.Timer;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;

import java.util.*;
import java.util.stream.Collectors;


public class PatternReconciliationCandidate {

    private final DefinedClass p1;
    private final DefinedClass p2;
    private final String id;
    private Set<OntologyClass> commonAncestors;
    private boolean logicallyEquivalent;
    private boolean syntacticallyEquivalent;
    private boolean grammarEquivalent;
    private double reconciliationComplexity;
    private long reconciliationEffect = -1;
    private double jaccardSimiliarity = -1;
    private double subclassSimilarity = -1;
    private String reconciliationclass;

    PatternReconciliationCandidate(DefinedClass p1, DefinedClass p2, RenderManager renderManager, Reasoner r) {
        this.p1 = p1;
        this.p2 = p2;
        this.id = p1.getOWLClass().getIRI().toString()+p2.getOWLClass().getIRI().toString();
        prepareLogicalEquivalence(p1, p2, r);
        prepareSyntacticEquivalence(p1, p2);
        prepareGrammarEquivalence(p1, p2);
        //this.rightDiff = new LogicalDiff(p1.getDefiniton(),p2.getDefiniton(),renderManager);
        //this.leftDiff = new LogicalDiff(p2.getDefiniton(),p1.getDefiniton(),renderManager);
        prepareReconciliationComplexity();
        prepareReconciliationClass();
    }

    private void prepareGrammarEquivalence(DefinedClass p1, DefinedClass p2) {
        this.grammarEquivalent = p1.getGrammar().getGrammarSignature().equals(p2.getGrammar().getGrammarSignature()) && !p1.getGrammar().getGrammarSignature().equals("none");
    }

    private void prepareSyntacticEquivalence(DefinedClass p1, DefinedClass p2) {
        this.syntacticallyEquivalent = p1.getPatternString().equals(p2.getPatternString()) && !p1.getPatternString().equals("Not given");
    }

    private void prepareLogicalEquivalence(DefinedClass p1, DefinedClass p2, Reasoner r) {
        this.logicallyEquivalent = r.equivalentClasses(p1.getOWLClass(),p2.getOWLClass());
    }

    private void prepareReconciliationClass() {
        List<String> grammars = new ArrayList<>();
        grammars.add(getP1().getGrammar().getGrammarSignature());
        grammars.add(getP2().getGrammar().getGrammarSignature());
        Collections.sort(grammars);
        reconciliationclass = String.join("",grammars);
    }

    private void prepareReconciliationComplexity() {
        Set<OWLClassExpression> union = new HashSet<>();
        union.addAll(p1.getDefiniton().getNestedClassExpressions());
        union.addAll(p2.getDefiniton().getNestedClassExpressions());
        Set<OWLClassExpression> intersection = new HashSet<>(p1.getDefiniton().getNestedClassExpressions());
        intersection.retainAll(p2.getDefiniton().getNestedClassExpressions());

        this.reconciliationComplexity = round(1.0-((double)intersection.size()/(double)union.size()),2);
    }

    private Set<OntologyClass> getMostSpecificAncestors() {
        Timer.start("PatternReconciliationCandidate::getMostSpecificAncestors()");
        Set<OntologyClass> ancestors = new HashSet<>();
        new HashSet<>();

        Timer.start("PatternReconciliationCandidate::getMostSpecificAncestors()::parents");
        Set<OntologyClass> commonParents = new HashSet<>(getP1().indirectParents());
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PatternReconciliationCandidate)) return false;
        PatternReconciliationCandidate that = (PatternReconciliationCandidate) o;
        return logicallyEquivalent == that.logicallyEquivalent &&
                syntacticallyEquivalent == that.syntacticallyEquivalent &&
                grammarEquivalent == that.grammarEquivalent &&
                Double.compare(that.reconciliationComplexity, reconciliationComplexity) == 0 &&
                reconciliationEffect == that.reconciliationEffect &&
                Double.compare(that.jaccardSimiliarity, jaccardSimiliarity) == 0 &&
                Double.compare(that.subclassSimilarity, subclassSimilarity) == 0 &&
                Objects.equals(p1, that.p1) &&
                Objects.equals(p2, that.p2) &&
                Objects.equals(id, that.id) &&
                Objects.equals(commonAncestors, that.commonAncestors) &&
                Objects.equals(reconciliationclass, that.reconciliationclass);
    }

    @Override
    public int hashCode() {

        return Objects.hash(p1, p2, id, commonAncestors, logicallyEquivalent, syntacticallyEquivalent, grammarEquivalent, reconciliationComplexity, reconciliationEffect, jaccardSimiliarity, subclassSimilarity, reconciliationclass);
    }
}
