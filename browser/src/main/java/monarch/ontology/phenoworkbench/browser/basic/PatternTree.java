package monarch.ontology.phenoworkbench.browser.basic;

import java.util.*;

import com.vaadin.data.TreeData;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.TreeDataProvider;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Tree;

import monarch.ontology.phenoworkbench.util.Node;
import monarch.ontology.phenoworkbench.util.OntologyClass;
import monarch.ontology.phenoworkbench.util.Timer;

public class PatternTree extends Tree<Node> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7392609965462603849L;
	private Map<String, Set<Node>> mapPatternTree = new HashMap<>();
	TreeData<Node> treeData = new TreeData<>();

	
	public PatternTree(Set<Node> toppatterns) {
		Timer.start("PatternTree::PatternTree()");
		// Couple of childless root items
		Timer.start("PatternTree::PatternTree():constructTree");
		toppatterns.forEach(pattern->addTopLevelItemToTree(pattern));
		Timer.end("PatternTree::PatternTree():constructTree");

		Timer.start("PatternTree::PatternTree():treeDataProvider");
		DataProvider<Node,?> inMemoryDataProvider = new TreeDataProvider<>(treeData);
		setDataProvider(inMemoryDataProvider);
		Timer.end("PatternTree::PatternTree():treeDataProvider");
		setHeight("100%");
		setCaptionAsHtml(true);
		setContentMode(ContentMode.HTML);
		addItemClickListener(e->expandLoad(e.getItem()));
		Timer.end("PatternTree::PatternTree()");
	}
	private void addTopLevelItemToTree(Node pi) {
		Timer.start("PatternTree::addTopLevelItemToTree");
		addToMapPatternTree(pi);
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
	
	private void addToMapPatternTree(Node pi) {
		Timer.start("PatternTree::addToMapPatternTree()");
		for(OntologyClass c:pi.getEquivalenceGroup()) {
			String iri = c.getOWLClass().getIRI().toString();
			if (!mapPatternTree.containsKey(iri)) {
				mapPatternTree.put(iri, new HashSet<>());
			}
			mapPatternTree.get(iri).add(pi);
		}
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
	
	

	private void loadSubTree(Node c) {
		Timer.start("PatternTree::loadSubTree()");
		for(Node csub:c.directChildren()) {
			addToMapPatternTree(csub);
			addTreeData(treeData, c, csub);
			Timer.end("PatternTree::loadSubTree()");
		}
	}
	
	Map<Node,Set<Node>> td = new HashMap<>();
	private void addTreeData(TreeData<Node> treeData, Node c, Node csub) {
		Timer.start("PatternTree::addTreeData");
		
		if(c!=null) {
			if(c.equals(csub)) {
				System.out.println("Trying to add same node as parent and child. Equivalent nodes should not be directChildren");
				return;
			}
		}
		if(!td.containsKey(c)) {
			td.put(c, new HashSet<>());
		}
		if(!td.get(c).contains(csub)) {
			try {
			treeData.addItem(c, csub);
			} catch(Exception e) {
				e.printStackTrace();
				if(td.containsKey(csub)) {
				System.out.println("Reverse: "+td.get(csub).contains(c));
				}
			}
			td.get(c).add(csub);
		}
		Timer.end("PatternTree::addTreeData");
	}

	public Set<Node> getMapPatternTreeItem(OntologyClass pc) {
		String iri = keyString(pc);
		return mapPatternTree.containsKey(iri) ?mapPatternTree.get(iri) :Collections.emptySet();
	}
	private String keyString(OntologyClass pc) {
		return pc.getOWLClass().getIRI().toString();
	}

	public void expandLoad(Node pi) {
		loadSubTree(pi);
		expand(pi);
	}
	
	public void expandSelect(OntologyClass pc) {
		Timer.start("PatternTree::expandSelect");
		//System.out.print("PatternTree::ExpandSelect");
		addTreeItemsRecursively(pc.getNode(),new HashSet<>());
		 for (Node pp : getMapPatternTreeItem(pc)) {
			 expand(pp);
			 select(pp);
	         //TODO implement: 
	         //scrollTo(1);
         }
		 Timer.end("PatternTree::expandSelect");
	}
	
	private void addTreeItemsRecursively(Node c, Set<String> done) {
		String iri = keyString(c.getRepresentativeElement());
		if(done.contains(iri)) {
			return;
		} else {
			done.add(iri);
		}
		for(Node parent: c.directParents()) {
			addTreeItemsRecursively(parent,done);
			getMapPatternTreeItem(parent.getRepresentativeElement()).forEach(this::expandLoad);
		}
	}
}
