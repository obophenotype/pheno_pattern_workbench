package monarch.ontology.phenoworkbench.util;

import java.util.*;
import java.util.function.Predicate;

public class ReconciliationCandidateSet {
    private final Collection<PatternReconciliationCandidate> pcrs = new HashSet<>();
    private List<CandidateChangeListener> changeListeners = new ArrayList<>();

    public ReconciliationCandidateSet() {
        this(new HashSet<>());
    }
    public ReconciliationCandidateSet(Collection<PatternReconciliationCandidate> pcrs) {
       addCandidates(pcrs);
    }

    public interface CandidateChangeListener {
        void changed();
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
        return new HashSet(pcrs);
    }

    public float getPercentageReconciledGrammarEquivalence() {
        return calculateReconciledPercentage(PatternReconciliationCandidate::isGrammarEquivalent);
    }

    private float calculateReconciledPercentage(Predicate<PatternReconciliationCandidate> pred) {
        long ct = pcrs.stream().filter(pred).count();
        long all = size();
        return all>0 ? (float)ct/(float)all : 0.0f;
    }

    public float getPercentageReconciledSyntaxEquivalence() {
        return calculateReconciledPercentage(PatternReconciliationCandidate::isSyntacticallyEquivalent);
    }

    public float getPercentageReconciledLogicalEquivalence() {
        return calculateReconciledPercentage(PatternReconciliationCandidate::isLogicallyEquivalent);
    }

    public long getMaxReconciliationImpact() {
        return pcrs.stream().mapToLong(PatternReconciliationCandidate::getReconciliationEffect).max().orElse(0);
    }

    public int size() {
        return pcrs.size();
    }

    public ReconciliationCandidateSet union(ReconciliationCandidateSet s2) {
        Set<PatternReconciliationCandidate> candidateSet = new HashSet<>(items());
        candidateSet.addAll(s2.items());
        return new ReconciliationCandidateSet(candidateSet);
    }

    public void addCandidateChangeListener(CandidateChangeListener listener) {
        changeListeners.add(listener);
    }

    public void removeCandidateChangeListener(CandidateChangeListener listener) {
        changeListeners.remove(listener);
    }

    private void changed() {
        changeListeners.forEach(c->c.changed());
    }




    public void removeCandidate(PatternReconciliationCandidate c) {
        pcrs.remove(c);
        changed();
    }

    public void removeCandidates(Set<PatternReconciliationCandidate> remove) {
        pcrs.removeAll(remove);
        changed();
    }

    public void addCandidate(PatternReconciliationCandidate c) {
        pcrs.add(c);
        changed();
    }

    public void addCandidates(Collection<PatternReconciliationCandidate> add) {
        pcrs.addAll(add);
        changed();
    }

    public boolean containsReconciliationCandidate(PatternReconciliationCandidate s) {
        return pcrs.contains(s);
    }



}
