package monarch.ontology.phenoworkbench.browser.reconciliation;

import monarch.ontology.phenoworkbench.util.PatternReconciliationCandidate;

public interface ReconciliationCandidateFunction {
    void handle(PatternReconciliationCandidate canidate);
}
