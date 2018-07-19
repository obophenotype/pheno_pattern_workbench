package monarch.ontology.phenoworkbench.analytics.pattern.generation;

import monarch.ontology.phenoworkbench.util.DefinedClass;
import monarch.ontology.phenoworkbench.util.OntologyClass;
import monarch.ontology.phenoworkbench.util.PatternClass;
import monarch.ontology.phenoworkbench.util.Timer;
import org.semanticweb.owlapi.model.OWLClass;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class PatternProviderDefaultImpl implements PatternProvider {

    private final PatternManager man;

    public PatternProviderDefaultImpl(PatternManager man) {
        this.man = man;
    }

    @Override
    public Set<PatternClass> getTopPatterns(boolean excludeObsolete) {
        Timer.start("PatternProviderDefaultImpl::getTopPatterns()");
        Set<PatternClass> patterns = new HashSet<>();
        Timer.start("PatternProviderDefaultImpl::getTopPatterns()::getPatternsAmongDefinedClasses");
        Set<PatternClass> allPatternClasses = getPatternsAmongDefinedClasses(excludeObsolete);
        Set<OWLClass> allPatternOWLClasses = allPatternClasses.stream().map(PatternClass::getOWLClass).collect(Collectors.toSet());
        Timer.end("PatternProviderDefaultImpl::getTopPatterns()::getPatternsAmongDefinedClasses");

        for (PatternClass c : allPatternClasses) {
            Timer.start("PatternProviderDefaultImpl::getTopPatterns()::getSuperClasses:noneMatch");
            Set<OWLClass> indirectParents = new HashSet<>();
            c.getNode().indirectParents().forEach(n->n.getEquivalenceGroup().forEach(e->indirectParents.add(e.getOWLClass())));
            if (indirectParents.stream().noneMatch(parent -> allPatternOWLClasses.contains(parent))) {
                patterns.add(c);
            }
            Timer.end("PatternProviderDefaultImpl::getTopPatterns()::getSuperClasses:noneMatch");
        }
        System.out.println("PatternProviderDefaultImpl::TOP" + patterns.size());
        System.out.println("PatternProviderDefaultImpl::ALLPATTERN" + allPatternClasses.size());

        Timer.end("PatternProviderDefaultImpl::getTopPatterns()");
        return patterns;
    }

    @Override
    public Set<DefinedClass> getTopDefinedClasses(boolean excludeObsolete) {
        //TODO Fix this method
        System.err.println("PatternProviderDefaultImpl::getTopDefinedClasses does not work");
        Timer.start("PatternProviderDefaultImpl::getTopDefinedClasses()");
        Set<DefinedClass> patterns = new HashSet<>();
        Timer.start("PatternProviderDefaultImpl::getTopDefinedClasses()::getPatternsAmongDefinedClasses");
        Set<DefinedClass> allPatternClasses = getAllDefinedClasses(excludeObsolete);
        Set<OWLClass> allPatternOWLClasses = allPatternClasses.stream().map(DefinedClass::getOWLClass).collect(Collectors.toSet());
        Timer.end("PatternProviderDefaultImpl::getTopDefinedClasses()::getPatternsAmongDefinedClasses");

        for (DefinedClass c : allPatternClasses) {
            Timer.start("PatternProviderDefaultImpl::getTopDefinedClasses()::getSuperClasses:noneMatch");
            Set<OWLClass> indirectParents = new HashSet<>();
            c.getNode().indirectParents().forEach(n->n.getEquivalenceGroup().forEach(e->indirectParents.add(e.getOWLClass())));
            if (indirectParents.stream().noneMatch(parent -> allPatternOWLClasses.contains(parent))) {
                patterns.add(c);
            }
            Timer.end("PatternProviderDefaultImpl::getTopDefinedClasses()::getSuperClasses:noneMatch");
        }
        System.out.println("PatternProviderDefaultImplDef::TOP" + patterns.size());
        System.out.println("PatternProviderDefaultImplDef::ALLPATTERN" + allPatternClasses.size());

        Timer.end("PatternProviderDefaultImpl::getTopDefinedClasses()");
        return patterns;
    }

    @Override
    public Set<PatternClass> getPatternsAmongDefinedClasses(boolean excludeObsolete) {
        return getAllDefinedClasses(excludeObsolete).stream().filter(PatternClass.class::isInstance).map(PatternClass.class::cast).collect(Collectors.toSet());
    }

    @Override
    public Set<DefinedClass> getAllDefinedClasses(boolean excludeObsolete) {
        return man.getAllDefinedClasses().stream().filter(d->!excludeObsolete||!d.isDeprecated()).collect(Collectors.toSet());
    }

    @Override
    public Set<OntologyClass> getTopOntologyClasses(boolean excludeObsolete) {
        return man.getAllClasses().stream().filter(c->c.getNode().directParents().isEmpty()&&(!excludeObsolete||!c.isDeprecated())).collect(Collectors.toSet());
    }
}
