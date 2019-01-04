package monarch.ontology.phenoworkbench.inferencereview.basic;

import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;
import monarch.ontology.phenoworkbench.uiutils.basic.LabelManager;
import monarch.ontology.phenoworkbench.util.Explanation;

public class ExplanationEntry extends VerticalLayout {
    public ExplanationEntry(Explanation e) {
        addComponent(LabelManager.htmlLabel(e.toString()));
    }
}
