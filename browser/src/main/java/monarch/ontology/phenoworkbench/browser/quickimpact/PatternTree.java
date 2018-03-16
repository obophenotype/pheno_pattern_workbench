package monarch.ontology.phenoworkbench.browser.quickimpact;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.vaadin.data.TreeData;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.TreeDataProvider;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Tree;

import monarch.ontology.phenoworkbench.analytics.quickimpact.QuickImpact;
import monarch.ontology.phenoworkbench.analytics.pattern.Pattern;
import monarch.ontology.phenoworkbench.analytics.pattern.PatternClass;

public class PatternTree extends Tree<PatternTreeItem> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7392609965462603849L;
	private Map<PatternClass, Set<PatternTreeItem>> mapPatternTree = new HashMap<>();
	List<PatternTreeItem> treeItemPatterns = new ArrayList<>();

	
	public PatternTree(QuickImpact p) {
		TreeData<PatternTreeItem> treeData = new TreeData<>();
		// Couple of childless root items

		
		for (PatternClass pattern : p.getTopPatterns()) {
			// System.out.println(pattern);
			if (pattern instanceof Pattern) {
				if (!((Pattern) pattern).isDefinedclass()) {
					PatternTreeItem pi = new PatternTreeItem(pattern);
					addToMapPatternTree(pattern, pi);
					TreeData td = treeData.addItem(null, pi);
					populateTreeRecursively(treeData, pi, p);
				}
			}
		}

		treeData.getRootItems().forEach(r -> {
			treeItemPatterns.add(r);
			treeItemPatterns.addAll(treeData.getChildren(r));
		});
		DataProvider inMemoryDataProvider = new TreeDataProvider<>(treeData);
		setDataProvider(inMemoryDataProvider);
		setHeight("100%");
		setCaptionAsHtml(true);
		setContentMode(ContentMode.HTML);
	}
	@Override
	public void setHeight(float height, Unit unit) {
		getCompositionRoot().setHeight(height, unit);
	}
	@Override
	public void setHeight(String height) {
		getCompositionRoot().setHeight(height);
	}
	@Override
	public void setHeightUndefined() {
		getCompositionRoot().setHeightUndefined();
	}
	
	private void addToMapPatternTree(PatternClass pattern, PatternTreeItem pi) {
		if (!mapPatternTree.containsKey(pattern)) {
			mapPatternTree.put(pattern, new HashSet<>());
		}
		mapPatternTree.get(pattern).add(pi);
	}
	
	public void collapseAll() {
		mapPatternTree.keySet().forEach(x -> mapPatternTree.get(x).forEach(y -> collapse(y)));
	}
	
	public void expand(PatternTreeItem patternTreeItem, QuickImpact p) {
		Set<PatternClass> parents = p.getParentPatterns(patternTreeItem.getPatternClass(), false);
		for (PatternClass parent : parents) {
			if (mapPatternTree.containsKey(parent)) {
				for (PatternTreeItem i : mapPatternTree.get(parent)) {
					expand(i);
				}
			}

		}
	}

	private void populateTreeRecursively(TreeData<PatternTreeItem> treeData, PatternTreeItem c, QuickImpact p) {
		for (PatternClass cs : p.getDirectChildren(c.getPatternClass())) {
			PatternTreeItem csub = new PatternTreeItem(cs);
			addToMapPatternTree(cs, csub);
			treeData.addItem(c, csub);
			populateTreeRecursively(treeData, csub, p);
		}
	}
	public Map<PatternClass, Set<PatternTreeItem>> getMapPatternTree() {
		return mapPatternTree;
	}
	public List<PatternTreeItem> getTreeItemPatterns() {
		return treeItemPatterns;
	}
}
