package monarch.ontology.phenoworkbench.util;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import java.util.*;

public class KB {

    private static KB instance = null;
    //private final Downloader downloader = Downloader.getInstance();
    //private final Files fileSystem = Files.getInstance();
    private final Map<String, OWLOntology> ontologyCache = new HashMap<>();

    private KB() {
        // Exists only to defeat instantiation.
    }

    public static KB getInstance() {
        if (instance == null) {
            instance = new KB();
        }
        return instance;
    }

    public void clearCache() {
        ontologyCache.clear();
    }

    public Set<OWLOntology> getOntologies(Set<String> iris) {
        Set<OWLOntology> ontologies = new HashSet<>();
        iris.forEach(i -> getOntology(i).ifPresent(ontologies::add));
        return ontologies;
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

}
