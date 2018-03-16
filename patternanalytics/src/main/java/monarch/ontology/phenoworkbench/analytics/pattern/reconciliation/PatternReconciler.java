package monarch.ontology.phenoworkbench.analytics.pattern.reconciliation;

import monarch.ontology.phenoworkbench.analytics.pattern.generation.PatternGenerator;
import monarch.ontology.phenoworkbench.analytics.pattern.PatternGrammar;
import monarch.ontology.phenoworkbench.analytics.pattern.impact.Impact;
import monarch.ontology.phenoworkbench.analytics.pattern.impact.PatternImpact;
import monarch.ontology.phenoworkbench.analytics.pattern.Pattern;
import monarch.ontology.phenoworkbench.analytics.pattern.PatternClass;
import monarch.ontology.phenoworkbench.util.Reasoner;
import monarch.ontology.phenoworkbench.util.Timer;
import monarch.ontology.phenoworkbench.util.UberOntology;
import org.apache.commons.io.FileUtils;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;

import java.io.File;
import java.nio.charset.Charset;
import java.util.*;

public class PatternReconciler {

    private monarch.ontology.phenoworkbench.util.Timer timer = new Timer();
    private PatternImpact patternImpact;
    private UberOntology o;
    private Reasoner r;
    private Map<Pattern, Impact> patternImpactMap = new HashMap<>();
    private Map<OWLClass, PatternClass> patternClassCache = new HashMap<>();
    private Map<Pattern, Set<PatternGrammar>> patternSubsumedGrammarsMap = new HashMap<>();
    private Set<Pattern> allPatterns = new HashSet<>();
    private Map<Pattern, Map<Pattern, PatternReconciliation>> patternReconciliation;
    private List<PatternReconciliation> reconciliations = new ArrayList<>();


