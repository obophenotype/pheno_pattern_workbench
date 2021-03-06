package monarch.ontology.phenoworkbench.util;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEntity;

import java.util.*;


public class PatternReconciliationCandidate {

    private final OntologyClass p1;
    private final OntologyClass p2;
    private final String id;
    private Set<OntologyClass> commonAncestors;
    private boolean logicallyEquivalent;
    private boolean p1_sub_p2;
    private boolean p2_sub_p1;
    private boolean syntacticallyEquivalent;
    private boolean grammarEquivalent;
    private double reconciliationComplexity;
    private long reconciliationEffect = -1;
    private double jaccardSimiliarity = -1;
    private double signatureOverlap;
    private String reconciliationclass;
    private Map<String, Double> otherMetrics = new HashMap<>();

    public PatternReconciliationCandidate(OntologyClass p1, OntologyClass p2, Optional<Reasoner> ro) {
        this.p1 = p1;
        this.p2 = p2;
        this.id = p1.getOWLClass().getIRI().toString() + p2.getOWLClass().getIRI().toString();

        ro.ifPresent(r->prepareLogicalEquivalence(p1, p2, r));
        prepareSyntacticEquivalence(p1, p2);
        prepareGrammarEquivalence(p1, p2);
        prepareReconciliationComplexity();
        prepareSignatureOverlap();
        prepareReconciliationClass();
    }

    public PatternReconciliationCandidate(OntologyClass p1, OntologyClass p2) {
        this(p1,p2,Optional.empty());
    }

    private void prepareSignatureOverlap() {
        Set<OWLEntity> union = new HashSet<>();
        if (getP1() instanceof DefinedClass && getP2() instanceof DefinedClass) {
            union.addAll(((DefinedClass) p1).getDefiniton().getSignature());
            union.addAll(((DefinedClass) p2).getDefiniton().getSignature());
            Set<OWLClassExpression> intersection = new HashSet<>(((DefinedClass) p1).getDefiniton().getNestedClassExpressions());
            intersection.retainAll(((DefinedClass) p2).getDefiniton().getNestedClassExpressions());
            setSignatureOverlap(((double) intersection.size() / (double) union.size()));

        } else {
            setSignatureOverlap(0.0);
        }

    }



    private void prepareGrammarEquivalence(OntologyClass p1, OntologyClass p2) {

        if (p1 instanceof DefinedClass && p2 instanceof DefinedClass) {
            this.grammarEquivalent = ((DefinedClass) p1).getGrammar().getGrammarSignature().equals(((DefinedClass) p2).getGrammar().getGrammarSignature()) && !((DefinedClass) p1).getGrammar().getGrammarSignature().equals("none");
        } else {
            this.grammarEquivalent = false;
        }
    }

    private void prepareSyntacticEquivalence(OntologyClass p1, OntologyClass p2) {
        if (p1 instanceof DefinedClass && p2 instanceof DefinedClass) {
            this.syntacticallyEquivalent = ((DefinedClass) p1).getPatternString().equals(((DefinedClass) p2).getPatternString()) && !((DefinedClass) p1).getPatternString().equals("Not given");
        } else {
            this.syntacticallyEquivalent = false;
        }
    }

    private void prepareLogicalEquivalence(OntologyClass p1, OntologyClass p2, Reasoner r) {
        this.logicallyEquivalent = r.equivalentClasses(p1.getOWLClass(), p2.getOWLClass());
        this.p1_sub_p2 = r.getSubclassesOf(p1.getOWLClass(),false,true).contains(p2.getOWLClass());
        this.p2_sub_p1 = r.getSubclassesOf(p2.getOWLClass(),false,true).contains(p1.getOWLClass());
    }

    private void prepareReconciliationClass() {
        List<String> grammars = new ArrayList<>();
        if (getP1() instanceof DefinedClass) {
            grammars.add(((DefinedClass) getP1()).getGrammar().getGrammarSignature());
        }
        if (getP2() instanceof DefinedClass) {
            grammars.add(((DefinedClass) getP2()).getGrammar().getGrammarSignature());
        }
        Collections.sort(grammars);
        reconciliationclass = String.join("", grammars);
    }

    private void prepareReconciliationComplexity() {
        Set<OWLClassExpression> union = new HashSet<>();
        if (getP1() instanceof DefinedClass && getP2() instanceof DefinedClass) {
            union.addAll(((DefinedClass) p1).getDefiniton().getNestedClassExpressions());
            union.addAll(((DefinedClass) p2).getDefiniton().getNestedClassExpressions());
            Set<OWLClassExpression> intersection = new HashSet<>(((DefinedClass) p1).getDefiniton().getNestedClassExpressions());
            intersection.retainAll(((DefinedClass) p2).getDefiniton().getNestedClassExpressions());
            setCommonExpressionRatio(((double) intersection.size() / (double) union.size()));

        } else {
            setCommonExpressionRatio(0.0);
        }

    }

