package monarch.ontology.phenoworkbench.analytics.inferenceanalysis;

import monarch.ontology.phenoworkbench.util.DefinedClass;
import monarch.ontology.phenoworkbench.util.RenderManager;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.util.HashSet;
import java.util.Set;

public abstract class PhenotypeDefinitionTransformer implements DefinitionTransformer {
    private final OWLDataFactory df = OWLManager.getOWLDataFactory();
    private final RenderManager renderManager;
    private final Set<OWLClass> pato;
    private final Set<OWLClass> phenotypes;


    public PhenotypeDefinitionTransformer(RenderManager renderManager, Set<OWLClass> pato,Set<OWLClass> phenotypes) {
        this.renderManager = renderManager;
        this.pato = pato;
        this.phenotypes = phenotypes;
        System.out.println("PATO size: "+pato.size());
        System.out.println("PHENOTYPE size: "+phenotypes.size());
    }

    @Override
    public DefinitionSet get(DefinitionSet basicDefinitions) {
        DefinitionSet out = new DefinitionSet();
        System.out.println("Transforming: "+this.getClass().getSimpleName());
        Set<DefinedClass> definedclasses = new HashSet<>();
        for (DefinedClass d : basicDefinitions.getDefinitions()) {
            if(containsAtLeastOnePatoTerm(d.getDefiniton())) {
                DefinedClass changed = transform(d);
                definedclasses.add(changed);
                if (!changed.getDefiniton().equals(d.getDefiniton())) {
                    System.out.println("----------------");
                    System.out.println("BEFORE: " + renderManager.renderForMarkdown(d.getDefiniton()));
                    System.out.println("AFTER: " + renderManager.renderForMarkdown(changed.getDefiniton()));
                }
            }
        }
        out.setDefinitions(definedclasses);
        return out;
    }

    public DefinedClass transform(DefinedClass d) {
        DefinedClass tranformed = new DefinedClass(d.getOWLClass(), transformClassExpression(d.getDefiniton()));
        tranformed.setPatternString(getRenderManager().renderForMarkdown(tranformed.getDefiniton()));
        tranformed.setLabel(getRenderManager().getLabel(tranformed.getOWLClass()));
        return tranformed;
    }


    protected abstract OWLClassExpression transformClassExpression(OWLClassExpression definiton);


    protected boolean containsAtLeastOnePatoTerm(OWLClassExpression cein) {
        for (OWLClass c : cein.getClassesInSignature()) {
            if (pato.contains(c)) {
                return true;
            }
        }
        return false;
    }

    protected RenderManager getRenderManager() {
        return renderManager;
    }

    protected OWLDataFactory df() {
        return df;
    }

    protected boolean isPhenotype(OWLClass c) {
        return phenotypes.contains(c);
    }
}
