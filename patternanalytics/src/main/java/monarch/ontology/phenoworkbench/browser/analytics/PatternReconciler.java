package monarch.ontology.phenoworkbench.browser.analytics;

import monarch.ontology.phenoworkbench.browser.util.RenderManager;
import monarch.ontology.phenoworkbench.browser.util.Timer;
import org.apache.commons.io.FileUtils;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

public class PatternReconciler {

    private Timer timer = new Timer();
    private PatternImpact patternImpact;
    private UberOntology o;
    private Reasoner r;
    private Map<Pattern, PatternImpact.Impact> patternImpactMap = new HashMap<>();
    private Map<OWLClass, PatternClass> patternClassCache = new HashMap<>();
    private Map<Pattern, Set<PatternGrammar>> patternSubsumedGrammarsMap = new HashMap<>();
    private Set<Pattern> allPatterns = new HashSet<>();
    private Map<Pattern,Set<Pattern>> mappedPatterns;
    private Map<Pattern,Map<Pattern,PatternReconciliation>> patternReconciliation = new HashMap<>();
    private List<PatternReconciliation> reconciliations = new ArrayList<>();


    public PatternReconciler(File corpus, File mappings, boolean imports, boolean lazyalign, boolean bidirectionmapping) {

        try {
            System.out.println("QI: Loading Uber Ontology: " + timer.getTimeElapsed());
            Imports i = imports ? Imports.INCLUDED : Imports.EXCLUDED;
            o = new UberOntology(i, corpus);
            System.out.println("QI: Initialising pattern generator.." + timer.getTimeElapsed());
            PatternGenerator patternGenerator = new PatternGenerator(o.getRender());
            System.out.println("QI: Create new Uber Ontology.." + timer.getTimeElapsed());
            OWLOntology all = o.createNewUberOntology();
            System.out.println("Done... Axiomct: " +o.getAllAxioms().size()+ timer.getTimeElapsed());
            System.out.println("QI: Preparing pattern reasoner" + timer.getTimeElapsed());
            r = new Reasoner(all);
            allPatterns.addAll(patternGenerator.extractPatterns(all.getAxioms(i), true));
            preparePatternsGrammarsAndLabels(patternGenerator);
            System.out.println("QI: Preparing pattern impact" + timer.getTimeElapsed());
            System.out.println("QI: Preparing patterns" + timer.getTimeElapsed());
            mappedPatterns = preparePatterns(mappings, i, patternGenerator, all,bidirectionmapping);
            //patternImpact = new PatternImpact(o, r.getOWLReasoner(), r.getUnsatisfiableClasses(), new HashSet<>());
            System.out.println("QI: Computing impact.." + timer.getTimeElapsed());
            //patternImpactMap = patternImpact.getImpactMap(allPatterns);
            if(!lazyalign) {
                System.out.println("QI: Computing alignments.." + timer.getTimeElapsed());
                for(Pattern p:mappedPatterns.keySet()) {
                    if(!patternReconciliation.containsKey(p)) {
                            patternReconciliation.put(p,new HashMap<>());
                    }

                    for(Pattern p2:mappedPatterns.get(p)) {
                        if(!patternReconciliation.get(p).containsKey(p2)) {
                            PatternReconciliation pr = new PatternReconciliation(p,p2,o.getRender(),r);
                            patternReconciliation.get(p).put(p2,pr);
                            reconciliations.add(pr);
                        }
                    }
                }
            }
            System.out.println("QI: Done.." + timer.getTimeElapsed());


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private Map<Pattern,Set<Pattern>> preparePatterns(File mappinFile, Imports i, PatternGenerator patternGenerator, OWLOntology all, boolean bidirectionmapping) {
        Map<Pattern,Set<Pattern>> patterns = new HashMap<>();
        Map<IRI,Set<IRI>> mapping = parseMappings(mappinFile);
        Map<IRI,Pattern> iriPatternMap = new HashMap<>();

        allPatterns.forEach(p->iriPatternMap.put(p.getOWLClass().getIRI(),p));
        for(IRI iri:mapping.keySet()) {
            if(iriPatternMap.containsKey(iri)) {
                Pattern p = iriPatternMap.get(iri);
                if(!patterns.containsKey(p)) {
                    patterns.put(p,new HashSet<>());
                }
                for (IRI to : mapping.get(iri)) {
                    if (iriPatternMap.containsKey(to)) {
                        Pattern p2 = iriPatternMap.get(to);
                        patterns.get(p).add(p2);
                        if(bidirectionmapping) {
                            if(!patterns.containsKey(p2)) {
                                patterns.put(p2,new HashSet<>());
                            }
                            patterns.get(p2).add(p);
                        }
                    }

                }
            }
        }
        return patterns;
    }

    private Map<IRI,Set<IRI>> parseMappings(File mappinFile) {
        Map<IRI,Set<IRI>> mappings = new HashMap<>();
        if(mappinFile.isFile()) {
            try {
                List<String> lines = FileUtils.readLines(mappinFile, Charset.forName("utf-8"));
                for(String line:lines) {
                    String[] explosion = line.split(",");
                    String i1 = explosion[0];
                    String i2 = explosion[1];
                    IRI iri1 = IRI.create(i1);
                    IRI iri2 = IRI.create(i2);
                    if(!mappings.containsKey(iri1)) {
                        mappings.put(iri1, new HashSet<>());
                    }
                    mappings.get(iri1).add(iri2);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return mappings;
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


    public PatternReconciliation getPatternAlignment(Pattern p1, Pattern p2) {
        if(!patternReconciliation.containsKey(p1)) {
            patternReconciliation.put(p1,new HashMap<>());
        }
        if(!patternReconciliation.get(p1).containsKey(p2)) {
            PatternReconciliation pr = new PatternReconciliation(p1,p2,o.getRender(),r);
            patternReconciliation.get(p1).put(p2, pr);
            reconciliations.add(pr);
        }
        return patternReconciliation.get(p1).get(p2);
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

    public List<PatternReconciliation> getAllPatternReconciliations() {
        return new ArrayList<>(reconciliations);
    }

}
