package monarch.ontology.phenoworkbench.analytics.pattern.generation;

import monarch.ontology.phenoworkbench.util.Reasoner;
import monarch.ontology.phenoworkbench.util.RenderManager;
import monarch.ontology.phenoworkbench.util.Timer;
import org.semanticweb.owlapi.model.OWLClass;

import java.util.*;

public class PatternManager {

    private final Reasoner r;
    private final RenderManager renderManager;
    private final Map<OWLClass, OntologyClass> patternClassCache = new HashMap<>();
    private final Map<DefinedClass, Set<PatternGrammar>> patternSubsumedGrammarsMap = new HashMap<>();
    private final Set<DefinedClass> allDefinedClasses;


    public PatternManager(Set<DefinedClass> definedClasses, Reasoner r, PatternGenerator patternGenerator, RenderManager renderManager) {
        this.r = r;
        this.renderManager = renderManager;
        this.allDefinedClasses = definedClasses;
        preparePatterns(patternGenerator);
    }

    private void preparePatterns(PatternGenerator patternGenerator) {
        Timer.start("PatternManager::preparePatterns()");
        patternGenerator.setGrammar(allDefinedClasses);
        int ct_defined = 0;
        int ct_nondef = 0;
        for (DefinedClass p : allDefinedClasses) {
            if(p instanceof PatternClass) {
                ct_defined++;
            } else {
                ct_nondef++;
            }
            p.setLabel(renderManager.getLabel(p.getOWLClass()));
            p.setPatternString(renderPattern(p));
            patternClassCache.put(p.getOWLClass(), p);
        }
        allDefinedClasses.forEach(this::setTaxonomy);
        allDefinedClasses.forEach(this::setGrammar);
        System.out.println("Def: "+ct_defined+", nondef:"+ct_nondef);
        Timer.end("PatternManager::preparePatterns()");
    }

    private void setTaxonomy(DefinedClass p) {
        Timer.start("PatternManager::setTaxonomy()");
        r.getSubclassesOf(p.getOWLClass(),true,true).forEach(c->p.addChild(getPatternClass(c)));
        r.getSuperClassesOf(p.getOWLClass(),true,true).forEach(c->p.addParent(getPatternClass(c)));
        Timer.end("PatternManager::setTaxonomy()");
    }

    private void setGrammar(DefinedClass p) {
        Timer.start("PatternManager::setGrammar()");
        patternSubsumedGrammarsMap.put(p, new HashSet<>());
        indexGrammarsOfChildren(p);
        removeOwnGrammarIfExists(p);
        Timer.end("PatternManager::setGrammar()");
    }

    private void indexGrammarsOfChildren(DefinedClass p) {
        for(OntologyClass child:p.indirectParents()) {
            if (child instanceof DefinedClass) {
                patternSubsumedGrammarsMap.get(p).add(((DefinedClass) child).getGrammar());
            }
        }
    }

    private void removeOwnGrammarIfExists(DefinedClass p) {
        if( patternSubsumedGrammarsMap.get(p).contains(p.getGrammar())) {
            patternSubsumedGrammarsMap.get(p).remove(p.getGrammar());
        }
    }

    public Set<DefinedClass> getAllDefinedClasses() {
        return allDefinedClasses;
    }

    private String renderPattern(DefinedClass definedClass) {
        return renderManager.renderForMarkdown(definedClass.getDefiniton());
    }

    private OntologyClass getPatternClass(OWLClass c) {
        Timer.start("PatternManager::getPatternClass()");
        if (!patternClassCache.containsKey(c)) {
            OntologyClass p = new OntologyClass(c);
            p.setLabel(renderManager.getLabel(c));
            patternClassCache.put(c, p);
        }
        Timer.end("PatternManager::getPatternClass()");
        return patternClassCache.get(c);
    }

    public Set<PatternGrammar> getSubsumedGrammars(DefinedClass p) {
        Timer.start("PatternManager::getSubsumedGrammars()");
        //TODO Perhaps make mutable for performance?
        Set<PatternGrammar> grammars = new HashSet<>();
        if(patternSubsumedGrammarsMap.containsKey(p)) {
            grammars.addAll( patternSubsumedGrammarsMap.get(p));
        }
        Timer.end("PatternManager::getSubsumedGrammars()");
        return grammars;
    }

}
