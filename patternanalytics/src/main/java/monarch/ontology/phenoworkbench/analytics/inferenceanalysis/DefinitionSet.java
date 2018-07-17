package monarch.ontology.phenoworkbench.analytics.inferenceanalysis;

import monarch.ontology.phenoworkbench.util.DefinedClass;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;


import java.util.*;
import java.util.stream.Collectors;

public class DefinitionSet {
    private Map<OWLClass, DefinedClass> definedClasses = new HashMap<>();
    private Set<OWLClassExpression> definedClassesDefinition = new HashSet<>();
    private Set<DefinedClass> definitions = new HashSet<>();

    public void setDefinitions(Set<DefinedClass> definitions) {
        this.definitions = definitions;
        this.definedClasses.clear();
        this.definitions.forEach(d -> definedClasses.put(d.getOWLClass(), d));
        this.definedClassesDefinition.addAll(definitions.stream().map(DefinedClass::getDefiniton).collect(Collectors.toSet()));
    }

    public Set<DefinedClass> getDefinitions() {
        return definitions;
    }

    public Set<OWLClass> getDefinedClasses() {
        return definedClasses.keySet();
    }

    public Set<OWLClassExpression> getDefinedClassDefinitions() {
        return definedClassesDefinition;
    }

    public Optional<DefinedClass> getDefinedClass(OWLClass c) {
        if(definedClasses.containsKey(c)) {
            return Optional.of(definedClasses.get(c));
        }
        return Optional.empty();
    }

    public boolean containsDefinedClass(OWLClass c) {
        return definedClasses.containsKey(c);
    }
}
