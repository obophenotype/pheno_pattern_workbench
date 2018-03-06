package monarch.ontology.phenoworkbench.browser;

import com.vaadin.data.HasValue.ValueChangeListener;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import monarch.ontology.phenoworkbench.browser.analytics.Pattern;
import monarch.ontology.phenoworkbench.browser.analytics.PatternReconciler;
import monarch.ontology.phenoworkbench.browser.analytics.PatternReconciliation;

public class ReconcilerLayoutPanel extends VerticalLayout {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3354993059350574732L;
	private ReconcilerGrid grid;
	private TextField tf_filter_patterns = new TextField("Filter");
	private CheckBox cb_exclude_reconciled = new CheckBox("Exclude Reconciled");
	
	public ReconcilerLayoutPanel(PatternReconciler p) {
		grid = new ReconcilerGrid(p);
		setSizeFull();
		setMargin(false);
		tf_filter_patterns.addValueChangeListener(v->toggleTextFilter());
		cb_exclude_reconciled.addValueChangeListener(v->toggleExcludeReconciled());
		addComponent(cb_exclude_reconciled);
		addComponent(tf_filter_patterns);
		addComponent(grid);
		
	}

	private void toggleTextFilter() {
		ListDataProvider<PatternReconciliation> dataProvider = (ListDataProvider<PatternReconciliation>) grid.getDataProvider();
        dataProvider.setFilter(PatternReconciliation::getP1, s -> caseInsensitiveContains(s));
	}

	private boolean caseInsensitiveContains(Pattern s) {
		String value = tf_filter_patterns.getValue();
		if(value.length()>2) {
	        return s.toString().toLowerCase().contains(value.toLowerCase());

		}
		return true;
	}

	private void toggleExcludeReconciled() {
		ListDataProvider<PatternReconciliation> dataProvider = (ListDataProvider<PatternReconciliation>) grid.getDataProvider();
        dataProvider.setFilter(PatternReconciliation::isSyntacticallyEquivalent, s -> includeReconciled(s));
	}

	private boolean includeReconciled(Boolean s) {
		boolean exclude = cb_exclude_reconciled.getValue();
		if(exclude) {
			return !s;
		}
		return true;
	}

}
