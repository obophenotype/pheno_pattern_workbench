package monarch.ontology.phenoworkbench.browser;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Component;
import com.vaadin.ui.Layout;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;

import monarch.ontology.phenoworkbench.browser.views.AxiomRedundancyAnalyserView;
import monarch.ontology.phenoworkbench.browser.views.InferenceAnalyserView;
import monarch.ontology.phenoworkbench.browser.views.PatternAnalyserView;
import monarch.ontology.phenoworkbench.browser.views.QuickImpactView;
import monarch.ontology.phenoworkbench.browser.views.UnionAnalyserView;

import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@Theme("valo")
@SuppressWarnings("serial")
@Push 
public class PhenoUI extends UI {

	public static String PATTERNANALYTICSVIEW = "Phenotype Pattern";
	public static String UNIONANALYTICSVIEW = "Ontology Union Analysis";
	public static String INFERENCEANALYTICSVIEW = "Ontology Inference";
	public static String SUBCLASSREDUNDANCYVEIW = "Ontology Subclass Redundancy";
	public static String QUICKCLASSIMPACT = "Quick Impact";
	Map<String, Layout> views = new HashMap<>();

	@Override
	protected void init(VaadinRequest request) {
		// The root of the component hierarchy
		//TODO Make tmdir configurable
		File tmpdir = new File("tnmp");
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
						views.put(PATTERNANALYTICSVIEW, new PatternAnalyserView(PhenoUI.this,tmpdir));
					}
					setNewView(views.get(PATTERNANALYTICSVIEW));
				} 
				else if (menuitem.equals(UNIONANALYTICSVIEW)) {
					if (!views.containsKey(UNIONANALYTICSVIEW)) {
						views.put(UNIONANALYTICSVIEW, new UnionAnalyserView(PhenoUI.this,tmpdir));
					}
					setNewView(views.get(UNIONANALYTICSVIEW));
				}
				else if (menuitem.equals(INFERENCEANALYTICSVIEW)) {
					if (!views.containsKey(INFERENCEANALYTICSVIEW)) {
						views.put(INFERENCEANALYTICSVIEW, new InferenceAnalyserView(PhenoUI.this,tmpdir));
					}
					setNewView(views.get(INFERENCEANALYTICSVIEW));
				}
				else if (menuitem.equals(SUBCLASSREDUNDANCYVEIW)) {
					if (!views.containsKey(SUBCLASSREDUNDANCYVEIW)) {
						views.put(SUBCLASSREDUNDANCYVEIW, new AxiomRedundancyAnalyserView(PhenoUI.this,tmpdir));
					}
					setNewView(views.get(SUBCLASSREDUNDANCYVEIW));
				} 
				else if (menuitem.equals(QUICKCLASSIMPACT)) {
					if (!views.containsKey(QUICKCLASSIMPACT)) {
						views.put(QUICKCLASSIMPACT, new QuickImpactView(PhenoUI.this,tmpdir));
					}
					setNewView(views.get(QUICKCLASSIMPACT));
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
		barmenu.addItem(QUICKCLASSIMPACT, null, mycommand);
		barmenu.addItem(SUBCLASSREDUNDANCYVEIW, null, mycommand);

	}

	@WebServlet(urlPatterns = "/*", name = "PhenoUIServlet", asyncSupported = true)
	@VaadinServletConfiguration(ui = PhenoUI.class, productionMode = false)
	public static class MyUIServlet extends VaadinServlet {
	}

}