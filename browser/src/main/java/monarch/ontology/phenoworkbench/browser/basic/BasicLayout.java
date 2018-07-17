package monarch.ontology.phenoworkbench.browser.basic;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.vaadin.ui.Component;
import monarch.ontology.phenoworkbench.util.*;

import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;


public abstract class BasicLayout extends VerticalLayout {

	/**
	 * 
	 */
	private static final long serialVersionUID = 216875934202756147L;
	//private final UI ui;
	private final RunAnalysisPanel runAnalysisPanel;
	private final Downloader downloader = Downloader.getInstance();
	private final KB kb = KB.getInstance();
	private final Files fileSystem = Files.getInstance();
	
	public BasicLayout(String title) {
		setMargin(false);
		setSpacing(false);
		setWidth("100%");
		setHeight("100%");
		runAnalysisPanel = new RunAnalysisPanel(getRunOptions());
		runAnalysisPanel.addClickListener(x -> runLong());
		
		addComponent(LabelManager.labelH1(title));
		addComponent(runAnalysisPanel);
	}
	private void runLong() {
		Window sub = new WaitingPopup();
		getUI().addWindow(sub);
		getUI().push();
		if(runAnalysisPanel.reDownload()) {kb.clearOntologyCache();
			UberOntology.instance().reset();}
		runAnalysis(runAnalysisPanel.getSelectedItems());
		UI.getCurrent().access(()->{sub.close();});
	}
	protected abstract Map<String, String> getRunOptions();
	protected abstract void runAnalysis(Set<OntologyEntry> selectedOntologies);

	private RunAnalysisPanel getRunAnalysisPanel() {
		return runAnalysisPanel;
	}

	protected String runOptionOrNull(String option) {
		return getRunAnalysisPanel().getRunoption(option).orElse("NULL");
	}

	protected void writeMarkdownToResults(List<String> report, boolean clear) {
		Label l = LabelManager.htmlLabelFromMarkdown(report);
		l.setWidth("100%");
		setResults(l,clear);
	}

	protected void setResults(Component c, boolean clear) {
		getRunAnalysisPanel().addResult(c, clear);
	}

	protected Optional<File> deleteMakeTmpDirectory(String name) {
		return fileSystem.deleteMakeTmpDirectory(name);
	}
	
	protected void setAdditionalSettingsComponent(Component c, boolean clear) {
		getRunAnalysisPanel().addAdditionalSettingsComponent(c, clear);
	}

	private Downloader getDownloader() {
		return downloader;
	}

	protected File downloadFile(String url, String extension) {
		return getDownloader().downloadFile(url,extension);
	}
}
