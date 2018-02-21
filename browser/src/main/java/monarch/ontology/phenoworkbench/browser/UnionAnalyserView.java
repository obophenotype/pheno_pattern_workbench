package monarch.ontology.phenoworkbench.browser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.vaadin.ui.*;
import org.apache.commons.io.FileUtils;

import com.vaadin.data.Binder;
import com.vaadin.navigator.View;
import com.vaadin.ui.Grid.Column;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.components.colorpicker.ColorPickerPopup;

import monarch.ontology.phenoworkbench.browser.unionanalytics.CorpusDebugger;
import org.vaadin.viritin.label.RichText;

public class UnionAnalyserView extends VerticalLayout {

	/**
	 * 
	 */
	private static final long serialVersionUID = 255453218992079876L;

	OntologyRegistry registry = new OntologyRegistry();
	CheckBoxGroup<String> cb_selectontologies = new CheckBoxGroup<>("Select Ontologies");
	Button bt_runanalysis = new Button("Run analysis");
	Grid<RunOption> grid = new Grid<>();
	List<RunOption> runoptions = new ArrayList<>();
	Layout results = new VerticalLayout();
	ProgressBar bar = new ProgressBar(0.0f);
	Layout vl_barcomponent = new HorizontalLayout();
	UI ui;
	
	public UnionAnalyserView(UI ui) {
		this.setMargin(false);
		this.setSpacing(false);
		this.ui = ui;
		final VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);
		layout.setSpacing(true);
		layout.setWidth("100%");
		layout.setHeight("100%");
		addComponent(layout);

		cb_selectontologies.setItems(registry.getOntologies());
		bt_runanalysis.addClickListener(x -> runAnalysis(cb_selectontologies.getSelectedItems()));
		bar.setIndeterminate(true);

		layout.addComponent(LabelManager.labelH1("Ontology Union Analysis"));
		layout.addComponent(LabelManager.labelH2("Select Ontologies for the analysis"));
		layout.addComponent(cb_selectontologies);
		layout.addComponent(prepareOptionsGrid());
		
		vl_barcomponent.setWidth("50%");
		vl_barcomponent.setHeight("100%");
		Layout vl_runanalysis = new HorizontalLayout();
		vl_runanalysis.setWidth("500px");
		vl_runanalysis.setHeight("100px");
		vl_runanalysis.addComponent(bt_runanalysis);
		vl_runanalysis.addComponent(vl_barcomponent);
		
		layout.addComponent(vl_runanalysis);
		layout.addComponent(results);
	}

	private Layout prepareOptionsGrid() {
		Layout vl_grid = new VerticalLayout();
		vl_grid.setHeight("300px");
		vl_grid.setWidth("400px");
		grid.setWidth("100%");
		grid.setHeight("100%");
		runoptions.add(new RunOption("imports", "yes"));
		runoptions.add(new RunOption("maxunsat", "10"));
		runoptions.add(new RunOption("maxexplanation", "10"));
		grid.setSelectionMode(SelectionMode.NONE);
		grid.setItems(runoptions);
		grid.addColumn(RunOption::getName).setCaption("Name");
		TextField taskField = new TextField();
		grid.addColumn(RunOption::getValue).setEditorComponent(taskField, RunOption::setValue).setExpandRatio(1);

		grid.getEditor().setEnabled(true);
		//grid.setHeight((grid.getFooterRowHeight()+grid.getBodyRowHeight()+grid.getHeaderRowHeight())+"px");
		vl_grid.addComponent(grid);
		return vl_grid;
	}

	private void runAnalysis(Set<String> selectedItems) {
		
		
		selectedItems.forEach(System.out::println);

		boolean imports = false;
		int maxunsat = 0;
		int maxexplunsat = 0;
		String reasoner = "elk";

		for (RunOption ro : runoptions) {
			System.out.println(ro.getName() + ":" + ro.getValue());
			if (ro.getName().equals("imports")) {
				imports = ro.getValue().equals("yes");
			} else if (ro.getName().equals("maxunsat")) {
				maxunsat = Integer.valueOf(ro.getValue());
			} else if (ro.getName().equals("maxexplanation")) {
				maxexplunsat = Integer.valueOf(ro.getValue());
			}
		}
		File db = new File("uatmp");
		File outdir = new File("uatmpout");
		try {
			FileUtils.forceDelete(db);
			FileUtils.forceDelete(outdir);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		db.mkdir();
		System.out.println(db.getAbsolutePath());
		outdir.mkdir();
		for (String iri : selectedItems) {
			String filename = iri.replaceAll("[^A-Za-z0-9]", "") + ".owl";
			try {
				FileUtils.copyURLToFile(new URL(iri), new File(db, filename));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		CorpusDebugger p = new CorpusDebugger(db, reasoner, imports, maxunsat, maxexplunsat);
		
		ui.access(new Runnable() {
			
			@Override
			public void run() {
				runDebugger(p);
			}
			
			private void runDebugger(CorpusDebugger p) {
				vl_barcomponent.removeAllComponents();
				vl_barcomponent.addComponent(bar);
			
				p.run();
				vl_barcomponent.removeAllComponents();
				List<String> report = p.getReportLines();
				StringBuilder sb = new StringBuilder();
				for (String line : report) {
					System.out.println(line);
					sb.append(line + "\n");
				}
				results.addComponent(new RichText().withMarkDown(sb.toString()));
			}
		});
	}

	
}
