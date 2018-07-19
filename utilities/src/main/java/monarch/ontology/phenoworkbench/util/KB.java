package monarch.ontology.phenoworkbench.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import java.util.*;

public class KB implements CandidateKB{

    private static KB instance = null;
    //private final Downloader downloader = Downloader.getInstance();
    //private final Files fileSystem = Files.getInstance();
    private final List<CandidateKBListener> listeners = new ArrayList<>();
    private final List<OntologyClass> classesInCurrentCandidate = new ArrayList<>();
    private final List<Bucket> buckets = new ArrayList<>();
    private final List<Candidate> candidates = new ArrayList<>();
    private final Set<OntologyClass> classesAcrossAllCandidatesInKB = new HashSet<>();
    private final Set<BlacklistItem> blacklist = new HashSet<>();
    private final Map<String, OWLOntology> ontologyCache = new HashMap<>();
    private final Map<String,Map<String, OntologyClass>> classCache = new HashMap<>();
    private final ReconciliationCandidateSet mappings = new ReconciliationCandidateSet();
    private final ReconciliationCandidateSet mappingsBlacklist = new ReconciliationCandidateSet();
    private final Set<PatternReconciliationCandidate> mappingCandidatesBasedOnReconciliationCandidates = new HashSet<>();

    private KB() {
        addCandidateChangeListener(()->addCandidatesAsPairwiseMappings());
    }

    public static KB getInstance() {
        if (instance == null) {
            instance = new KB();
        }
        return instance;
    }

    public void clearOntologyCache() {
        ontologyCache.clear();
    }

    public Optional<OWLOntology> getOntology(String url) {
        if (!ontologyCache.containsKey(url)) {
            try {
                OWLOntology o = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(IRI.create(url));
                ontologyCache.put(url, o);
            } catch (OWLOntologyCreationException e) {
                e.printStackTrace();
            }
        }
        return Optional.ofNullable(ontologyCache.get(url));
    }

    public void addMapping(PatternReconciliationCandidate mapping) {
        mappings.addCandidate(mapping);
    }


    public ReconciliationCandidateSet getMappings() {
        return mappings;
    }

    private void addCandidatesAsPairwiseMappings() {
        mappings.removeCandidates(mappingCandidatesBasedOnReconciliationCandidates);
        for(Candidate c:getAllCandidates()) {
            for(OntologyClass c1:c.getCandidates()) {
                for(OntologyClass c2:c.getCandidates()) {
                    if(c1.equals(c2)){
                        continue;
                    }
                    mappingCandidatesBasedOnReconciliationCandidates.add(new PatternReconciliationCandidate(c1,c2));
                }
            }
        }
        mappings.addCandidates(mappingCandidatesBasedOnReconciliationCandidates);
    }

    public Set<OntologyClass> getOntologyClasses(String iri) {
            Set<OntologyClass> classes = new HashSet<>();
            for(String oid:classCache.keySet()) {
                if(classCache.get(oid).containsKey(iri)) {
                    classes.add(classCache.get(oid).get(iri));
                }
            }
            return classes;
    }

    public void cacheClasses(String oid, Set<OntologyClass> items) {
        Map<String,OntologyClass> classes  = new HashMap<>();
        items.forEach(e->classes.put(e.getIri(),e));
        classCache.put(oid,classes);
    }

    public void addClassToCurrentCandidate(OntologyClass c) {
        classesInCurrentCandidate.add(c);
        refresh();
    }

    public boolean isCandidateKBContainsClass(OntologyClass c) {
        return classesAcrossAllCandidatesInKB.contains(c);
    }

    public boolean isCurrentCandidateContainsClass(OntologyClass c) {
        return classesInCurrentCandidate.contains(c);
    }

    public void removeClassFromCurrentCandidate(OntologyClass c) {
        classesInCurrentCandidate.remove(c);
        refresh();
    }

    public Collection<OntologyClass> getClassesForCurrentCandidate() {
        return classesInCurrentCandidate;
    }

    public void clearClassesForCurrentCandidate() {
        classesInCurrentCandidate.clear();
        refresh();
    }

    public void addClassesToCurrentCandidate(Set<OntologyClass> candidates) {
        classesInCurrentCandidate.addAll(candidates);
        refresh();
    }

    public void setCurrentCandidate(Candidate c) {
        classesInCurrentCandidate.clear();
        classesInCurrentCandidate.addAll(c.getCandidates());
        refresh();
    }

    public void removeCandidate(Candidate c) {
        candidates.remove(c);
        refreshAllClassAcrossCanidatesList();
    }

    public Collection<Candidate> getAllCandidates() {
        return candidates;
    }

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

    public void addCandidateChangeListener(CandidateKBListener e) {
        listeners.add(e);
    }

    public void saveCurrentCandidate() {
        Candidate c = new Candidate();
        c.addOntologyClasses(getClassesForCurrentCandidate());
        candidates.add(c);
        classesInCurrentCandidate.clear();
        refreshAllClassAcrossCanidatesList();
    }

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

    public void addBucket(Bucket bucket) {
        buckets.add(bucket);
        refresh();
    }

    public void removeBucket(Bucket bucket) {
        buckets.remove(bucket);
        refresh();
    }

    public Collection<Bucket> getBuckets() {
        return buckets;
    }

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

    public void blacklistClassForCurrentCandidate(OntologyClass c) {
        for(OntologyClass current:getClassesForCurrentCandidate()) {
            blacklist(c, current);
        }
        refresh();
    }

    public void blacklist(OntologyClass context, OntologyClass blacklistitem) {
        blacklist.add(new BlacklistItem(context,blacklistitem));
    }

    public void blacklist(OntologyClass c) {
        blacklist.add(new BlacklistItem(c));
        refresh();
    }

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

    public void removeBlacklisted(OntologyClass c) {
        blacklist.remove(new BlacklistItem(c));
        refresh();
    }

    public void removeBlacklisted(OntologyClass cl,OntologyClass remove) {
        blacklist.remove(new BlacklistItem(cl,remove));
        refresh();
    }

    public Set<BlacklistItem> getBlacklist() {
        return blacklist;
    }

    public void removeBlacklist(BlacklistItem c) {
        blacklist.remove(c);
        refresh();
    }

    public boolean isValidatedMapping(PatternReconciliationCandidate s) {
        return mappings.containsReconciliationCandidate(s);
    }

    public void removeMapping(PatternReconciliationCandidate c) {
        mappings.removeCandidate(c);
    }

    public void blacklistMapping(PatternReconciliationCandidate c) {
        mappingsBlacklist.addCandidate(c);
        mappings.removeCandidate(c);
    }

    public void whitelistMapping(PatternReconciliationCandidate c) {
        mappingsBlacklist.removeCandidate(c);
    }

    public boolean isBlacklistedMapping(PatternReconciliationCandidate s) {
        return mappingsBlacklist.containsReconciliationCandidate(s);
    }

    public ReconciliationCandidateSet getMappingBlacklist() {
        return mappingsBlacklist;
    }
}
