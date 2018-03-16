package monarch.ontology.phenoworkbench.browser.basic;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.ui.Grid.SelectionMode;

public class BranchGrid extends VerticalLayout {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 9003001146110849173L;
	
	Grid<Branch> grid = new Grid<>();
	List<Branch> branches = new ArrayList<>();
	
	public BranchGrid() {
		setHeight("400px");
		setWidth("100%");
		grid.setWidth("100%");
		grid.setHeight("100%");
		grid.setSelectionMode(SelectionMode.NONE);
		populateDefault();
		grid.setItems(branches);
		grid.addColumn(Branch::getBranchiri).setCaption("IRI");
		grid.addComponentColumn(this::buildDeleteButton);
		grid.getEditor().setEnabled(true);
		//grid.setHeight((grid.getFooterRowHeight()+grid.getBodyRowHeight()+grid.getHeaderRowHeight())+"px");
		addComponent(grid);
	}
	
	private void populateDefault() {
		branches.add(new Branch("http://purl.obolibrary.org/obo/MP_0005386"));
		branches.add(new Branch("http://purl.obolibrary.org/obo/NBO_0000243"));
		branches.add(new Branch("http://purl.obolibrary.org/obo/FBcv_0000387"));
		branches.add(new Branch("http://purl.obolibrary.org/obo/WBPhenotype_0000517"));
		branches.add(new Branch("http://purl.obolibrary.org/obo/NBO_0000243"));
		branches.add(new Branch("http://purl.obolibrary.org/obo/NBO_0020110"));
		branches.add(new Branch("http://purl.obolibrary.org/obo/NBO_0000313"));
		branches.add(new Branch("http://purl.obolibrary.org/obo/PATO_0002265"));
	}
	
	private Button buildDeleteButton(Branch p) {
        Button button = new Button(VaadinIcons.CLOSE);
        button.addStyleName(ValoTheme.BUTTON_SMALL);
        button.addClickListener(e -> deleteBranch(p));
        return button;
    }

    private void deleteBranch(Branch p) {
        branches.remove(p);
        grid.setItems(branches);
    }

	public List<Branch> getBranches() {
		return new ArrayList<>(branches);
	}

}
