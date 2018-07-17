package monarch.ontology.phenoworkbench.browser.candident;

import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.StyleGenerator;
import com.vaadin.ui.renderers.HtmlRenderer;

import monarch.ontology.phenoworkbench.util.Candidate;
import monarch.ontology.phenoworkbench.util.CandidateKB;

class CandidateGrid extends Grid<Candidate> {

	/**
	 *
	 */
	private static final long serialVersionUID = -1503748580420486580L;

	private final CandidateKB kb;

	public CandidateGrid(CandidateKB kb) {
		this.kb = kb;
		kb.addCandidateChangeListener(this::refresh);
		setWidth("100%");
		setHeight("100%");
		setStyleName("termgrid");
		Column<Candidate, String> p = addColumn(rec -> "<strong>" + rec.getLabel() + "</strong>", new HtmlRenderer())
				.setCaption("Candidate");

		Column<Candidate, Integer> c_compl = addColumn(c -> c.getLabel().length()).setCaption("Impact").setWidth(70.0);

		Column<Candidate, Button> c_candidate = addComponentColumn(recon -> {
			Button button = new Button("");
			button.addClickListener(click -> removeCandidate(recon));
			button.setStyleName("cg-button");
			return button;
		}).setWidth(25.0).setStyleGenerator(sg).setCaption("X");
		Column<Candidate, Button> c_edit = addComponentColumn(recon -> {
			Button button = new Button("");
			button.addClickListener(e -> editCandidate(kb, recon));
			button.setStyleName("cg-button");
			return button;
		}).setWidth(25.0).setStyleGenerator(sg).setCaption("E");

	}

	private void editCandidate(CandidateKB currentCandidateGrid, Candidate c) {
		currentCandidateGrid.setCurrentCandidate(c);
		removeCandidate(c);
	}

	private void removeCandidate(Candidate c) {
		kb.removeCandidate(c);
		refresh();
	}

	private void refresh() {
		setItems(kb.getAllCandidates());
	}

	StyleGenerator<Candidate> sg = new StyleGenerator<Candidate>() {
		private static final long serialVersionUID = 1L;

		@Override
		public String apply(Candidate item) {
			return "bt-termgrid";
		}
	};

}
