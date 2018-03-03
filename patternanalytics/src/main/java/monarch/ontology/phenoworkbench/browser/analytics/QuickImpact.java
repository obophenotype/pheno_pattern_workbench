package monarch.ontology.phenoworkbench.browser.analytics;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;

import java.io.File;
import java.util.*;

public class QuickImpact {

    private PatternImpact patternImpact;
    private UberOntology o;
    private Reasoner r;
    private Map<Pattern, PatternImpact.Impact> patternImpactMap = new HashMap<>();
    private Map<OWLClass, PatternClass> patternClassCache = new HashMap<>();
    private Map<Pattern, Set<PatternGrammar>> patternSubsumedGrammarsMap = new HashMap<>();
    private Set<Pattern> allPatterns = new HashSet<>();

    public QuickImpact(File corpus, File patternsfile, boolean imports, ImpactMode mode, int samplesize) {

        try {

            Imports i = imports ? Imports.INCLUDED : Imports.EXCLUDED;

            o = new UberOntology(i, corpus);
            PatternGenerator patternGenerator = new PatternGenerator(o.getRender());
            OWLOntology all = o.createNewUberOntology();
            Set<Pattern> patterns = preparePatterns(patternsfile, mode, samplesize, i, patternGenerator, all);
            r = preparePatternReasoner(patterns, all);

            allPatterns.addAll(patterns);
            allPatterns.addAll(patternGenerator.extractPatterns(o.getAllAxioms(), true));
            prepareAllPatterns(patternGenerator);
            patternImpact = new PatternImpact(o, r.getOWLReasoner(), r.getUnsatisfiableClasses(), new HashSet<>());
            patternImpactMap = patternImpact.getImpactMap(allPatterns);

        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }

    }

    private Set<Pattern> preparePatterns(File patternsfile, ImpactMode mode, int samplesize, Imports i, PatternGenerator patternGenerator, OWLOntology all) throws OWLOntologyCreationException {
        Set<Pattern> patterns = new HashSet<>();
        switch (mode) {
            case EXTERNAL:
                OWLOntology o_patterns = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(patternsfile);
                patterns.addAll(patternGenerator.extractPatterns(o_patterns.getAxioms(i), false));
                break;
            case ALL:
                patterns.addAll(patternGenerator.generateDefinitionPatterns(all.getAxioms(i), new Reasoner(all).getOWLReasoner(),samplesize));
                break;
            case THING:
                patterns.addAll(patternGenerator.generateHighLevelDefinitionPatterns(all.getAxioms(i)));
                break;
            default:
                patterns.addAll(patternGenerator.generateHighLevelDefinitionPatterns(all.getAxioms(i)));
        }
        return patterns;
    }

    private Reasoner preparePatternReasoner(Set<Pattern> patterns, OWLOntology uberOntology) {
        OWLDataFactory df = OWLManager.getOWLDataFactory();
        Set<OWLAxiom> patternAxioms = new HashSet<>();
        for(Pattern p:patterns) {
            patternAxioms.add(df.getOWLEquivalentClassesAxiom(p.getOWLClass(),p.getDefiniton()));
        }
        uberOntology.getOWLOntologyManager().addAxioms(uberOntology,patternAxioms);
        return new Reasoner(uberOntology);
    }

    private void prepareAllPatterns(PatternGenerator patternGenerator) {
        patternGenerator.setGrammar(allPatterns);
        Set<Pattern> tmp = new HashSet<>(allPatterns);
        allPatterns.clear();
        allPatterns.addAll(tmp);
        tmp.clear();
        int ct_defined = 0;
        int ct_nondef = 0;
        for (Pattern p : allPatterns) {
            if(p.isDefinedclass()) {
                ct_defined++;
            } else {
                ct_nondef++;
            }
            p.setLabel(o.getRender().getLabel(p.getOWLClass()));
            p.setPatternString(renderPattern(p));
            patternClassCache.put(p.getOWLClass(), p);
            patternSubsumedGrammarsMap.put(p, new HashSet<>());
            for (PatternClass child : getChildrenPatterns(p, false)) {
                if (child instanceof Pattern) {
                    patternSubsumedGrammarsMap.get(p).add(((Pattern) child).getGrammar());
                }
            }
            if( patternSubsumedGrammarsMap.get(p).contains(p.getGrammar())) {
                patternSubsumedGrammarsMap.get(p).remove(p.getGrammar());
            }
        }
        System.out.println("Def: "+ct_defined+", nondef:"+ct_nondef);
    }

    public Set<PatternGrammar> getSubsumedGrammars(Pattern p) {
        Set<PatternGrammar> grammars = new HashSet<>();
        if(patternSubsumedGrammarsMap.containsKey(p)) {
            grammars.addAll( patternSubsumedGrammarsMap.get(p));
        } else {
            /*System.out.println(":::"+p+"|"+p.hashCode());
            patternSubsumedGrammarsMap.keySet().stream().filter(pp->!pp.isDefinedclass()).forEach(ppp->System.out.println(ppp+"|"+ppp.hashCode()));*/
        }
        return grammars;
    }

    public Set<Pattern> getAllPatterns() {
        return allPatterns;
    }

    public Set<PatternClass> getTopPatterns() {
        Set<PatternClass> patterns = new HashSet<>();
        Set<OWLClass> patternClasses = new HashSet<>();
        allPatterns.forEach(p -> {
            if (!p.isDefinedclass()) patternClasses.add(p.getOWLClass());
        });
        for (Pattern c : allPatterns) {
            if (r.getSuperClassesOf(c.getOWLClass(), false).stream().noneMatch(patternClasses::contains)) {
                patterns.add(c);
            }

        }
        return patterns;
    }

    private Set<PatternClass> asPatterns(Collection<? extends OWLClass> classes) {
        Set<PatternClass> patterns = new HashSet<>();
        for (OWLClass c : classes) {
            patterns.add(getPatternClass(c));
        }
        return patterns;
    }

    private PatternClass getPatternClass(OWLClass c) {
        if (!patternClassCache.containsKey(c)) {
            PatternClass p = new PatternClass(c);
            p.setLabel(o.getRender().getLabel(c));
            patternClassCache.put(c, p);
        }
        return patternClassCache.get(c);
    }

    public Set<PatternClass> getDirectChildren(PatternClass c) {
        return getChildrenPatterns(c, true);
    }

    private Set<PatternClass> getChildrenPatterns(PatternClass p, boolean direct) {
        return asPatterns(getChildren(p.getOWLClass(), direct));
    }

    public Set<PatternClass> getParentPatterns(PatternClass pc, boolean direct) {
        return asPatterns(getParents(pc.getOWLClass(), direct));
    }

    private Set<OWLClass> getParents(OWLClass c, boolean direct) {
        Set<OWLClass> patterns = new HashSet<>(r.getSuperClassesOf(c, direct));
        patterns.removeAll(r.getUnsatisfiableClasses());
        return patterns;
    }

    private Set<OWLClass> getChildren(OWLClass c, boolean direct) {
        Set<OWLClass> patterns = new HashSet<>(r.getSubclassesOf(c, direct));
        patterns.removeAll(r.getUnsatisfiableClasses());
        return patterns;
    }

    public PatternImpact.Impact getImpact(Pattern c) {
        if (patternImpactMap.containsKey(c)) {
            return patternImpactMap.get(c);
        }
        return patternImpact.noImpact(c);
    }

    public String renderPattern(Pattern pattern) {
        return o.getRender().renderForMarkdown(pattern.getDefiniton());
    }


}
