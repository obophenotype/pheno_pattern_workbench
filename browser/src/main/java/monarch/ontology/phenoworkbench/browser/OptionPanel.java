package monarch.ontology.phenoworkbench.browser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.Grid;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Grid.SelectionMode;

public class OptionPanel extends VerticalLayout {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8850027539409352463L;
	Grid<RunOption> grid = new Grid<>();
	List<RunOption> runoptions = new ArrayList<>();
	
	OptionPanel(Map<String,String> options) {
		setHeightUndefined();
		setWidth("100%");
		setMargin(false);
		setSpacing(false);
		
		options.entrySet().forEach(e->runoptions.add(new RunOption(e.getKey(), e.getValue())));
		grid.setSelectionMode(SelectionMode.NONE);
		grid.setItems(runoptions);
		grid.addColumn(RunOption::getName).setCaption("Name");
		TextField taskField = new TextField();
		grid.addColumn(RunOption::getValue).setEditorComponent(taskField, RunOption::setValue).setExpandRatio(1);
		grid.setWidth("100%");
		//grid.setHeight((grid.getBodyRowHeight()*runoptions.size())+grid.getBodyRowHeight()+3+"px");
		grid.getEditor().setEnabled(true);
		grid.setHeightMode(HeightMode.UNDEFINED);
		addComponent(grid);
	}

	public Optional<String> getRunoption(String option) {
		for (RunOption ro : runoptions) {
			if (ro.getName().equals(option)) {
				return Optional.of(ro.getValue());
			} 
		}
		return Optional.empty();
	}

}
