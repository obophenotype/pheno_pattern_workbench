package monarch.ontology.phenoworkbench.browser.basic;

import java.util.*;
import java.util.stream.Collectors;

import com.vaadin.data.TreeData;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.TreeDataProvider;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Tree;

import monarch.ontology.phenoworkbench.analytics.pattern.generation.OntologyClass;
import monarch.ontology.phenoworkbench.util.Timer;

public class PatternTree extends Tree<PatternTreeItem> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7392609965462603849L;
	private Map<String, Set<PatternTreeItem>> mapPatternTree = new HashMap<>();
	TreeData<PatternTreeItem> treeData = new TreeData<>();

	
	public PatternTree(Set<? extends OntologyClass> toppatterns) {
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
		addItemClickListener(e->expandLoad(e.getItem()));
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
	
	public void collapseAll() {
		mapPatternTree.keySet().forEach(x -> mapPatternTree.get(x).forEach(this::collapse));
	}
	
	public void expandAll() {
		Set<String> items = new HashSet<>(mapPatternTree.keySet());
		expandAllRecursive(items);
	}
	
	private void expandAllRecursive(Set<String> items) {
		for(String iri:items) {
			mapPatternTree.get(iri).forEach(this::expandLoad);
		}
	}

	private void loadSubTree(PatternTreeItem c) {
		Timer.start("PatternTree::loadSubTree()");
		Timer.start("PatternTree::populateTreeRecursively():getDirectChildren()");
		Set<OntologyClass> directchildren = c.getPatternClass().directChildren();
		Timer.end("PatternTree::populateTreeRecursively():getDirectChildren()");
		for(OntologyClass cs:directchildren) {
			PatternTreeItem csub = new PatternTreeItem(cs);
			addToMapPatternTree(cs, csub);
			addTreeData(treeData, c, csub);
			Timer.end("PatternTree::loadSubTree()");
		}
	}
	private void addTreeData(TreeData<PatternTreeItem> treeData, PatternTreeItem c, PatternTreeItem csub) {
		Timer.start("PatternTree::addTreeData");
		treeData.addItem(c, csub);
		Timer.end("PatternTree::addTreeData");
	}

	public Set<PatternTreeItem> getMapPatternTreeItem(OntologyClass pc) {
		String iri = keyString(pc);
		return mapPatternTree.containsKey(iri) ?mapPatternTree.get(iri) :Collections.emptySet();
	}
	private String keyString(OntologyClass pc) {
		return pc.getOWLClass().getIRI().toString();
	}

	public void expandLoad(PatternTreeItem pi) {
		loadSubTree(pi);
		expand(pi);
	}
	
	public void expandSelect(OntologyClass pc) {
		Timer.start("PatternTree::expandSelect");
		//System.out.print("PatternTree::ExpandSelect");
		addTreeItemsRecursively(pc,new HashSet<>());
		 for (PatternTreeItem pp : getMapPatternTreeItem(pc)) {
			 expand(pp);
			 select(pp);
	         //TODO implement: 
	         //scrollTo(1);
         }
		 Timer.end("PatternTree::expandSelect");
	}
	
	private void addTreeItemsRecursively(OntologyClass c, Set<String> done) {
		String iri = keyString(c);
		if(done.contains(iri)) {
			return;
		} else {
			done.add(iri);
		}
		for(OntologyClass parent: c.directParents()) {
			addTreeItemsRecursively(parent,done);
			getMapPatternTreeItem(parent).forEach(this::expandLoad);
		}
	}
}
