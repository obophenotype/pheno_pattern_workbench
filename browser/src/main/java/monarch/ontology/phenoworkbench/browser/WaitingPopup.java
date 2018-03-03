package monarch.ontology.phenoworkbench.browser;

import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

//Define a sub-window by inheritance
public class WaitingPopup extends Window {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 983881322993478390L;
	ProgressBar bar = new ProgressBar();
	
 public WaitingPopup() {
     super("Running analysis");
     center();
     bar.setIndeterminate(true);
     setWidth("350px");
     setHeight("200px");
     bar.setWidth("100%");
     bar.setHeight("100%");
     setModal(true);
     setClosable(false);
     VerticalLayout l = new VerticalLayout();
     l.setWidth("100%");
     l.setHeight("100%");
     l.setMargin(true);
     Label label = LabelManager.htmlLabel("<div style=' text-align: center;'>Wait for Analysis to finish. This can take a while.</div>");
     label.setWidth("100%");
     l.addComponent(label);
     l.addComponent(bar);
     setContent(l);
 }
}