package monarch.ontology.phenoworkbench.browser.candident;

import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import monarch.ontology.phenoworkbench.analytics.pattern.reconciliation.Bucket;
import monarch.ontology.phenoworkbench.analytics.pattern.reconciliation.CandidateIdentifierApp;
import monarch.ontology.phenoworkbench.browser.basic.LabelManager;

class CandidentLayoutPanel extends VerticalLayout {

	private static final long serialVersionUID = -8301978468694076930L;
	private final CandidateIdentifierApp p;
    private final OntologySearchBoxContainer searchBoxContainer;
    private final CandidateGridPanel candidateGridPanel;
    private final CurrentCandidateGridPanel currentCandidateGridPanel;
    private final BucketGridPanel bucketGridPanel;
    private final TextField tf_search = new TextField();
    private final TextField tf_bucket = new TextField();
    private final Button bt_search = new Button("S");
    private final Button bt_bucket = new Button("Add Bucket");
    private final CandidateKB kb;

    
    CandidentLayoutPanel(CandidateIdentifierApp p) {
        this.p = p;
        setSizeFull();
        setMargin(false);
        
        kb = new CandidateKBImpl();
        currentCandidateGridPanel = new CurrentCandidateGridPanel(kb);
        candidateGridPanel = new CandidateGridPanel(kb,p);
        bucketGridPanel = new BucketGridPanel();

        searchBoxContainer = new OntologySearchBoxContainer(this.p.getCandidatesByOntology().values(),kb);
        addComponent(layoutSearchBox());
        addComponent(layoutBucketCandidates());

        bt_search.addClickListener(e -> filter());
        bt_search.addClickListener(e -> addBucket());
        tf_search.setValue(".*behaviou?r.*");
        filter();
    }

	private void addBucket() {
		kb.addBucket(new Bucket(tf_bucket.getValue(),searchBoxContainer.getSearchConfig()));
	}

	private Component layoutBucketCandidates() {
        HorizontalLayout hl = new HorizontalLayout();
        hl.setMargin(false);
        hl.setWidthUndefined();
        hl.addComponent(currentCandidateGridPanel);
        hl.addComponent(candidateGridPanel);
        hl.addComponent(bucketGridPanel);
        return hl;
    }

    private void filter() {
        String value = tf_search.getValue();
        searchBoxContainer.searchAll(value);
    }

    private Component layoutSearchBox() {
        VerticalLayout vl = new VerticalLayout();
        vl.setMargin(false);
        vl.setWidth("100%");
        vl.addComponent(LabelManager.hr("100%"));
        vl.addComponent(layoutSearchBar());
        vl.addComponent(LabelManager.hr("100%"));
        vl.addComponent(searchBoxContainer);
        vl.addComponent(LabelManager.hr("100%"));
        return vl;
    }

    private Component layoutSearchBar() {
        HorizontalLayout hl = new HorizontalLayout();
        //hl.setWidth("100%");
        hl.setWidthUndefined();
        hl.addComponent(LabelManager.htmlLabel("Global search"));
        hl.addComponent(tf_search);
        hl.addComponent(bt_search);
        hl.addComponent(tf_bucket);
        hl.addComponent(bt_bucket);
        return hl;
    }

}
