package monarch.ontology.phenoworkbench.analytics.pattern.reconciliation;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

public class ReconciliationCandidateSet {
    private final Collection<PatternReconciliationCandidate> pcrs;
    private float percentageReconciledGrammar = -1.0f;
    private float percentageReconciledSyntax = -1.0f;
    private float percentageReconciledLogical = -1.0f;

    ReconciliationCandidateSet(Collection<PatternReconciliationCandidate> pcrs) {
        this.pcrs = new HashSet<>(pcrs);
    }

    public Optional<PatternReconciliationCandidate> getClosestMatchCandidate() {
        double max = 0;
        PatternReconciliationCandidate p = null;
        for(PatternReconciliationCandidate pcr:pcrs) {
            double sim = pcr.getJaccardSimiliarity();
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
}
