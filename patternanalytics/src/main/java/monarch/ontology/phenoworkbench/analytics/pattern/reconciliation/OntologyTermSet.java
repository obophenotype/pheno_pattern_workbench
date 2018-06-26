package monarch.ontology.phenoworkbench.analytics.pattern.reconciliation;

import monarch.ontology.phenoworkbench.analytics.pattern.generation.*;
import monarch.ontology.phenoworkbench.analytics.pattern.impact.OntologyClassImpact;
import monarch.ontology.phenoworkbench.util.ExplanationAnalyser;
import monarch.ontology.phenoworkbench.util.OntologyEntry;
import monarch.ontology.phenoworkbench.util.Reasoner;
import monarch.ontology.phenoworkbench.util.RenderManager;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.parameters.Imports;

import java.util.*;
import java.util.stream.Collectors;

public class OntologyTermSet implements GrammarProvider, ImpactProvider,ExplanationRenderProvider {

    private PatternManager patternManager;
    private final OntologyEntry entry;
    private Reasoner r;
    private RenderManager renderManager;
    private Map<String,OntologyClass> classes = new HashMap<>();
    private ExplanationRenderProvider explanationProvider;

    OntologyTermSet(OntologyEntry entry,Set<OWLAxiom> axioms, PatternGenerator patternGenerator, RenderManager renderManager) {
        this.renderManager = renderManager;
        this.entry = entry;
        try {
            OWLOntology o1 = OWLManager.createOWLOntologyManager().createOntology(axioms);
            r = new Reasoner(o1);
            preparePatternManager(patternGenerator, o1);
            patternManager.getAllClasses().forEach(o->classes.put(o.getIri(),o));
            explanationProvider = new DefaultExplanationProvider(r,renderManager);
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }
    }

    public Set<OntologyClass> items() {
        return new HashSet<>(classes.values());
    }

    public Set<OntologyClass> items(boolean roots) {
        return roots ? items(entry.getRoots()) : items();
    }

    private Set<OntologyClass> items(Set<String> roots) {
        Set<OntologyClass> classesUnderRoots = new HashSet<>();
            for (String iri : roots) {
                if(classes.containsKey(iri)) {
                    classesUnderRoots.addAll(classes.get(iri).indirectChildren());
                }
            }
        return classesUnderRoots;
    }

    private void preparePatternManager(PatternGenerator patternGenerator, OWLOntology all) {
        patternManager = new PatternManager(extractDefined(patternGenerator, all.getAxioms(Imports.INCLUDED)), r, patternGenerator, renderManager);
    }

    private Set<DefinedClass> extractDefined(PatternGenerator patternGenerator, Set<OWLAxiom> axioms) {
        return patternGenerator.extractDefinedClasses(axioms, true);
    }

    @Override
    public Set<PatternGrammar> getSubsumedGrammars(DefinedClass p) {
        return patternManager.getSubsumedGrammars(p);
    }

    @Override
    public int getInstanceCount(PatternGrammar grammar) {
        return 0;
    }

    @Override
    public Optional<OntologyClassImpact> getImpact(OntologyClass c) {
        return Optional.empty();
    }

    public String getOid() {
        return entry.getOid();
    }

    @Override
    public Optional<ExplanationAnalyser> getSubsumptionExplanationRendered(OntologyClass current, OntologyClass p) {
        return explanationProvider.getSubsumptionExplanationRendered(current,p);
    }

    public Optional<OntologyClass> getTermByIRI(String iri) {
        if(classes.containsKey(iri)) {
            return Optional.of(classes.get(iri));
        }
        return Optional.empty();
    }

    public Collection<OntologyClass> searchTerms(String s) {
        return classes.values().stream().filter(o->matchesSearch(o,s)).collect(Collectors.toSet());
    }

    private boolean matchesSearch(OntologyClass o, String s) {
        //TODO extend to take into account additional fields
        return o.getLabel().matches(s);
    }
}
