package monarch.ontology.phenoworkbench.browser;

import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Layout;
import com.vaadin.ui.ProgressBar;

public class RunAnalysisComponent extends HorizontalLayout {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2148045102477118777L;
	Button bt_runanalysis = new Button("Run analysis");
	
public RunAnalysisComponent() {
	setMargin(false);
	setSpacing(false);
	setWidth("500px");
	setHeight("100px");
	addComponent(bt_runanalysis);
}


public void addClickListener(Button.ClickListener object) {
	bt_runanalysis.addClickListener(object);
}
}
