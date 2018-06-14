package monarch.ontology.phenoworkbench.browser.candident;

import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.StyleGenerator;
import com.vaadin.ui.renderers.HtmlRenderer;

import monarch.ontology.phenoworkbench.analytics.pattern.reconciliation.Bucket;

class BucketGrid extends Grid<Bucket> {

	private static final long serialVersionUID = 7205333089896193601L;
	final private CandidateKB kb;

    public BucketGrid(CandidateKB kb) {
    		this.kb = kb;
    		kb.addGridChangeListener(this::refresh);
        setWidth("100%");
        setHeight("100%");
        setStyleName("termgrid");
        addColumn(rec -> "<strong>"+rec.getLabel()+"</strong>", new HtmlRenderer()).setCaption("Candidate");
        addComponentColumn(recon -> {
            Button button = new Button("");
            button.addClickListener(click -> removeBucket(recon));
            button.setStyleName("cg-button");
            return button;
        }).setWidth(25.0).setStyleGenerator(sg).setCaption("X");
    }
    
    private void removeBucket(Bucket c) {
        kb.removeBucket(c);
        refresh();
	}



	private void refresh() {
		setItems(kb.getBuckets());
	}

	StyleGenerator<Bucket> sg = new StyleGenerator<Bucket>() {
    		private static final long serialVersionUID = 1L;

		@Override
		public String apply(Bucket item) {
			return "bt-termgrid";
		}
	};

	
}