    private void setCommonExpressionRatio(Double complexity) {
        this.reconciliationComplexity = round(complexity, 2);
    }

    private Set<OntologyClass> getMostSpecificAncestors() {
        Timer.start("PatternReconciliationCandidate::getMostSpecificAncestors()");
        Set<OntologyClass> ancestors = new HashSet<>();
        new HashSet<>();

        Timer.start("PatternReconciliationCandidate::getMostSpecificAncestors()::parents");
        Set<Node> commonParents = new HashSet<>(getP1().getNode().indirectParents());
        commonParents.retainAll(getP2().getNode().indirectParents());
        Timer.end("PatternReconciliationCandidate::getMostSpecificAncestors()::parents");

        Timer.start("PatternReconciliationCandidate::getMostSpecificAncestors()::owlclass");
        Set<OWLClass> commonParentsOWL = new HashSet<>();
        commonParents.forEach(n->n.getEquivalenceGroup().forEach(e->commonParentsOWL.add(e.getOWLClass())));
        Timer.end("PatternReconciliationCandidate::getMostSpecificAncestors()::owlclass");

        Timer.start("PatternReconciliationCandidate::getMostSpecificAncestors()::commonparents");

        for (Node c : commonParents) {
            Set<OWLClass> indirectChildren = new HashSet<>();
            c.indirectChildren().forEach(n->n.getEquivalenceGroup().forEach(e->indirectChildren.add(e.getOWLClass())));
            if (indirectChildren.stream().noneMatch(commonParentsOWL::contains)) {
                ancestors.addAll(c.getEquivalenceGroup());
            }
        }
        Timer.end("PatternReconciliationCandidate::getMostSpecificAncestors()::commonparents");

        Timer.end("PatternReconciliationCandidate::getMostSpecificAncestors()");
        return ancestors;
    }

    private double round(double val, int digits) {
        double f = (double) (10 ^ digits);
        return Math.round(val * f) / f;
    }

    public double getReconciliationComplexity() {
        return reconciliationComplexity;
    }

    public long getReconciliationEffect() {
        return reconciliationEffect;
    }

    public OntologyClass getP1() {
        return p1;
    }

    public OntologyClass getP2() {
        return p2;
    }

    public boolean isLogicallyEquivalent() {
        return logicallyEquivalent;
    }

    public boolean isP1SubclassOfP2() {
        return p1_sub_p2;
    }

    public boolean isP2SubclassOfP1() {
        return p2_sub_p1;
    }

    public boolean isSyntacticallyEquivalent() {
        return syntacticallyEquivalent;
    }

    public boolean isGrammarEquivalent() {
        return grammarEquivalent;
    }

    public void setSimiliarity(double jaccardSimiliarity) {
        this.jaccardSimiliarity = round(jaccardSimiliarity, 2);
    }


    public double getSimiliarity() {
        return jaccardSimiliarity;
    }


    public PatternReconciliationCandidate getItself() {
        return this;
    }

    public Set<OntologyClass> getCommonAncestors() {
        if (commonAncestors == null) {
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

    public void setOtherMetrics(Map<String, Double> otherMetrics) {
        this.otherMetrics = otherMetrics;
    }

    public Map<String, Double> getOtherMetrics() {
        return otherMetrics;
    }

    public String getReconciliationID() {
        return StringUtils.lcStripNonAlpha(getP1().getLabel()+"_"+getP2().getLabel());
    }

    public String stringForSearch() {
        String s = getP1().toString();
        s += getP2().toString();
        s+=" " + getP1().getIri() +" " + getP2().getIri();
        return s;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PatternReconciliationCandidate)) return false;
        PatternReconciliationCandidate that = (PatternReconciliationCandidate) o;
        return Objects.equals(p1, that.p1) &&
                Objects.equals(p2, that.p2) &&
                Objects.equals(id, that.id) &&
                Objects.equals(reconciliationclass, that.reconciliationclass);
    }

    @Override
    public int hashCode() {

        return Objects.hash(p1, p2, id, reconciliationclass);
    }

    public boolean isBothDefinitionSet() {
        return ((getP1() instanceof DefinedClass) && (getP2() instanceof DefinedClass));
    }

    public double getSignatureOverlap() {
        return signatureOverlap;
    }

    public void setSignatureOverlap(double signatureOverlap) {
        this.signatureOverlap = signatureOverlap;
    }
}
