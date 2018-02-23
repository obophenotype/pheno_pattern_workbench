package monarch.ontology.phenoworkbench.browser;

import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Label;

public class LabelManager {
	
	public static Label labelH1(String s) {
		return htmlLabel("<h1>"+s+"<h1>");
	}
	
	public static Label labelH2(String s) {
		return htmlLabel("<h2>"+s+"<h2>");
	}

	public static Label htmlLabel(String s) {
		return new Label(s,ContentMode.HTML);
	}

}
