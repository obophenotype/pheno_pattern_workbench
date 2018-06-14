package monarch.ontology.phenoworkbench.browser.candident;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.StyleGenerator;
import com.vaadin.ui.renderers.HtmlRenderer;

import monarch.ontology.phenoworkbench.analytics.pattern.generation.OntologyClass;
import monarch.ontology.phenoworkbench.analytics.pattern.reconciliation.Candidate;

class BlacklistGrid extends Grid<BlacklistItem> {

	/**
	 *
	 */
	private static final long serialVersionUID = -1503748580420486580L;
	private final List<GridChangedListener> listeners = new ArrayList<>();
	private final CandidateKB kb;

	public BlacklistGrid(CandidateKB kb) {
		this.kb = kb;
		kb.addGridChangeListener(this::refresh);
		setWidth("100%");
		setHeight("100%");
		setStyleName("termgrid");
		addColumn(rec -> "<strong>" + rec.getLabel() + "</strong>", new HtmlRenderer()).setCaption("Blacklist");
		addComponentColumn(recon -> {
			Button button = new Button("");
			button.addClickListener(click -> removeCandidate(recon));
			button.setStyleName("cg-button");
			return button;
		}).setWidth(25.0).setStyleGenerator(sg).setCaption("X");

	}

	void removeCandidate(BlacklistItem c) {
		kb.removeBlacklist(c);
		refresh();
	}

	private void refresh() {
		setItems(kb.getBlacklist());
		listeners.forEach(l -> l.gridChange(new GridChangeEvent(this)));
	}

	StyleGenerator<BlacklistItem> sg = new StyleGenerator<BlacklistItem>() {
		private static final long serialVersionUID = 1L;

		@Override
		public String apply(BlacklistItem item) {
			return "bt-termgrid";
		}
	};

	public void addCandidate(OntologyClass c) {
		kb.addClassToCurrentCandidate(c);
		refresh();
	}

	public Collection<OntologyClass> getCandidates() {
		return kb.getClassesForCurrentCandidate();
	}

	public void clearCandidate() {
		kb.clearClassesForCurrentCandidate();
		refresh();
	}

	public Candidate getCandidate() {
		Candidate c = new Candidate();
		c.addOntologyClasses(getCandidates());
		return c;
	}

	public void addGridChangeListener(GridChangedListener listener) {
		listeners.add(listener);
	}

	public void setCandidate(Candidate c) {
		kb.clearClassesForCurrentCandidate();
		kb.addClassesToCurrentCandidate(c.getCandidates());
		refresh();
	}

}
