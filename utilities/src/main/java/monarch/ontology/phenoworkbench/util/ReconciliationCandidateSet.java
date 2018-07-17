package monarch.ontology.phenoworkbench.util;

import java.util.*;
import java.util.function.Predicate;

public class ReconciliationCandidateSet {
    private final Collection<PatternReconciliationCandidate> pcrs;
    private float percentageReconciledGrammar = -1.0f;
    private float percentageReconciledSyntax = -1.0f;
    private float percentageReconciledLogical = -1.0f;
    private long maxReconciliationImpact = 1;
    private List<CandidateChangeListener> changeListeners = new ArrayList<>();

    public ReconciliationCandidateSet() {
        this(new HashSet<>());
    }
    public ReconciliationCandidateSet(Collection<PatternReconciliationCandidate> pcrs) {
        this.pcrs = new HashSet<>(pcrs);
        for(PatternReconciliationCandidate pr:pcrs) {
            if(pr.getReconciliationEffect()>maxReconciliationImpact) {
                maxReconciliationImpact = pr.getReconciliationEffect();
            }
        }
    }

    public void addCandidate(PatternReconciliationCandidate c) {
        pcrs.add(c);
        if(c.getReconciliationEffect()>maxReconciliationImpact) {
            maxReconciliationImpact = c.getReconciliationEffect();
        }
        changed();
    }
    public Optional<PatternReconciliationCandidate> getClosestMatchCandidate() {
        double max = 0;
        PatternReconciliationCandidate p = null;
        for(PatternReconciliationCandidate pcr:pcrs) {
            double sim = pcr.getSimiliarity();
            if(sim>max) {
                max = sim;
                p = pcr;
            }
        }
        return Optional.ofNullable(p);
    }

    public Collection<PatternReconciliationCandidate> items() {
        return pcrs;
    }

    public float getPercentageReconciledGrammarEquivalence() {
        if(percentageReconciledGrammar<0) {
            percentageReconciledGrammar = calculateReconciledPercentage(PatternReconciliationCandidate::isGrammarEquivalent);
        }
        return percentageReconciledGrammar;
    }

    private float calculateReconciledPercentage(Predicate<PatternReconciliationCandidate> pred) {
        long ct = pcrs.stream().filter(pred).count();
        long all = size();
        return all>0 ? (float)ct/(float)all : 0.0f;
    }

    public float getPercentageReconciledSyntaxEquivalence() {
        if(percentageReconciledSyntax<0) {
            percentageReconciledSyntax = calculateReconciledPercentage(PatternReconciliationCandidate::isSyntacticallyEquivalent);
        }
        return percentageReconciledSyntax;
    }

    public float getPercentageReconciledLogicalEquivalence() {
        if(percentageReconciledLogical<0) {
            percentageReconciledLogical = calculateReconciledPercentage(PatternReconciliationCandidate::isLogicallyEquivalent);
        }
        return percentageReconciledLogical;
    }

    public long size() {
        return pcrs.size();
    }

    public ReconciliationCandidateSet union(ReconciliationCandidateSet s2) {
        Set<PatternReconciliationCandidate> candidateSet = new HashSet<>(items());
        candidateSet.addAll(s2.items());
        return new ReconciliationCandidateSet(candidateSet);
    }

    public long getMaxReconciliationImpact() {
        return maxReconciliationImpact;
    }

    public void addCandidateChangeListener(CandidateChangeListener listener) {
        changeListeners.add(listener);
    }

    private void changed() {
        changeListeners.forEach(c->c.changed());
    }


    public void removeCandidateChangeListener(CandidateChangeListener listener) {
        changeListeners.remove(listener);
    }

    public void removeCandidates(Set<PatternReconciliationCandidate> remove) {
        pcrs.removeAll(remove);
        changed();
    }

    public void addCandidates(Set<PatternReconciliationCandidate> add) {
        pcrs.addAll(add);
        changed();
    }

    public boolean containsReconciliationCandidate(PatternReconciliationCandidate s) {
        return pcrs.contains(s);
    }

    public void removeCandidate(PatternReconciliationCandidate c) {
        pcrs.remove(c);
        changed();
    }

    public interface CandidateChangeListener {
        void changed();
    }
}
