package monarch.ontology.phenoworkbench.browser.basic;

import monarch.ontology.phenoworkbench.analytics.pattern.generation.DefinedClass;
import monarch.ontology.phenoworkbench.analytics.pattern.generation.OntologyClass;
import monarch.ontology.phenoworkbench.browser.quickimpact.OLSLinkout;
import monarch.ontology.phenoworkbench.util.StringUtils;

public class HTMLRenderUtils {

	  public static String renderDefinedClass(DefinedClass p) {
		  return renderDefinedClass(p, -1);
	  }
    public static String renderDefinedClass(DefinedClass p, int breakafter) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div>");
        sb.append("<strong>");
        sb.append(renderOLSLinkout(p));
        sb.append(" ("+p.getOWLClass().getIRI().getRemainder().or(p.getLabel())+")");
        sb.append("</strong></a><br />");
        String s = breakafter > 0 ? StringUtils.insertPeriodically(p.getPatternString(), "<br>", breakafter) : colourKeywords(p.getPatternString());
        sb.append("<div>" + s + "</div>");
        sb.append("</div>");
        return sb.toString();
    }

	private static String colourKeywords(String patternString) {
		String out = addKeywordClass(patternString, "some");
		out = addKeywordClass(out, "and");
		out = addKeywordClass(out, "that");
		out = addKeywordClass(out, "not");
		out = addKeywordClass(out, "or");
		return out;
	}
	private static String addKeywordClass(String patternString, String key) {
		String out = patternString.replaceAll("(^)"+key+"([\\W])", "<span class=\"owlkey\">"+key+"</span> ");
		out = patternString.replaceAll("(\\W)"+key+"([\\W])", " <span class=\"owlkey\">"+key+"</span> ");
		return out;
	}
	public static String renderOLSLinkout(OntologyClass p) {
		return "<a href='"+ OLSLinkout.linkout(p.getOWLClass().getIRI().toString(),p.getLabel())+"' target='_blank'>"+p.getLabel()+"</a>";
	}
}
