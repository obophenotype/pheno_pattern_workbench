package monarch.ontology.phenoworkbench.browser.analytics;

import monarch.ontology.phenoworkbench.browser.util.OntologyUtils;
import org.semanticweb.owl.explanation.api.ExplanationGenerator;
import org.semanticweb.owl.explanation.api.ExplanationGeneratorFactory;
import org.semanticweb.owl.explanation.api.ExplanationManager;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import monarch.ontology.phenoworkbench.browser.util.Timer;

import java.io.File;
import java.util.*;

public class QuickImpact {

    private Timer timer = new Timer();
    private PatternImpact patternImpact;
    private UberOntology o;
    private Reasoner r;
    private ExplanationRenderer explanationRenderer;
    private Map<Pattern, PatternImpact.Impact> patternImpactMap = new HashMap<>();
    private Map<OWLClass, PatternClass> patternClassCache = new HashMap<>();
    private Map<Pattern, Set<PatternGrammar>> patternSubsumedGrammarsMap = new HashMap<>();
    private Set<Pattern> allPatterns = new HashSet<>();

    public QuickImpact(File corpus, File patternsfile, boolean imports, ImpactMode mode, int samplesize) {

        try {
            System.out.println("QI: Loading Uber Ontology: " + timer.getTimeElapsed());
            Imports i = imports ? Imports.INCLUDED : Imports.EXCLUDED;

            o = new UberOntology(i, corpus);
            System.out.println("QI: Initialising pattern generator.." + timer.getTimeElapsed());
            PatternGenerator patternGenerator = new PatternGenerator(o.getRender());
            System.out.println("QI: Create new Uber Ontology.." + timer.getTimeElapsed());
            OWLOntology all = o.createNewUberOntology();
            System.out.println("QI: Preparing patterns" + timer.getTimeElapsed());
            Set<Pattern> patterns = preparePatterns(patternsfile, mode, samplesize, i, patternGenerator, all);
            System.out.println("QI: Preparing pattern reasoner" + timer.getTimeElapsed());
            r = preparePatternReasoner(patterns, all);
            explanationRenderer = new ExplanationRenderer(o.getRender());

            allPatterns.addAll(patterns);
            System.out.println("QI: Extract definition patterns.." + timer.getTimeElapsed());
            allPatterns.addAll(patternGenerator.extractPatterns(o.getAllAxioms(), true));
            System.out.println("QI: Preparing pattern grammar and labels.." + timer.getTimeElapsed());
            preparePatternsGrammarsAndLabels(patternGenerator);
            System.out.println("QI: Preparing pattern impact" + timer.getTimeElapsed());
            patternImpact = new PatternImpact(o, r.getOWLReasoner(), r.getUnsatisfiableClasses(), new HashSet<>());
            System.out.println("QI: Computing impact.." + timer.getTimeElapsed());
            patternImpactMap = patternImpact.getImpactMap(allPatterns);
            System.out.println("QI: Done.." + timer.getTimeElapsed());

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
                o.getRender().addLabel(o_patterns);
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

    private void preparePatternsGrammarsAndLabels(PatternGenerator patternGenerator) {
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


    public Explanation getSubsumptionExplanation(PatternClass c, PatternClass p) {
        return r.getExplanation(c.getOWLClass(),p.getOWLClass());
    }

    public String getSubsumptionExplanationRendered(PatternClass c, PatternClass p) {
        return explanationRenderer.renderExplanation(getSubsumptionExplanation(c,p));
    }
}
