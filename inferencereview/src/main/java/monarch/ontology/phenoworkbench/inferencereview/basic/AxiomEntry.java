package monarch.ontology.phenoworkbench.inferencereview.basic;

import com.vaadin.ui.VerticalLayout;
import monarch.ontology.phenoworkbench.uiutils.basic.LabelManager;
import monarch.ontology.phenoworkbench.util.RenderManager;
import monarch.ontology.phenoworkbench.util.Subsumption;

public class AxiomEntry extends VerticalLayout{
    private final Subsumption s;
    RenderManager rm  =RenderManager.getInstance();
    AxiomEntry(Subsumption subsumption) {
        s = subsumption;
        addComponent(LabelManager.labelH1(rm.render(subsumption.getSub_c())+" SubclassOf: "+rm.render(subsumption.getSuper_c())));
    }
}
