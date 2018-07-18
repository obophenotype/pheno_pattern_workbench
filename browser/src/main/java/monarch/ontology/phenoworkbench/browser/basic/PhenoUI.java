package monarch.ontology.phenoworkbench.browser.basic;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Component;
import com.vaadin.ui.Layout;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;

import monarch.ontology.phenoworkbench.browser.candident.CandidentView;
import monarch.ontology.phenoworkbench.browser.quickimpact.QuickImpactView;
import monarch.ontology.phenoworkbench.browser.reconciliation.MappingReviewView;
import monarch.ontology.phenoworkbench.browser.reportviews.*;

import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@Theme("pheno")
@SuppressWarnings("serial")
@Push 
public class PhenoUI extends UI {

	public static String PATTERNANALYTICSVIEW = "Pattern Analysis";
	public static String UNIONANALYTICSVIEW = "Ontology Union Debugger";
	public static String INFERENCEANALYTICSVIEW = "Inference Analysis";
	public static String SUBCLASSREDUNDANCYVEIW = "Subclass Redundancy";
	public static String QUICKCLASSIMPACT = "Pattern Browser";
	public static String RECONCILIATION = "Candidate Identification";
	public static String CANDIDENT = "Candidate Identification";
	public static String KB = "Reconciliation Candidates";
	Map<String, Layout> views = new HashMap<>();

	@Override
	protected void init(VaadinRequest request) {
		// The root of the component hierarchy
		VaadinSession.getCurrent().getSession().setMaxInactiveInterval(1200); 
		VerticalLayout main = new VerticalLayout();
		setContent(main);
		MenuBar barmenu = new MenuBar();
		main.addComponent(barmenu);
		setWidth("100%");
		final VerticalLayout selection = new VerticalLayout();
		selection.setWidth("100%");
		selection.setMargin(true);
		selection.setSpacing(true);
		main.addComponent(selection);

		// Define a common menu command for all the menu items.
		MenuBar.Command mycommand = new MenuBar.Command() {
			public void menuSelected(MenuItem selectedItem) {
				String menuitem = selectedItem.getText();
				if (menuitem.equals(PATTERNANALYTICSVIEW)) {
					if (!views.containsKey(PATTERNANALYTICSVIEW)) {
						views.put(PATTERNANALYTICSVIEW, new PatternAnalyserView());
					}
					setNewView(views.get(PATTERNANALYTICSVIEW));
				}
				else if (menuitem.equals(KB)) {
					if (!views.containsKey(KB)) {
						views.put(KB, new VerticalLayout());
					}
					setNewView(views.get(KB));
				}
				else if (menuitem.equals(UNIONANALYTICSVIEW)) {
					if (!views.containsKey(UNIONANALYTICSVIEW)) {
						views.put(UNIONANALYTICSVIEW, new UnionAnalyserView());
					}
					setNewView(views.get(UNIONANALYTICSVIEW));
				}
				else if (menuitem.equals(INFERENCEANALYTICSVIEW)) {
					if (!views.containsKey(INFERENCEANALYTICSVIEW)) {
						views.put(INFERENCEANALYTICSVIEW, new InferenceAnalyserView());
					}
					setNewView(views.get(INFERENCEANALYTICSVIEW));
				}
				else if (menuitem.equals(SUBCLASSREDUNDANCYVEIW)) {
					if (!views.containsKey(SUBCLASSREDUNDANCYVEIW)) {
						views.put(SUBCLASSREDUNDANCYVEIW, new AxiomRedundancyAnalyserView());
					}
					setNewView(views.get(SUBCLASSREDUNDANCYVEIW));
				} 
				else if (menuitem.equals(QUICKCLASSIMPACT)) {
					if (!views.containsKey(QUICKCLASSIMPACT)) {
						views.put(QUICKCLASSIMPACT, new QuickImpactView());
					}
					setNewView(views.get(QUICKCLASSIMPACT));
				}
				else if (menuitem.equals(RECONCILIATION)) {
					if (!views.containsKey(RECONCILIATION)) {
						views.put(RECONCILIATION, new MappingReviewView());
					}
					setNewView(views.get(RECONCILIATION));
				}
				else if (menuitem.equals(CANDIDENT)) {
					if (!views.containsKey(CANDIDENT)) {
						views.put(CANDIDENT, new CandidentView());
					}
					setNewView(views.get(CANDIDENT));
				}
			}

			private void setNewView(Component c) {
				selection.removeAllComponents();
				selection.addComponent(c);
			}
		};

		barmenu.addItem(PATTERNANALYTICSVIEW, null, mycommand);
		barmenu.addItem(UNIONANALYTICSVIEW, null, mycommand);
		barmenu.addItem(INFERENCEANALYTICSVIEW, null, mycommand);
		barmenu.addItem(SUBCLASSREDUNDANCYVEIW, null, mycommand);
		barmenu.addItem(QUICKCLASSIMPACT, null, mycommand);
		barmenu.addItem(RECONCILIATION, null, mycommand);
		//barmenu.addItem(CANDIDENT, null, mycommand);
		//barmenu.addItem(KB, null, mycommand);

	}

	@WebServlet(urlPatterns = "/*", name = "PhenoUIServlet", asyncSupported = true)
	@VaadinServletConfiguration(ui = PhenoUI.class, productionMode = false)
	public static class MyUIServlet extends VaadinServlet {
	}

}