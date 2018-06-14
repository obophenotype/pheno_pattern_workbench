package monarch.ontology.phenoworkbench.browser.candident;

import java.util.Collection;
import java.util.Set;

import monarch.ontology.phenoworkbench.analytics.pattern.generation.OntologyClass;
import monarch.ontology.phenoworkbench.analytics.pattern.reconciliation.Bucket;
import monarch.ontology.phenoworkbench.analytics.pattern.reconciliation.Candidate;

public interface CandidateKB {

	void addClassToCurrentCandidate(OntologyClass recon);

	boolean isCandidateKBContainsClass(OntologyClass s);

	boolean isCurrentCandidateContainsClass(OntologyClass s);

	void removeClassFromCurrentCandidate(OntologyClass c);

	Collection<OntologyClass> getClassesForCurrentCandidate();

	void clearClassesForCurrentCandidate();

	void addClassesToCurrentCandidate(Set<OntologyClass> candidates);

	void setCurrentCandidate(Candidate c);

	void removeCandidate(Candidate c);

	Collection<Candidate> getAllCandidates();

	void addCandidate(Candidate c);

	void addGridChangeListener(CandidateKBListener listener);

	void saveCurrentCandidate();

	String exportCandidate();

	void addBucket(Bucket bucket);

	void removeBucket(Bucket c);

	Collection<Bucket> getBuckets();

	String exportBuckets();

	void blacklistClassForCurrentCandidate(OntologyClass c);

	boolean isBlacklisted(OntologyClass s);

	void blacklist(OntologyClass c);

	void blacklist(OntologyClass context, OntologyClass blacklist);

	String exportBlacklist();

	Set<BlacklistItem> getBlacklist();

	void removeBlacklisted(OntologyClass cl, OntologyClass remove);

	void removeBlacklisted(OntologyClass c);

	void removeBlacklist(BlacklistItem c);

}
