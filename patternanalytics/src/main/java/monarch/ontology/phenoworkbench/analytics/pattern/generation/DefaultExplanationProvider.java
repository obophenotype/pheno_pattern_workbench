package monarch.ontology.phenoworkbench.analytics.pattern.generation;

import monarch.ontology.phenoworkbench.util.*;

import java.util.HashSet;
import java.util.Optional;

public class DefaultExplanationProvider implements ExplanationRenderProvider {

    private final Reasoner r;
    private final RenderManager renderManager;

    public DefaultExplanationProvider(Reasoner r,RenderManager renderManager) {
        this.r = r;
        this.renderManager = renderManager;
    }

    private Optional<Explanation> getSubsumptionExplanation(OntologyClass c, OntologyClass p) {
        return r.getExplanation(c.getOWLClass(), p.getOWLClass());
    }

    @Override
    public Optional<ExplanationAnalyser> getSubsumptionExplanationRendered(OntologyClass subC, OntologyClass superC) {
        Optional<Explanation> explanation = getSubsumptionExplanation(subC, superC);
        if (explanation.isPresent()) {
            return Optional.of(createExplanationRenderer(explanation.get()));
        }
        return Optional.empty();
    }

    private ExplantionAnalyserImpl createExplanationRenderer(Explanation e) {
        return new ExplantionAnalyserImpl(e, new HashSet<>(), renderManager);
    }
}
