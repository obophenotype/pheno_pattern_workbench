package monarch.ontology.phenoworkbench.inferencereview.basic;

import com.vaadin.ui.VerticalLayout;
import monarch.ontology.phenoworkbench.util.Explanation;
import monarch.ontology.phenoworkbench.util.Subsumption;

import java.io.File;
import java.util.Set;

public class InferenceReviewView extends VerticalLayout {

	private final AxiomEntry axiomEntry;

	public InferenceReviewView(Subsumption sub, Set<Explanation> explanationSet) {
		axiomEntry = new AxiomEntry(sub);
		addComponent(axiomEntry);
		explanationSet.forEach(e->addComponent(new ExplanationEntry(e)));
	}

	
}
