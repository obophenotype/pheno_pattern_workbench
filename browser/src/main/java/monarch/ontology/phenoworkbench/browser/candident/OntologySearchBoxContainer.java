package monarch.ontology.phenoworkbench.browser.candident;

import com.vaadin.ui.HorizontalLayout;
import monarch.ontology.phenoworkbench.analytics.pattern.reconciliation.CandidateIdentifierApp;
import monarch.ontology.phenoworkbench.analytics.pattern.reconciliation.OntologyTermSet;
import monarch.ontology.phenoworkbench.util.CandidateKB;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class OntologySearchBoxContainer extends HorizontalLayout {

	private static final long serialVersionUID = -3231970522503848146L;
	Set<OntologyTermWidget> widgets = new HashSet<>();

    OntologySearchBoxContainer(CandidateIdentifierApp app, CandidateKB editor) {
    		setMargin(false);
    		setWidth("100%");
        for(OntologyTermSet ts:app.getCandidatesByOntology().values()) {
            widgets.add(new OntologyTermWidget(ts, app, editor));
        }
        for(OntologyTermWidget tw:widgets) {
            addComponent(tw);
        }
    }

	public void searchAll(String value) {
		widgets.forEach(w->w.search(value));
	}

    public void refreshFilter() {
        widgets.forEach(w->w.refreshFilter());
    }

	public Map<String, String> getSearchConfig() {
		Map<String,String> buckets = new HashMap<>();
		for(OntologyTermWidget w:widgets) {
			buckets.put(w.getOid(), w.getCurrentSearch());
		}
		return buckets;
	}
}
