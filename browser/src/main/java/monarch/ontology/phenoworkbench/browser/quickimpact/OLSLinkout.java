package monarch.ontology.phenoworkbench.browser.quickimpact;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class OLSLinkout {

	private final static String OLS_SEARCH  = "https://www.ebi.ac.uk/ols/search?q=%s";
	
	public static String linkout(String iri, String label) {
		String q;
		try {
			q = URLEncoder.encode(iri, "UTF-8");
			return String.format(OLS_SEARCH, q)+"&exact=true";
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return String.format(OLS_SEARCH, label.replaceAll("[^A-Za-z0-9 ]", label).replaceAll(" ", "%20"));
		
	}
}
