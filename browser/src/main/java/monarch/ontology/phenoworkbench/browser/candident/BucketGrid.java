package monarch.ontology.phenoworkbench.browser.candident;

import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.StyleGenerator;
import com.vaadin.ui.renderers.HtmlRenderer;
import monarch.ontology.phenoworkbench.analytics.pattern.reconciliation.Bucket;
import monarch.ontology.phenoworkbench.analytics.pattern.reconciliation.Candidate;

import java.util.ArrayList;
import java.util.List;

class BucketGrid extends Grid<Bucket> {

    private final List<Bucket> candidates = new ArrayList<>();


    public BucketGrid() {
        setWidth("100%");
        setHeight("100%");
        setStyleName("termgrid");
        Column<Bucket, String> p = addColumn(
                rec -> "<strong>"+rec.getLabel()+"</strong>", new HtmlRenderer()).setCaption("Candidate");
        
        Column<Bucket, Integer> c_compl = addColumn(c->c.getLabel().length()).setCaption("Impact").setWidth(70.0);
        
        Column<Bucket, Button> c_candidate = addComponentColumn(recon -> {
            Button button = new Button("");
            button.addClickListener(click -> removeBucket(recon));
            button.setStyleName("cg-button");
            return button;
        }).setWidth(25.0).setStyleGenerator(sg).setCaption("X");

    }
    
    private void removeBucket(Bucket c) {
        candidates.remove(c);
        setItems(candidates);
	}

	StyleGenerator<Bucket> sg = new StyleGenerator<Bucket>() {
    		private static final long serialVersionUID = 1L;

		@Override
		public String apply(Bucket item) {
			return "bt-termgrid";
		}
	};

    public void addBucket(Bucket c) {
        candidates.add(c);
        setItems(candidates);
	}



	
}
