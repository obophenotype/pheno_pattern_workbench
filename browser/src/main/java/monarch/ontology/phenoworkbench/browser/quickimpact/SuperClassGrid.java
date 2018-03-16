package monarch.ontology.phenoworkbench.browser.quickimpact;

import java.util.ArrayList;
import java.util.List;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

import monarch.ontology.phenoworkbench.analytics.quickimpact.QuickImpact;
import monarch.ontology.phenoworkbench.analytics.pattern.PatternClass;

public class SuperClassGrid extends Grid<PatternClass>{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7693916597555928732L;
	private QuickImpact qi;
	private PatternClass current;
	
	public SuperClassGrid() {
		addColumn(PatternClass::getLabel).setCaption("Name");
		addComponentColumn(this::createEntailmentButton).setCaption("Superclasses");
	}
	
	void update(QuickImpact qi,PatternClass c) {
		this.current = c;
		this.qi = qi;
		List<PatternClass> weightedPatterns = new ArrayList<>();
		for (PatternClass pattern : qi.getParentPatterns(c, true)) {
			weightedPatterns.add(pattern);
		}
		setItems(weightedPatterns);
		setRowHeight(40.0);
		setHeaderRowHeight(40.0);
		setHeight((40*weightedPatterns.size())+40+"px");
	}

	private Button createEntailmentButton(PatternClass p) {
        Button button = new Button("?");
        button.addStyleName(ValoTheme.BUTTON_SMALL);
        button.addClickListener(e -> showExplanation(p));
        return button;
    }

	private void showExplanation(PatternClass p) {
		Window sub = new ExplanationWindow(qi,p, current);
		  this.getUI().addWindow(sub);
		  this.getUI().push(); 
		  
	}

}
