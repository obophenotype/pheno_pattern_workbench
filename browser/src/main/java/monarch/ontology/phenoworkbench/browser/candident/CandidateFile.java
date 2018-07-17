package monarch.ontology.phenoworkbench.browser.candident;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import monarch.ontology.phenoworkbench.util.Candidate;

public class CandidateFile {
	@JsonProperty
    private List<Candidate> candidates;

	public List<Candidate> getCandidates() {
		return candidates;
	}
}
