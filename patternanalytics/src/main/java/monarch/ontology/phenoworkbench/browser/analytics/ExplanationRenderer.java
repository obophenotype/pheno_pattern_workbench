package monarch.ontology.phenoworkbench.browser.analytics;


import monarch.ontology.phenoworkbench.browser.util.RenderManager;
import org.semanticweb.owlapi.model.OWLAxiom;

public class ExplanationRenderer {
    private final RenderManager renderManager ;
    ExplanationRenderer(RenderManager renderManager) {
        this.renderManager = renderManager;
    }
    public String renderExplanation(Explanation expl) {
        StringBuilder sb = new StringBuilder();
        for(OWLAxiom ax:expl.getAxioms()) {
            sb.append(renderManager.renderForMarkdown(ax));
        }
        return sb.toString();
    }
}
