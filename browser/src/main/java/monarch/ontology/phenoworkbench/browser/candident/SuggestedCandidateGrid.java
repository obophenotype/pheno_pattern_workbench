package monarch.ontology.phenoworkbench.browser.candident;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.StyleGenerator;
import com.vaadin.ui.renderers.HtmlRenderer;

import monarch.ontology.phenoworkbench.util.CandidateKB;
import monarch.ontology.phenoworkbench.util.OntologyClass;
import monarch.ontology.phenoworkbench.analytics.pattern.reconciliation.OntologyClassMatch;

class SuggestedCandidateGrid extends Grid<OntologyClassMatch> {

    /**
     *
     */
    private static final long serialVersionUID = -1503748580420486580L;
    private final List<OntologyClassMatch> suggestions = new ArrayList<>();
    private CandidateKB kb;


    public SuggestedCandidateGrid(CandidateKB kb) {
        this.kb = kb;
        setWidth("100%");
        setHeight("100%");
        setStyleName("termgrid");
        addColumn(rec -> "<strong>"+rec.getS2().getLabel()+"</strong>", new HtmlRenderer()).setCaption("Phenotype");
        
       addColumn(c->c.getJacc_super()).setCaption("JSC").setWidth(50.0);
        addColumn(c->c.getJacc_substring()).setCaption("JSS").setWidth(50.0);
        addColumn(c->c.getJacc_bucketsim()).setCaption("JSB").setWidth(50.0);
        
        kb.addCandidateChangeListener(this::refreshFilter);
        addComponentColumn(recon -> {
            Button button = new Button("");
            button.addClickListener(click -> blacklist(recon.getS2()));
            button.setStyleName("termgrid-button-blacklist");
            return button;
        }).setWidth(25.0).setStyleGenerator(sg).setCaption("B");
        addComponentColumn(recon -> {
            Button button = new Button("");
            button.addClickListener(click -> addCandidate(recon.getS2()));
            button.setStyleName("termgrid-button-candidate");
            return button;
        }).setWidth(25.0).setStyleGenerator(sg).setCaption("C");
     }
    
    private void blacklist(OntologyClass c) {
		kb.blacklistClassForCurrentCandidate(c);
	}

	StyleGenerator<OntologyClassMatch> sg = new StyleGenerator<OntologyClassMatch>() {
    		private static final long serialVersionUID = 1L;

		@Override
		public String apply(OntologyClassMatch item) {
			return "bt-termgrid";
		}
	};

    private void addCandidate(OntologyClass recon) {
    		kb.addClassToCurrentCandidate(recon);
	}

    public void setItems(Collection<OntologyClassMatch> p) {
    		suggestions.clear();
    		suggestions.addAll(p);
        super.setItems(suggestions);
    }

    public void refreshFilter() {
        ((ListDataProvider<OntologyClassMatch>) getDataProvider()).setFilter(s -> filterCriteria(s.getS2()));
    }

	private boolean filterCriteria(OntologyClass s) {
		if(kb.isCurrentCandidateContainsClass(s)) {
			return false;
		} 
		if(kb.isCandidateKBContainsClass(s)) {
			return false;
		}
		if(kb.isBlacklisted(s)) {
			return false;
		}
		return true;
	}
}
