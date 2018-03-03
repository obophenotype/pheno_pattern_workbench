package monarch.ontology.phenoworkbench.browser;

import com.vaadin.ui.RadioButtonGroup;
import com.vaadin.ui.VerticalLayout;
import monarch.ontology.phenoworkbench.browser.analytics.ImpactMode;

public class QuickImpactModeSelect extends VerticalLayout {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -575938527956672285L;
	private RadioButtonGroup<ImpactMode> select =
		    new RadioButtonGroup<>("Quick Impact Mode");
	
	public QuickImpactModeSelect() {
		select.setItems(ImpactMode.values());
		select.setSelectedItem(ImpactMode.EXTERNAL);
		setMargin(false);
		setSpacing(false);
		addComponent(select);
	}

	public ImpactMode getSelectedItem() {
		return select.getSelectedItem().orElse(ImpactMode.THING);
	}

}
