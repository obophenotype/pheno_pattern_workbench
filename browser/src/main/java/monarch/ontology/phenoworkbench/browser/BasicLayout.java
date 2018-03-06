package monarch.ontology.phenoworkbench.browser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.vaadin.ui.Component;
import monarch.ontology.phenoworkbench.browser.util.Downloader;
import org.apache.commons.io.FileUtils;

import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import monarch.ontology.phenoworkbench.browser.util.StringUtils;

public abstract class BasicLayout extends VerticalLayout {

	/**
	 * 
	 */
	private static final long serialVersionUID = 216875934202756147L;
	private final UI ui;
	private final RunAnalysisPanel runAnalysisPanel;
	private final File tmpdir;
	private final Downloader downloader = new Downloader();
	
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
		runAnalysisPanel.addClickListener(x -> runLong());
		
		addComponent(LabelManager.labelH1(title));
		addComponent(runAnalysisPanel);
	}
	private void runLong() {
		Window sub = new WaitingPopup();
		this.getUI().addWindow(sub);
		ui.push();
		runAnalysis(runAnalysisPanel.getSelectedItems());UI.getCurrent().access(()->{sub.close();});
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
	
	protected void downloadFiles(Set<String> iris, File dir) {
		for (String iri : iris) {
			downloadFile(dir, iri,"owl");
		}
	}

	protected File downloadFile(File dir, String iri, String extension) {
		String filename = iri.replaceAll("[^A-Za-z0-9]", "") + "."+extension;
		File f = new File(dir, filename);
		try {
			downloader.download(new URL(iri), f);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return f;
	}

	protected void writeMarkdownToResults(List<String> report, boolean clear) {
		Label l = getHTMLFromMarkdown(report);
		l.setWidth("100%");
		setResults(l,clear);
	}
	
	protected Label getHTMLFromMarkdown(List<String> report) {
		StringBuilder sb = StringUtils.linesToStringBuilder(report);
		return getHTMLFromMarkdown(sb.toString());
	}
	
	protected Label getHTMLFromMarkdown(String s) {
		Label l = LabelManager.htmlLabel(MarkDownTools.toHTML(s));
		return l;
	}

	public void setResults(Component c, boolean clear) {
		getRunAnalysisPanel().addResult(c, clear);
	}
	
	public void setAdditionalSettingsComponent(Component c, boolean clear) {
		getRunAnalysisPanel().addAdditionalSettingsComponent(c, clear);
	}
}
