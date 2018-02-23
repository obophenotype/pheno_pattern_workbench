package monarch.ontology.phenoworkbench.browser;

import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Layout;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.VerticalLayout;

public class RunAnalysisComponent extends HorizontalLayout {
	ProgressBar bar = new ProgressBar(0.0f);
	Layout vl_barcomponent = new HorizontalLayout();
	Button bt_runanalysis = new Button("Run analysis");
	
	
public RunAnalysisComponent() {
	
	bar.setIndeterminate(true);
	vl_barcomponent.setWidth("50%");
	vl_barcomponent.setHeight("100%");
	
	Layout vl_runanalysis = new HorizontalLayout();
	vl_runanalysis.setWidth("500px");
	vl_runanalysis.setHeight("100px");
	vl_runanalysis.addComponent(bt_runanalysis);
	vl_runanalysis.addComponent(vl_barcomponent);
}


public void addClickListener(Button.ClickListener object) {
	bt_runanalysis.addClickListener(object);
}
}
