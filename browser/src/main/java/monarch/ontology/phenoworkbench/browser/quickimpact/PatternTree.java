package monarch.ontology.phenoworkbench.browser.quickimpact;

import java.util.*;

import com.vaadin.data.TreeData;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.TreeDataProvider;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Tree;

import monarch.ontology.phenoworkbench.analytics.pattern.generation.OntologyClass;
import monarch.ontology.phenoworkbench.analytics.pattern.generation.PatternClass;
import monarch.ontology.phenoworkbench.util.Timer;
import monarch.ontology.phenoworkbench.analytics.pattern.generation.DefinedClass;

public class PatternTree extends Tree<PatternTreeItem> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7392609965462603849L;
	private Map<String, Set<PatternTreeItem>> mapPatternTree = new HashMap<>();
	TreeData<PatternTreeItem> treeData = new TreeData<>();

	
	PatternTree(Set<PatternClass> toppatterns) {
		Timer.start("PatternTree::PatternTree()");
		// Couple of childless root items
		Timer.start("PatternTree::PatternTree():constructTree");
		toppatterns.forEach(pattern->addTopLevelItemToTree(pattern));
		Timer.end("PatternTree::PatternTree():constructTree");

		Timer.start("PatternTree::PatternTree():treeDataProvider");
		DataProvider<PatternTreeItem,?> inMemoryDataProvider = new TreeDataProvider<>(treeData);
		setDataProvider(inMemoryDataProvider);
		Timer.end("PatternTree::PatternTree():treeDataProvider");
		setHeight("100%");
		setCaptionAsHtml(true);
		setContentMode(ContentMode.HTML);
		Timer.end("PatternTree::PatternTree()");
	}
	private void addTopLevelItemToTree(OntologyClass pattern) {
		Timer.start("PatternTree::addTopLevelItemToTree");
		PatternTreeItem pi = new PatternTreeItem(pattern);
		addToMapPatternTree(pattern, pi);
		addTreeData(treeData, null, pi);
		//populateTreeRecursively(treeData, pi);
		Timer.end("PatternTree::addTopLevelItemToTree");
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
	
	private void addToMapPatternTree(OntologyClass pattern, PatternTreeItem pi) {
		Timer.start("PatternTree::addToMapPatternTree()");
		String iri = pattern.getOWLClass().getIRI().toString();
		if (!mapPatternTree.containsKey(iri)) {
			mapPatternTree.put(iri, new HashSet<>());
		}
		mapPatternTree.get(iri).add(pi);
		Timer.end("PatternTree::addToMapPatternTree()");
	}
	/*
	public void collapseAll() {
		mapPatternTree.keySet().forEach(x -> mapPatternTree.get(x).forEach(this::collapse));
	}
	
	public void expand(PatternTreeItem patternTreeItem) {
		Iterator<OntologyClass> parents = patternTreeItem.getPatternClass().indirectParents();
		while(parents.hasNext()) {
			OntologyClass parent = parents.next();
			if (mapPatternTree.containsKey(parent)) {
				for (PatternTreeItem i : mapPatternTree.get(parent)) {
					expand(i);
				}
			}

		}
	}*/

	private void loadSubTree(PatternTreeItem c) {
		Timer.start("PatternTree::populateTreeRecursively()");
		Timer.start("PatternTree::populateTreeRecursively():getDirectChildren()");
		Set<OntologyClass> directchildren = c.getPatternClass().directChildren();
		Timer.end("PatternTree::populateTreeRecursively():getDirectChildren()");
		for(OntologyClass cs:directchildren) {
			PatternTreeItem csub = new PatternTreeItem(cs);
			addToMapPatternTree(cs, csub);
			addTreeData(treeData, c, csub);
			Timer.end("PatternTree::populateTreeRecursively()");
		}
	}
	private void addTreeData(TreeData<PatternTreeItem> treeData, PatternTreeItem c, PatternTreeItem csub) {
		Timer.start("PatternTree::addTreeData");
		treeData.addItem(c, csub);
		Timer.end("PatternTree::addTreeData");
	}

	public Set<PatternTreeItem> getMapPatternTreeItem(DefinedClass pc) {
		String iri = pc.getOWLClass().getIRI().toString();
		return mapPatternTree.containsKey(iri) ?mapPatternTree.get(iri) :Collections.emptySet();
	}

	public void expandLoad(PatternTreeItem pi) {
		loadSubTree(pi);
		expand(pi);
	}
}
