package monarch.ontology.phenoworkbench.browser;

import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Label;

public class LabelManager {
	
	public static Label labelH1(String s) {
		return new Label("<h1>"+s+"<h1>",ContentMode.HTML);
	}
	
	public static Label labelH2(String s) {
		return new Label("<h2>"+s+"<h2>",ContentMode.HTML);
	}

}
