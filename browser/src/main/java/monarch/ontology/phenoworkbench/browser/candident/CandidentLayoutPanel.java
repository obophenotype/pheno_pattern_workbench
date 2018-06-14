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
    private final OntologySearchBoxContainer searchBoxContainer;
    private final CandidateGridPanel candidateGridPanel;
    private final CurrentCandidateGridPanel currentCandidateGridPanel;
    private final BucketGridPanel bucketGridPanel;
    private final BlacklistGridPanel blacklistGridPanel;
    private final TextField tf_search = new TextField();
    private final TextField tf_bucket = new TextField();
    private final Button bt_search = new Button("S");
    private final Button bt_bucket = new Button("Add Bucket");
    private final CandidateKB kb;

    
    CandidentLayoutPanel(CandidateIdentifierApp p) {
        setSizeFull();
        setMargin(false);
        
        kb = new CandidateKBImpl();
        currentCandidateGridPanel = new CurrentCandidateGridPanel(kb);
        candidateGridPanel = new CandidateGridPanel(kb,p);
        bucketGridPanel = new BucketGridPanel(kb,p);
        blacklistGridPanel = new BlacklistGridPanel(kb, p);

        searchBoxContainer = new OntologySearchBoxContainer(p,kb);
        addComponent(layoutSearchBox());
        addComponent(layoutCandidatePanels());
        addComponent(layoutBucketPanels());

        bt_search.addClickListener(e -> filter());
        bt_bucket.addClickListener(e -> addBucket());
        tf_search.setValue(".*behaviou?r.*");
        tf_bucket.setValue("behaviour");
        filter();
    }

	private void addBucket() {
		kb.addBucket(new Bucket(tf_bucket.getValue(),searchBoxContainer.getSearchConfig()));
	}

	private Component layoutCandidatePanels() {
        HorizontalLayout hl = new HorizontalLayout();
        hl.setMargin(false);
        hl.setWidthUndefined();
        hl.addComponent(currentCandidateGridPanel);
        hl.addComponent(candidateGridPanel);
        return hl;
    }
	
	private Component layoutBucketPanels() {
        HorizontalLayout hl = new HorizontalLayout();
        hl.setMargin(false);
        hl.setWidthUndefined();
        hl.addComponent(bucketGridPanel);
        hl.addComponent(blacklistGridPanel);
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
