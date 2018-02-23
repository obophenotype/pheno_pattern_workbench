package monarch.ontology.phenoworkbench.browser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import monarch.ontology.phenoworkbench.browser.util.StringUtils;

public abstract class BasicLayout extends VerticalLayout {

	/**
	 * 
	 */
	private static final long serialVersionUID = 216875934202756147L;
	private final UI ui;
	private final RunAnalysisPanel runAnalysisPanel;
	private final File tmpdir;
	
	public BasicLayout(UI ui, File tmpdir, String title) {
		this.ui = ui;
		this.tmpdir = tmpdir;
		if(!tmpdir.exists()) {
			tmpdir.mkdir();
		}
		setMargin(false);
		setSpacing(false);
		setWidth("100%");
		setHeight("100%");
		runAnalysisPanel = new RunAnalysisPanel(getRunOptions());
		runAnalysisPanel.addClickListener(x -> runAnalysis(runAnalysisPanel.getSelectedItems()));
		
		addComponent(LabelManager.labelH1(title));
		addComponent(runAnalysisPanel);
	}
	protected abstract Map<String, String> getRunOptions();
	protected abstract void runAnalysis(Set<String> items);
	
	protected UI getUIFixed() {
		return ui;
	}
	

	protected RunAnalysisPanel getRunAnalysisPanel() {
		return runAnalysisPanel;
	}

	protected String runOptionOrNull(String option) {
		return getRunAnalysisPanel().getRunoption(option).orElse("NULL");
	}
	public File getTmpdir() {
		return tmpdir;
	}
	
	protected File deleteMakeTmpDirectory(String name) {
		File dir = new File(getTmpdir(),name);
		try {
			if(dir.exists()) FileUtils.forceDelete(dir);
			dir.mkdir();
			return dir;
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return null;
	}
	
	protected void downloadOntologies(Set<String> ontologyiris, File ontologiesdir) {
		for (String iri : ontologyiris) {
			String filename = iri.replaceAll("[^A-Za-z0-9]", "") + ".owl";
			try {
				FileUtils.copyURLToFile(new URL(iri), new File(ontologiesdir, filename));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	protected void writeMarkdownToResults(List<String> report) {
		StringBuilder sb = StringUtils.linesToStringBuilder(report);
		Label l = LabelManager.htmlLabel(MarkDownTools.toHTML(sb.toString()));
		l.setWidth("100%");
		getRunAnalysisPanel().addResult(l);
	}
	
}
