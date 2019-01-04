package monarch.ontology.phenoworkbench.inferencereview.basic;

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

import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@Theme("pheno")
@SuppressWarnings("serial")
@Push 
public class InferenceAnalyserUI extends UI {

	public static String PATTERNANALYTICSVIEW = "Pattern Analysis";
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
						views.put(PATTERNANALYTICSVIEW, new MainView());
					}
					setNewView(views.get(PATTERNANALYTICSVIEW));
				}
			}

			private void setNewView(Component c) {
				selection.removeAllComponents();
				selection.addComponent(c);
			}
		};

		barmenu.addItem(PATTERNANALYTICSVIEW, null, mycommand);
		//barmenu.addItem(CANDIDENT, null, mycommand);
		//barmenu.addItem(KB, null, mycommand);

	}

	@WebServlet(urlPatterns = "/*", name = "PhenoUIServlet", asyncSupported = true)
	@VaadinServletConfiguration(ui = InferenceAnalyserUI.class, productionMode = false)
	public static class MyUIServlet extends VaadinServlet {
	}

}