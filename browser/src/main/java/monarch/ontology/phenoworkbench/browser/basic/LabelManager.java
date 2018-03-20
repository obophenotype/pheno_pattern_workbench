package monarch.ontology.phenoworkbench.browser.basic;

import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Label;
import monarch.ontology.phenoworkbench.util.StringUtils;

import java.util.List;

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

	public static Label htmlLabelFromMarkdown(List<String> report) {
		return htmlLabelFromMarkdown(StringUtils.linesToStringBuilder(report).toString());
	}

	public static Label htmlLabelFromMarkdown(String s) {
		return htmlLabel(MarkDownTools.toHTML(s));
	}
}
