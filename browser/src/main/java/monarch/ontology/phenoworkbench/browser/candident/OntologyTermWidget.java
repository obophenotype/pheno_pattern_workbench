package monarch.ontology.phenoworkbench.browser.candident;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import monarch.ontology.phenoworkbench.analytics.pattern.reconciliation.CandidateIdentifierApp;
import monarch.ontology.phenoworkbench.analytics.pattern.reconciliation.OntologyTermSet;
import monarch.ontology.phenoworkbench.browser.basic.LabelManager;
import monarch.ontology.phenoworkbench.util.CandidateKB;

public class OntologyTermWidget extends VerticalLayout{

    /**
	 * 
	 */
	private static final long serialVersionUID = -1988267491284938217L;
	Label l;
	TextField tf_search = new TextField();
    Button bt_search = new Button("S");
    TermGrid grid;

    OntologyTermWidget(OntologyTermSet ts, CandidateIdentifierApp app, CandidateKB editor) {
    		setMargin(false);
    		//setSpacing(false);
        grid = new TermGrid(ts,app,editor);
        l = LabelManager.htmlLabel("<strong>"+ts.getOid().toUpperCase()+"</strong>");
        bt_search.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = -3059032648139411370L;

			@Override
			public void buttonClick(ClickEvent event) {
				filter();
			}
		});
        addComponent(l);
        addComponent(layoutSearch());
        addComponent(grid);
    }

    private Component layoutSearch() {
        HorizontalLayout hl = new HorizontalLayout();
        hl.addComponent(tf_search);
        hl.addComponent(bt_search);
        return hl;
    }
    
    private void filter() {
        String value = tf_search.getValue();
        grid.applyFilter(value);
    }



	public void search(String value) {
		tf_search.setValue(value);
		grid.applyFilter(value);
	}

    public void refreshFilter() {
        grid.refreshFilter();
    }

	public String getCurrentSearch() {
		return tf_search.getValue();
	}

	public String getOid() {
		return grid.getOid();
	}
}
