package monarch.ontology.phenoworkbench.browser.quickimpact;

import java.util.ArrayList;
import java.util.List;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

import monarch.ontology.phenoworkbench.analytics.pattern.generation.OntologyClass;
import monarch.ontology.phenoworkbench.analytics.pattern.generation.ExplanationRenderProvider;

public class SuperClassGrid extends Grid<OntologyClass>{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7693916597555928732L;
	private ExplanationRenderProvider qi;
	private OntologyClass current;
	
	public SuperClassGrid() {
		addColumn(OntologyClass::getLabel).setCaption("Superclasses");
		addComponentColumn(this::createEntailmentButton).setCaption("?").setWidth(60.0);
		updateHeight(0);
	}
	
	public void update(ExplanationRenderProvider qi,OntologyClass c) {
		this.current = c;
		this.qi = qi;
		List<OntologyClass> weightedPatterns = new ArrayList<>(c.directParents());
		setItems(weightedPatterns);
		updateHeight(weightedPatterns.size());
	}

	private void updateHeight(int rowcount) {
		setRowHeight(40.0);
		setHeaderRowHeight(40.0);
		setHeight((40*rowcount)+40+"px");
	}

	private Button createEntailmentButton(OntologyClass p) {
        Button button = new Button("?");
        button.addStyleName(ValoTheme.BUTTON_SMALL);
        button.addClickListener(e -> showExplanation(p));
        return button;
    }

	private void showExplanation(OntologyClass p) {
		if(qi!=null) {
		Window sub = new ExplanationWindow(qi,p, current);
		  this.getUI().addWindow(sub);
		  this.getUI().push(); 
		} else {
			Notification.show("Failed to generate Explanation.");
		}
	}

}
