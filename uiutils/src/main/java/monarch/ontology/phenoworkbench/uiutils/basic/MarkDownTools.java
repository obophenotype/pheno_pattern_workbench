package monarch.ontology.phenoworkbench.uiutils.basic;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

public class MarkDownTools {

	public static String toHTML(String md) {
		Parser parser = Parser.builder().build();
		Node document = parser.parse(md);
		HtmlRenderer renderer = HtmlRenderer.builder().build();
		return renderer.render(document);
	}


	
}
