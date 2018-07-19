package monarch.ontology.phenoworkbench.browser.candident;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.StyleGenerator;
import com.vaadin.ui.Window;
import com.vaadin.ui.renderers.HtmlRenderer;

import monarch.ontology.phenoworkbench.util.CandidateKB;
import monarch.ontology.phenoworkbench.util.OntologyClass;
import monarch.ontology.phenoworkbench.analytics.pattern.reconciliation.CandidateIdentifierApp;
import monarch.ontology.phenoworkbench.analytics.pattern.reconciliation.OntologyTermSet;
import monarch.ontology.phenoworkbench.browser.basic.TermInfoWindow;

class TermGrid extends Grid<OntologyClass> {

	/**
	 *
	 */
	private static final long serialVersionUID = -1503748580420486580L;
	private final List<OntologyClass> reconciliations = new ArrayList<>();
	private String currentSearch = "";
	private CandidateKB kb;
	private OntologyTermSet ts;

	public TermGrid(OntologyTermSet ts, CandidateIdentifierApp app,CandidateKB kb) {
		this.kb = kb;
		this.ts = ts;
		setWidth("100%");
		setHeight("100%");
		setStyleName("termgrid");
		addColumn(rec -> "<strong>" + rec.getLabel() + "</strong>", new HtmlRenderer()).setCaption("Phenotype");

		addColumn(c -> c.getNode().indirectChildrenFlat().size()).setCaption("Impact").setWidth(70.0);

		kb.addCandidateChangeListener(this::refreshFilter);
		addComponentColumn(recon -> {
			Button button = new Button("");
			button.addClickListener(click -> suggestSimilar(recon,app));
			button.setStyleName("termgrid-button");
			return button;
		}).setWidth(25.0).setStyleGenerator(sg).setCaption("S");
		addComponentColumn(recon -> {
			Button button = new Button("");
			button.addClickListener(click -> showHierarchy(recon));
			button.setStyleName("termgrid-button");
			return button;
		}).setWidth(25.0).setStyleGenerator(sg).setCaption("H");
		addComponentColumn(recon -> {
			Button button = new Button("");
			button.addClickListener(click -> addCandidate(recon, kb));
			button.setStyleName("termgrid-button-candidate");
			return button;
		}).setWidth(25.0).setStyleGenerator(sg).setCaption("C");
		addComponentColumn(recon -> {
			Button button = new Button("");
			button.addClickListener(click -> kb.blacklist(recon));
			button.setStyleName("termgrid-button-blacklist");
			return button;
		}).setWidth(25.0).setStyleGenerator(sg).setCaption("B");
		setItems(ts.items(true));
		/*
		 * p.setExpandRatio(3); c_compl.setExpandRatio(2); c_sugg.setExpandRatio(1);
		 * c_ch.setExpandRatio(1); c_candidate.setExpandRatio(1);
		 */
		// setRowHeight(60);
		System.out.println("Done Creating Grid");

	}

	StyleGenerator<OntologyClass> sg = new StyleGenerator<OntologyClass>() {
		private static final long serialVersionUID = 1L;

		@Override
		public String apply(OntologyClass item) {
			return "bt-termgrid";
		}
	};

	private void addCandidate(OntologyClass recon, CandidateKB kb) {
		kb.addClassToCurrentCandidate(recon);
	}

	private void showHierarchy(OntologyClass c) {
		Window sub = new TermInfoWindow(c, ts);
		this.getUI().addWindow(sub);
		this.getUI().push();
	}

	private void suggestSimilar(OntologyClass c,CandidateIdentifierApp app) {
		Window sub = new SuggestSimilarWindow(c, app,kb);
		this.getUI().addWindow(sub);
		this.getUI().push();
	}

	public void setItems(Collection<OntologyClass> p) {
		System.out.println("Get Reconciliations");
		reconciliations.clear();
		reconciliations.addAll(p);
		System.out.println("Set Reconciliations: " + reconciliations.size());
		super.setItems(reconciliations);
	}

	public void restrict(OntologyTermSet candidateSet) {
		setItems(candidateSet.items());
	}

	public void applyFilter(String value) {
		currentSearch = value;
		refreshFilter();
	}

	public void refreshFilter() {
		((ListDataProvider<OntologyClass>) getDataProvider()).setFilter(s -> filterCriteria(s));
	}

	private boolean filterCriteria(OntologyClass s) {
		if (kb.isCurrentCandidateContainsClass(s)) {
			return false;
		}
		if (kb.isCandidateKBContainsClass(s)) {
			return false;
		}
		if (kb.isBlacklisted(s)) {
			return false;
		}
		if (currentSearch.isEmpty()) {
			return true;
		}
		return containsSearchString(s);
	}

	private boolean containsSearchString(OntologyClass s) {
		return s.getLabel().matches(currentSearch);
	}

	public String getOid() {
		return ts.getOid();
	}
}
