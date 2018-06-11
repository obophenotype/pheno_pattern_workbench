package monarch.ontology.phenoworkbench.browser.candident;

import com.vaadin.data.provider.DataProviderListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;

import monarch.ontology.phenoworkbench.analytics.pattern.generation.OntologyClass;
import monarch.ontology.phenoworkbench.analytics.pattern.reconciliation.Candidate;
import monarch.ontology.phenoworkbench.browser.basic.LabelManager;

class CurrentCandidateGridPanel extends VerticalLayout {

	private static final long serialVersionUID = -6731820896046578107L;
    private final CurrentCandidateGrid grid;
    private final Button bt_save = new Button("Save");

    
    CurrentCandidateGridPanel(CandidateKB kb) {
    		this.grid = new CurrentCandidateGrid(kb);
        setSizeFull();
        setMargin(false);
        bt_save.addClickListener(e->kb.saveCurrentCandidate());
        addComponent(layoutHeader());
        addComponent(grid);
    }
    
   private Component layoutHeader() {
        HorizontalLayout vl = new HorizontalLayout();
        vl.setMargin(false);
        vl.setWidth("100%");
        vl.addComponent(LabelManager.htmlLabel("<strong>Current Candidate</strong>"));
        bt_save.setWidth("80px");
        vl.addComponent(bt_save);
        return vl;
    }

   public void addCandidate(OntologyClass o) {
		grid.addCandidate(o);
	}

	public void remove(OntologyClass o) {
		grid.removeCandidate(o);
	}

	public void addDataChangeListener(DataProviderListener<OntologyClass> l) {
   		grid.getDataProvider().addDataProviderListener(l);
	}

	public void addCandidateSaveListener(Button.ClickListener listener) {
		bt_save.addClickListener(listener);
	}
	
	public void clearCandidate() {
		grid.clearCandidate();
	}

	public Candidate getCandidate() {
		return grid.getCandidate();
	}

	public void addGridChangeListener(GridChangedListener listener) {
		grid.addGridChangeListener(listener);
	}

	public void setCandidate(Candidate recon) {
		grid.setCandidate(recon);
	}
}
