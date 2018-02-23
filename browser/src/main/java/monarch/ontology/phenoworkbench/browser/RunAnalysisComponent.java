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
	ProgressBar bar = new ProgressBar(0.0f);
	Layout vl_barcomponent = new HorizontalLayout();
	Button bt_runanalysis = new Button("Run analysis");
	
	
public RunAnalysisComponent() {
	
	bar.setIndeterminate(true);
	vl_barcomponent.setWidth("50%");
	vl_barcomponent.setHeight("100%");
	
	setWidth("500px");
	setHeight("100px");
	addComponent(bt_runanalysis);
	addComponent(vl_barcomponent);
}


public void addClickListener(Button.ClickListener object) {
	bt_runanalysis.addClickListener(object);
}
}
