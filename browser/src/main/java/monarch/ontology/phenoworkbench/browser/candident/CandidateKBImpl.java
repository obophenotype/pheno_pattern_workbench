package monarch.ontology.phenoworkbench.browser.candident;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import monarch.ontology.phenoworkbench.analytics.pattern.generation.OntologyClass;
import monarch.ontology.phenoworkbench.analytics.pattern.reconciliation.Bucket;
import monarch.ontology.phenoworkbench.analytics.pattern.reconciliation.Candidate;

public class CandidateKBImpl implements CandidateKB {
	
	private final List<CandidateKBListener> listeners = new ArrayList<>();
	private final List<OntologyClass> classesInCurrentCandidate = new ArrayList<>();
	private final List<Bucket> buckets = new ArrayList<>();
    private final List<Candidate> candidates = new ArrayList<>();
    private final Set<OntologyClass> classesAcrossAllCandidatesInKB = new HashSet<>();
    private final Set<BlacklistItem> blacklist = new HashSet<>();
	
	public CandidateKBImpl() {
		
	}

	@Override
	public void addClassToCurrentCandidate(OntologyClass c) {
		classesInCurrentCandidate.add(c);
		refresh();
	}

	@Override
	public boolean isCandidateKBContainsClass(OntologyClass c) {
		return classesAcrossAllCandidatesInKB.contains(c);
	}

	@Override
	public boolean isCurrentCandidateContainsClass(OntologyClass c) {
		return classesInCurrentCandidate.contains(c);
	}

	@Override
	public void removeClassFromCurrentCandidate(OntologyClass c) {
		classesInCurrentCandidate.remove(c);
		refresh();
	}

	@Override
	public Collection<OntologyClass> getClassesForCurrentCandidate() {
		return classesInCurrentCandidate;
	}

	@Override
	public void clearClassesForCurrentCandidate() {
		classesInCurrentCandidate.clear();
		refresh();
	}

	@Override
	public void addClassesToCurrentCandidate(Set<OntologyClass> candidates) {
		classesInCurrentCandidate.addAll(candidates);
		refresh();
	}

	@Override
	public void setCurrentCandidate(Candidate c) {
		classesInCurrentCandidate.clear();
		classesInCurrentCandidate.addAll(c.getCandidates());
		refresh();
	}

	@Override
	public void removeCandidate(Candidate c) {
		candidates.remove(c);
		refreshAllClassAcrossCanidatesList();
	}

	@Override
	public Collection<Candidate> getAllCandidates() {
		return candidates;
	}

	@Override
	public void addCandidate(Candidate c) {
		candidates.add(c);
		refreshAllClassAcrossCanidatesList();
	}

	private void refreshAllClassAcrossCanidatesList() {
		classesAcrossAllCandidatesInKB.clear();
		getAllCandidates().forEach(c->classesAcrossAllCandidatesInKB.addAll(c.getCandidates()));
		refresh();
	}
	
    private void refresh() {
		listeners.forEach(l -> l.kbChange());
	}

	@Override
	public void addGridChangeListener(CandidateKBListener e) {
		listeners.add(e);
	}

	@Override
	public void saveCurrentCandidate() {
		Candidate c = new Candidate();
		c.addOntologyClasses(getClassesForCurrentCandidate());
		candidates.add(c);
		classesInCurrentCandidate.clear();
		refreshAllClassAcrossCanidatesList();
	}

	@Override
	public String exportCandidate() {
		ObjectMapper mapper = new ObjectMapper();		 
        ArrayNode arrayNode = mapper.createArrayNode();
        
        for(Candidate c:getAllCandidates()) {
			ArrayNode candidateArray = mapper.createArrayNode();
			ObjectNode n = mapper.createObjectNode();
	        n.put("description", c.getLabel());
	        n.putPOJO("classes",candidateArray);
	        for(OntologyClass oc:c.getCandidates()) {
	        		ObjectNode no = mapper.createObjectNode();
		        no.put("iri", oc.getIri());
		        candidateArray.add(no);
	        }
	        arrayNode.add(n);
		}
		try {
			return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(arrayNode);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return "empty";
	}

	@Override
	public void addBucket(Bucket bucket) {
		buckets.add(bucket);
		refresh();
	}

	@Override
	public void removeBucket(Bucket bucket) {
		buckets.remove(bucket);
		refresh();
	}

	@Override
	public Collection<Bucket> getBuckets() {
		return buckets;
	}

	@Override
	public String exportBuckets() {
		ObjectMapper mapper = new ObjectMapper();		 
        ArrayNode arrayNode = mapper.createArrayNode();
        
        for(Bucket c:getBuckets()) {
			ArrayNode candidateArray = mapper.createArrayNode();
			ObjectNode n = mapper.createObjectNode();
	        n.put("description", c.getLabel());
	        n.putPOJO("searches",candidateArray);
	        for(String oid:c.getBucket().keySet()) {
	        		ObjectNode no = mapper.createObjectNode();
		        no.put("oid", oid);
		        no.put("search", c.getBucket().get(oid));
		        candidateArray.add(no);
	        }
	        arrayNode.add(n);
		}
		try {
			return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(arrayNode);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return "empty";
	}

	@Override
	public void blacklistClassForCurrentCandidate(OntologyClass c) {
		for(OntologyClass current:getClassesForCurrentCandidate()) {
			blacklist(c, current);
		}
		refresh();
	}

	@Override
	public void blacklist(OntologyClass context, OntologyClass blacklistitem) {
		blacklist.add(new BlacklistItem(context,blacklistitem));
	}
	
	@Override
	public void blacklist(OntologyClass c) {
		blacklist.add(new BlacklistItem(c));
		refresh();
	}

	@Override
	public boolean isBlacklisted(OntologyClass c) {
		boolean blacklisted = false;
		
		if(blacklist.contains(new BlacklistItem(c))) {
			return true;
		}
		for(OntologyClass current:getClassesForCurrentCandidate()) {
			if(blacklist.contains(new BlacklistItem(current,c))) {
				return true;
			}
		}
		return blacklisted;
	}

	@Override
	public String exportBlacklist() {
		ObjectMapper mapper = new ObjectMapper();	
		ObjectNode root = mapper.createObjectNode();
        ArrayNode arrayBlacklist = mapper.createArrayNode();
        root.putPOJO("blacklist", arrayBlacklist);
        for(BlacklistItem c:blacklist) {
        		ObjectNode cb = mapper.createObjectNode();
        		cb.put("blacklisted",c.getBlacklisted().getIri());
        		cb.put("context", c.getContext().toString());
        		arrayBlacklist.add(cb);
        }
		try {
			return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return "empty";
	}

	@Override
	public void removeBlacklisted(OntologyClass c) {
		blacklist.remove(new BlacklistItem(c));
		refresh();
	}
	
	@Override
	public void removeBlacklisted(OntologyClass cl,OntologyClass remove) {
		blacklist.remove(new BlacklistItem(cl,remove));
		refresh();
	}

	@Override
	public Set<BlacklistItem> getBlacklist() {
		return blacklist;
	}

	@Override
	public void removeBlacklist(BlacklistItem c) {
		blacklist.remove(c);
		refresh();
	}

}