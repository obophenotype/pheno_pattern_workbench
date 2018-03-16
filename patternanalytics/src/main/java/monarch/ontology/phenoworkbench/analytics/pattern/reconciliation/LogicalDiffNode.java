package monarch.ontology.phenoworkbench.analytics.pattern.reconciliation;

import org.semanticweb.owlapi.model.OWLClassExpression;

import java.util.HashSet;
import java.util.Set;

public class LogicalDiffNode {
    Set<OWLClassExpression> diffnode = new HashSet<>();
    public LogicalDiffNode(Set<OWLClassExpression> diffnode) {
        diffnode.addAll(diffnode);
    }
}
