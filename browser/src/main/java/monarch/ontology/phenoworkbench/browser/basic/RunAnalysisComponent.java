package monarch.ontology.phenoworkbench.browser.basic;

import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;

public class RunAnalysisComponent extends HorizontalLayout {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2148045102477118777L;
	Button bt_runanalysis = new Button("Run analysis");
	CheckBox cb_refreshdownload = new CheckBox("Refresh Download");
	
public RunAnalysisComponent() {
	cb_refreshdownload.setValue(false);
	setMargin(false);
	setSpacing(false);
	setWidth("500px");
	setHeight("100px");
	addComponent(bt_runanalysis);
	addComponent(cb_refreshdownload);
}


public void addClickListener(Button.ClickListener object) {
	bt_runanalysis.addClickListener(object);
}

boolean isRefreshDownload() {
	return cb_refreshdownload.getValue();
			
}
}
