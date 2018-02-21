package monarch.ontology.phenoworkbench.browser;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@Theme("valo")
@SuppressWarnings("serial")
public class PhenoUI extends UI {
	
	public static String PATTERNANALYTICSVIEW = "Phenotype Pattern Analytics";
	public static String UNIONANALYTICSVIEW = "Ontology Union Analysis";
	Map<String,Layout> views = new HashMap<>();
	

	@Override
    protected void init(VaadinRequest request) {
        // The root of the component hierarchy
        VerticalLayout main = new VerticalLayout();
        setContent(main);  
        MenuBar barmenu = new MenuBar();
        main.addComponent(barmenu);
        
        final Layout selection = new VerticalLayout();
    	main.addComponent(selection);

    	// Define a common menu command for all the menu items.
    	MenuBar.Command mycommand = new MenuBar.Command() {
    	    public void menuSelected(MenuItem selectedItem) {
    	        String menuitem = selectedItem.getText();
    	        if(menuitem.equals(PATTERNANALYTICSVIEW)) {
    	        		selection.removeAllComponents();
    	        		if(!views.containsKey(PATTERNANALYTICSVIEW)) {
    	        			views.put(PATTERNANALYTICSVIEW, new PatternAnalyserView());
    	        		}
    	        		selection.addComponent(views.get(PATTERNANALYTICSVIEW));
    	        } else if(menuitem.equals(UNIONANALYTICSVIEW)) {
	        		selection.removeAllComponents();
	        		if(!views.containsKey(UNIONANALYTICSVIEW)) {
	        			views.put(UNIONANALYTICSVIEW, new UnionAnalyserView(PhenoUI.this));
	        		}
	        		selection.addComponent(views.get(UNIONANALYTICSVIEW));
	        }
    	    }
    	};
        
    		MenuItem phenotypepatterns = barmenu.addItem(PATTERNANALYTICSVIEW, null, mycommand);

    		MenuItem unionpatterns = barmenu.addItem(UNIONANALYTICSVIEW, null, mycommand);



    }
	
	@WebServlet(urlPatterns = "/*", name = "PhenoUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = PhenoUI.class, productionMode = false)
    public static class MyUIServlet extends VaadinServlet {
    }
	
}