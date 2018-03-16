package monarch.ontology.phenoworkbench.analytics.pattern.reconciliation;

import monarch.ontology.phenoworkbench.util.RenderManager;
import org.semanticweb.owlapi.model.OWLClassExpression;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LogicalDiff {
    Map<LogicalDiffNode, Set<LogicalDiffNode>> instantiation1 = new HashMap<>();
    Map<LogicalDiffNode, Set<LogicalDiffNode>> instantiation2 = new HashMap<>();
    RenderManager render;

    Set<OWLClassExpression> nestedadd = new HashSet<>();
    Set<OWLClassExpression> nestedrem = new HashSet<>();

    public LogicalDiff(OWLClassExpression ce1, OWLClassExpression ce2, RenderManager render) {
        this.render = render;
        nestedadd.addAll(ce1.getNestedClassExpressions());
        nestedadd.removeAll(ce2.getNestedClassExpressions());
        nestedadd.remove(ce1);

        nestedrem.addAll(ce2.getNestedClassExpressions());
        nestedrem.removeAll(ce1.getNestedClassExpressions());
        nestedrem.remove(ce2);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Additional nested expressions: \n");
        for (OWLClassExpression ce : nestedadd) {
            sb.append(render.renderForMarkdown(ce) + "\n");
        }

        sb.append("Removed nested expressions: \n");
        for (OWLClassExpression ce : nestedrem) {
            sb.append(render.renderForMarkdown(ce) + "\n");
        }
        return sb.toString();
    }
}