    public PatternReconciler(File corpus, File mappings, boolean imports, boolean lazyalign, boolean bidirectionmapping, double confidencethreshold) {

        try {
            System.out.println("QI: Loading Uber Ontology: " + timer.getTimeElapsed());
            Imports i = imports ? Imports.INCLUDED : Imports.EXCLUDED;
            o = new UberOntology(i, corpus);
            System.out.println("QI: Initialising pattern generator.." + timer.getTimeElapsed());
            PatternGenerator patternGenerator = new PatternGenerator(o.getRender());
            System.out.println("QI: Create new Uber Ontology.." + timer.getTimeElapsed());
            OWLOntology all = o.createNewUberOntology();
            System.out.println("Done... Axiomct: " + o.getAllAxioms().size() + timer.getTimeElapsed());
            System.out.println("QI: Preparing pattern reasoner" + timer.getTimeElapsed());
            r = new Reasoner(all);
            allPatterns.addAll(patternGenerator.extractPatterns(all.getAxioms(i), true));
            preparePatternsGrammarsAndLabels(patternGenerator);
            System.out.println("QI: Preparing pattern impact" + timer.getTimeElapsed());
            System.out.println("QI: Preparing patterns" + timer.getTimeElapsed());
            patternReconciliation = preparePatternMap(mappings, bidirectionmapping, lazyalign, confidencethreshold);
            //patternImpact = new PatternImpact(o, r.getOWLReasoner(), r.getUnsatisfiableClasses(), new HashSet<>());
            System.out.println("QI: Computing impact.." + timer.getTimeElapsed());
            //patternImpactMap = patternImpact.getImpactMap(allPatterns);

            System.out.println("QI: Done.." + timer.getTimeElapsed());


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private Map<Pattern, Map<Pattern, PatternReconciliation>> preparePatternMap(File mappinFile, boolean bidirectionmapping, boolean lazyalign, double confidencethreshold) {
        Map<Pattern, Map<Pattern, PatternReconciliation>> patternReconciliation = new HashMap<>();
        List<IRIMapping> mapping = parseMappings(mappinFile, confidencethreshold);
        Map<IRI, Pattern> iriPatternMap = new HashMap<>();

        allPatterns.forEach(p -> iriPatternMap.put(p.getOWLClass().getIRI(), p));
        if (!lazyalign) {
            System.out.println("QI: Computing alignments.." + timer.getTimeElapsed());

            for (IRIMapping imap : mapping) {
                IRI iri = imap.getI1();
                IRI to = imap.getI2();
                if (iriPatternMap.containsKey(iri) && iriPatternMap.containsKey(to)) {
                    Pattern p = iriPatternMap.get(iri);
                    Pattern p2 = iriPatternMap.get(to);
                    if (!patternReconciliation.containsKey(p)) {
                        patternReconciliation.put(p, new HashMap<>());
                    }
                    if (!patternReconciliation.get(p).containsKey(p2)) {
                        PatternReconciliation pr = new PatternReconciliation(p, p2, o.getRender(), r);
                        pr.setJaccardSimiliarity(imap.getJackard());
                        pr.setSubclassSimilarity(imap.getSbcl());
                        patternReconciliation.get(p).put(p2, pr);
                        reconciliations.add(pr);
                    }
                }


            }
        }
        return patternReconciliation;
    }

    /*
     if (!patterns.containsKey(p)) {
                    patterns.put(p, new HashSet<>());
                }

                if (iriPatternMap.containsKey(to)) {
                    Pattern p2 = iriPatternMap.get(to);
                    patterns.get(p).add(p2);
                    if (bidirectionmapping) {
                        if (!patterns.containsKey(p2)) {
                            patterns.put(p2, new HashSet<>());
                        }
                        patterns.get(p2).add(p);
                    }
                }
     */

    private List<IRIMapping> parseMappings(File mappinFile, double confidencethreshold) {

        List<IRIMapping> mappings = new ArrayList<>();
        if (mappinFile.isFile()) {
            try {
                List<String> lines = FileUtils.readLines(mappinFile, Charset.forName("utf-8"));
                for (String line : lines) {
                    String[] explosion = line.split("\t");
                    IRI iri1 = getIRI(explosion[0]);
                    String logo1 = explosion[1];
                    IRI iri2 = getIRI(explosion[2]);
                    String logo2 = explosion[3];
                    Double v1 = Double.valueOf(explosion[4]);
                    Double v2 = Double.valueOf(explosion[5]);
                    if (v1 < 0 || v1 > confidencethreshold) {
                        mappings.add(new IRIMapping(iri1, iri2, v1, v2));
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return mappings;
    }

    private IRI getIRI(String s) {
        String v = s;
        if (v.contains(":")) {
            String prefix = v.substring(0, v.indexOf(":"));
            v = v.replace(prefix + ":", "http://purl.obolibrary.org/obo/" + prefix + "_");
        }
        return IRI.create(v);
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
            if (p.isDefinedclass()) {
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
            if (patternSubsumedGrammarsMap.get(p).contains(p.getGrammar())) {
                patternSubsumedGrammarsMap.get(p).remove(p.getGrammar());
            }
        }
        System.out.println("Def: " + ct_defined + ", nondef:" + ct_nondef);
    }


    public PatternReconciliation getPatternAlignment(Pattern p1, Pattern p2) {
        if (!patternReconciliation.containsKey(p1)) {
            patternReconciliation.put(p1, new HashMap<>());
        }
        if (!patternReconciliation.get(p1).containsKey(p2)) {
            PatternReconciliation pr = new PatternReconciliation(p1, p2, o.getRender(), r);
            patternReconciliation.get(p1).put(p2, pr);
            reconciliations.add(pr);
        }
        return patternReconciliation.get(p1).get(p2);
    }

    public Set<PatternGrammar> getSubsumedGrammars(Pattern p) {
        Set<PatternGrammar> grammars = new HashSet<>();
        if (patternSubsumedGrammarsMap.containsKey(p)) {
            grammars.addAll(patternSubsumedGrammarsMap.get(p));
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

    public Impact getImpact(Pattern c) {
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
