package monarch.ontology.phenoworkbench.browser.basic;

import com.vaadin.ui.Label;
import monarch.ontology.phenoworkbench.util.StringUtils;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.util.List;

public class MarkDownTools {

	public static String toHTML(String md) {
		Parser parser = Parser.builder().build();
		Node document = parser.parse(md);
		HtmlRenderer renderer = HtmlRenderer.builder().build();
		return renderer.render(document);
	}


	
}
