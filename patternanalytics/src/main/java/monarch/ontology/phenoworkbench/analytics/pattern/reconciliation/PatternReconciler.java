package monarch.ontology.phenoworkbench.analytics.pattern.reconciliation;

import monarch.ontology.phenoworkbench.analytics.pattern.generation.DefinedClass;
import monarch.ontology.phenoworkbench.analytics.pattern.generation.PatternGenerator;
import monarch.ontology.phenoworkbench.analytics.pattern.generation.PatternManager;
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

    private UberOntology o;
    private PatternManager patternManager;
    private Reasoner r;
    private Map<DefinedClass, Map<DefinedClass, PatternReconciliationCandidate>> patternReconciliation;
    private List<PatternReconciliationCandidate> reconciliations = new ArrayList<>();


    public PatternReconciler(Set<String> corpus, File mappings, boolean imports, boolean lazyalign, boolean bidirectionmapping, double confidencethreshold) {
        Timer.start("PatternReconciler::PatternReconciler()");
        try {
            System.out.println("QI: Loading Uber Ontology: " + Timer.getSecondsElapsed("PatternReconciler::PatternReconciler()"));
            Imports i = imports ? Imports.INCLUDED : Imports.EXCLUDED;
            o = new UberOntology(i, corpus);
            System.out.println("QI: Initialising pattern generator.." + Timer.getSecondsElapsed("PatternReconciler::PatternReconciler()"));
            PatternGenerator patternGenerator = new PatternGenerator(o.getRender());
            System.out.println("QI: Create new Uber Ontology.." + Timer.getSecondsElapsed("PatternReconciler::PatternReconciler()"));
            OWLOntology all = o.createNewUberOntology();
            System.out.println("Done... Axiomct: " + o.getAllAxioms().size() + Timer.getSecondsElapsed("PatternReconciler::PatternReconciler()"));
            System.out.println("QI: Preparing pattern reasoner" + Timer.getSecondsElapsed("PatternReconciler::PatternReconciler()"));
            r = new Reasoner(all);
            Set<DefinedClass> allDefinedClasses = new HashSet<>(patternGenerator.extractDefinedClasses(all.getAxioms(i), true));
            patternManager = new PatternManager(allDefinedClasses,r,patternGenerator,o.getRender());
            System.out.println("QI: Preparing patterns" + Timer.getSecondsElapsed("PatternReconciler::PatternReconciler()"));
            patternReconciliation = preparePatternMap(mappings, bidirectionmapping, lazyalign, confidencethreshold);
            //patternImpact = new DefinedClassImpactCalculator(o, r.getOWLReasoner(), r.getUnsatisfiableClasses(), new HashSet<>());
            System.out.println("QI: Computing impact.." + Timer.getSecondsElapsed("PatternReconciler::PatternReconciler()"));
            //patternImpactMap = patternImpact.precomputeImpactMap(allDefinedClasses);

            System.out.println("QI: Done.." + Timer.getSecondsElapsed("PatternReconciler::PatternReconciler()"));


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private Map<DefinedClass, Map<DefinedClass, PatternReconciliationCandidate>> preparePatternMap(File mappinFile, boolean bidirectionmapping, boolean lazyalign, double confidencethreshold) {
        Map<DefinedClass, Map<DefinedClass, PatternReconciliationCandidate>> patternReconciliation = new HashMap<>();
        List<IRIMapping> mapping = parseMappings(mappinFile, confidencethreshold);
        Map<IRI, DefinedClass> iriPatternMap = new HashMap<>();

        patternManager.getAllDefinedClasses().forEach(p -> iriPatternMap.put(p.getOWLClass().getIRI(), p));
        if (!lazyalign) {
            System.out.println("QI: Computing alignments.." + Timer.getSecondsElapsed("PatternReconciler::PatternReconciler()"));

            for (IRIMapping imap : mapping) {
                IRI iri = imap.getI1();
                IRI to = imap.getI2();
                indexReconciliationCandidate(patternReconciliation, iriPatternMap, imap,iri,to);
                if(bidirectionmapping) {
                    indexReconciliationCandidate(patternReconciliation, iriPatternMap, imap,to,iri);
                }

            }
        }
        return patternReconciliation;
    }

    private void indexReconciliationCandidate(Map<DefinedClass, Map<DefinedClass, PatternReconciliationCandidate>> patternReconciliation, Map<IRI, DefinedClass> iriPatternMap, IRIMapping imap, IRI iri, IRI to) {

        if (iriPatternMap.containsKey(iri) && iriPatternMap.containsKey(to)) {
            DefinedClass p = iriPatternMap.get(iri);
            DefinedClass p2 = iriPatternMap.get(to);
            if (!patternReconciliation.containsKey(p)) {
                patternReconciliation.put(p, new HashMap<>());
            }
            if (!patternReconciliation.get(p).containsKey(p2)) {
                PatternReconciliationCandidate pr = new PatternReconciliationCandidate(p, p2, o.getRender(), r);
                pr.setJaccardSimiliarity(imap.getJackard());
                pr.setSubclassSimilarity(imap.getSbcl());
                patternReconciliation.get(p).put(p2, pr);
                reconciliations.add(pr);
            }
        }
    }

    private List<IRIMapping> parseMappings(File mappinFile, double confidencethreshold) {

        List<IRIMapping> mappings = new ArrayList<>();
        if (mappinFile.isFile()) {
            try {
                List<String> lines = FileUtils.readLines(mappinFile, Charset.forName("utf-8"));
                for (String line : lines) {
                    String[] explosion = line.split("\t");
                    IRI iri1 = getIRI(explosion[0]);
                    //String logo1 = explosion[1];
                    IRI iri2 = getIRI(explosion[2]);
                    //String logo2 = explosion[3];
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

    public List<PatternReconciliationCandidate> getAllPatternReconciliations() {
        return reconciliations;
    }

}
