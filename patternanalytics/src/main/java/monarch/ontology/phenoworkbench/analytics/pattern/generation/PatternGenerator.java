package monarch.ontology.phenoworkbench.analytics.pattern.generation;

import monarch.ontology.phenoworkbench.analytics.pattern.Pattern;
import monarch.ontology.phenoworkbench.analytics.pattern.PatternGrammar;
import monarch.ontology.phenoworkbench.util.BaseIRIs;
import monarch.ontology.phenoworkbench.util.OntologyUtils;
import monarch.ontology.phenoworkbench.util.RenderManager;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.OWLObjectDuplicator;

import java.util.*;

public class PatternGenerator {
    private final OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();

    private final Map<OWLClass,Set<OWLClass>> superClasses = new HashMap<>();
    private final Map<OWLClassExpression,Map<OWLClass,Map<OWLClass,OWLClassExpression>>> cacheGeneratedExpressions = new HashMap<>();
    private final Map<OWLClassExpression,OWLClassExpression> mapDefinitionThingDefinition = new HashMap<>();
    private final Map<OWLClassExpression,PatternGrammar> mapThingDefinitionGrammar = new HashMap<>();
    private final RenderManager renderManager;


    public PatternGenerator(RenderManager renderManager) {
        this.renderManager = renderManager;
    }

    public Set<Pattern> extractPatterns(Set<OWLAxiom> axioms, boolean definition) {
        Set<OWLEquivalentClassesAxiom> allDefinitions = getDefinitionAxioms(axioms);
        OntologyUtils.p("extractPatterns(). Definitons: " + allDefinitions.size());
        Set<Pattern> generatedDefinitions = new HashSet<>();

        int all = allDefinitions.size();
        int i = 0;
        long timespent_all = 0;

        for (OWLEquivalentClassesAxiom ax : allDefinitions) {
            long start = System.currentTimeMillis();
            i++;
            if(i % 100 == 0) {
                OntologyUtils.p("Processing definition "+i+"/"+all);
            }
            Set<OWLClass> classes = new HashSet<>();
            Set<OWLClassExpression> ces = new HashSet<>();
            for (OWLClassExpression ce : ax.getClassExpressionsAsList()) {
                if (ce.isClassExpressionLiteral()) {
                    classes.add(ce.asOWLClass());
                } else if (!getPropertiesInSignature(ce).isEmpty()) {
                    ces.add(ce);
                }
            }
            for(OWLClass c: classes) {
                for(OWLClassExpression ce:ces) {
                    Pattern p = new Pattern(c, ce, definition);
                    generatedDefinitions.add(p);
                }
            }
            timespent_all +=(System.currentTimeMillis()-start);
        }

        OntologyUtils.p("Generated: " + generatedDefinitions.size());
        OntologyUtils.p("All: "+timespent_all/1000);
        return generatedDefinitions;
    }

    public Set<Pattern> generateHighLevelDefinitionPatterns(Set<OWLAxiom> axioms)  {
        Set<OWLEquivalentClassesAxiom> allDefinitions = getDefinitionAxioms(axioms);
        OntologyUtils.p("generateHighLevelDefinitionPatterns. Definitons: " + allDefinitions.size());
        List<Pattern> generatedDefinitions = new ArrayList<>();

        int all = allDefinitions.size();
        int i = 0;
        long timespent_all = 0;

        for (OWLEquivalentClassesAxiom ax : allDefinitions) {
            long start = System.currentTimeMillis();
            i++;
            if(i % 100 == 0) {
                OntologyUtils.p("Processing definition "+i+"/"+all);
            }
            for (OWLClassExpression ce : ax.getClassExpressionsAsList()) {
                if (!getPropertiesInSignature(ce).isEmpty()) {
                    generatedDefinitions.add(getThingPattern(ce));
                }
            }
            timespent_all +=(System.currentTimeMillis()-start);
        }

        OntologyUtils.p("Generated: " + generatedDefinitions.size());
        OntologyUtils.p("All: "+timespent_all/1000);
        return new HashSet<>(generatedDefinitions);
    }

    private Set<OWLEntity> getPropertiesInSignature(OWLClassExpression ce) {
        Set<OWLEntity> props = new HashSet<>();
        props.addAll(ce.getObjectPropertiesInSignature());
        props.addAll(ce.getDataPropertiesInSignature());
        return props;
    }

    private OWLClassExpression generateThingPattern(OWLClassExpression ce) {
        if(!mapDefinitionThingDefinition.containsKey(ce)) {
            OWLClassExpression ceout = replaceClassesInSignatureWith(ce,df.getOWLThing());
            mapDefinitionThingDefinition.put(ce, ceout);
        }
        return mapDefinitionThingDefinition.get(ce);
    }

    private OWLClassExpression replaceClassesInSignatureWith(OWLClassExpression ce, OWLClass replacement) {
        Map<IRI, IRI> replace = new HashMap<>();
        for (OWLClass c : ce.getClassesInSignature()) {
            replace.put(c.getIRI(), replacement.getIRI());
        }
        OWLObjectDuplicator replacer = new OWLObjectDuplicator(df, replace);
        return replacer.duplicateObject(ce);
    }

    private Pattern getThingPattern(OWLClassExpression ce) {
        OWLClassExpression ce_thing = generateThingPattern(ce);
        return createNewNonIndexedPattern(ce_thing,false);
    }

    private Pattern createNewNonIndexedPattern(OWLClassExpression ce, boolean definition) {
        OWLClass c = PatternClassNameGenerator.generateNamedClassForExpression(ce);
        return new Pattern(c,ce,definition);
    }

    public void setGrammar(Collection<Pattern> p) {
        p.forEach(this::setGrammar);
    }

    public void setGrammar(Pattern p) {
        p.setGrammar(generateGrammar(p.getDefiniton()));
    }

    private PatternGrammar generateGrammar(OWLClassExpression definition) {
        OWLClassExpression ce = replaceClassesInSignatureWith(definition,df.getOWLClass(IRI.create(BaseIRIs.EBIBASE+"X")));
        if(!mapThingDefinitionGrammar.containsKey(ce)) {
            String s=renderManager.renderForMarkdown(ce);
            mapThingDefinitionGrammar.put(ce, new PatternGrammar(s));
        }
        return mapThingDefinitionGrammar.get(ce);
    }


    public Set<Pattern> generateDefinitionPatterns(Set<OWLAxiom> axioms, OWLReasoner r, int SAMPLESIZE) {
        Set<OWLEquivalentClassesAxiom> allDefinitions = getDefinitionAxioms(axioms);
        OntologyUtils.p("generateDefinitionPatterns");
        Set<OWLClassExpression> generatedDefinitions = new HashSet<>();
        List<OWLClassExpression> existing_definitions = new ArrayList<>();

        OntologyUtils.p("generateDefinitionPatterns. Sampling: ");

        Set<OWLEquivalentClassesAxiom> defs_sample = new HashSet<>();
        long timespent_all = 0;

        createSample(allDefinitions, defs_sample,SAMPLESIZE);
        int i = 0;
        int all = defs_sample.size();

        for (OWLEquivalentClassesAxiom ax : defs_sample) {
            long start = System.currentTimeMillis();
            i++;
            if(i % 1000 == 0) {
                OntologyUtils.p("Pattern: "+i+"/"+all);
                OntologyUtils.p("All: "+timespent_all/1000);
            }
            for (OWLClassExpression ce : ax.getClassExpressionsAsList()) {

                if (!getPropertiesInSignature(ce).isEmpty()) {
                    existing_definitions.add(ce);
                    OWLClassExpression ceout = replaceUnsatisfiableClassesWithOWLThing(r, ce);
                    constructPatternsRecursively(ceout,r,generatedDefinitions);
                }
            }
            timespent_all +=(System.currentTimeMillis()-start);
        }

        Set<OWLClassExpression> exist = new HashSet<>(existing_definitions);
        OntologyUtils.p("Generated: " + generatedDefinitions.size());
        filterGeneratedDefinitions(generatedDefinitions,exist);

        OntologyUtils.p("Generated After Removing existing: " + generatedDefinitions.size());
        OntologyUtils.p("All: "+timespent_all/1000);
        Set<Pattern> patterns = new HashSet<>();
        for(OWLClassExpression ce:generatedDefinitions) {
            OWLClass c = PatternClassNameGenerator.generateNamedClassForExpression(ce);
            Pattern p = new Pattern(c,ce, false);
            patterns.add(p);
        }
        return patterns;
    }

    private void constructPatternsRecursively(OWLClassExpression ce, OWLReasoner r, Set<OWLClassExpression> patterns) {
        //OntologyUtils.p("##############");
        // OntologyUtils.p("Pattern: "+ patterns.size());
        patterns.add(ce);
        Set<OWLClassExpression> generated = generateAbstractions(ce, r);
        for(OWLClassExpression ceg:generated) {
            if(!patterns.contains(ceg)) {
                constructPatternsRecursively(ceg, r, patterns);
            }
        }
    }

    private Set<OWLClassExpression> generateAbstractions(OWLClassExpression ce, OWLReasoner r) {
        Set<OWLClassExpression> generated = new HashSet<>();
        Set<OWLClass> sig = ce.getClassesInSignature();
        for (OWLClass c : sig) {
            for (OWLClass superC : fetchSuperClasses(r, c)) {
                generated.add(replaceClassInClassExpression(ce, c, superC));
            }
        }
        return generated;
    }

    private void createSample(Set<OWLEquivalentClassesAxiom> allDefinitions, Set<OWLEquivalentClassesAxiom> defs_sample, int SAMPLESIZE) {
        if(allDefinitions.size()>SAMPLESIZE) {
            List<OWLEquivalentClassesAxiom> alldefs = new ArrayList<>(allDefinitions);
            Collections.shuffle(alldefs);
            defs_sample.addAll(alldefs.subList(0, SAMPLESIZE));
        } else {
            defs_sample.addAll(allDefinitions);
        }
    }

    private OWLClassExpression replaceUnsatisfiableClassesWithOWLThing(OWLReasoner r, OWLClassExpression ce) {
        Map<IRI, IRI> iriMap = new HashMap<>();
        for(OWLClass c:ce.getClassesInSignature()) {
            if(!r.isSatisfiable(c)) {
                iriMap.put(c.getIRI(), df.getOWLThing().getIRI());
            }
        }
        OWLObjectDuplicator replacer = new OWLObjectDuplicator(df, iriMap);
        return replacer.duplicateObject(ce);
    }



    private OWLClassExpression replaceClassInClassExpression(OWLClassExpression ce, OWLClass c, OWLClass superC) {
        if(cacheGeneratedExpressions.containsKey(ce)) {
            if(cacheGeneratedExpressions.get(ce).containsKey(c)) {
                if(cacheGeneratedExpressions.get(ce).get(c).containsKey(superC)) {
                    return cacheGeneratedExpressions.get(ce).get(c).get(superC);
                }
            } else {
                cacheGeneratedExpressions.get(ce).put(c,new HashMap<>());
            }
        } else {
            cacheGeneratedExpressions.put(ce,new HashMap<>());
            cacheGeneratedExpressions.get(ce).put(c,new HashMap<>());
        }
        Map<IRI, IRI> iriMap = new HashMap<>();
        iriMap.put(c.getIRI(), superC.getIRI());
        OWLObjectDuplicator replacer = new OWLObjectDuplicator(df, iriMap);
        OWLClassExpression ceout = replacer.duplicateObject(ce);
        cacheGeneratedExpressions.get(ce).get(c).put(superC,ceout);
        return ceout;
    }

    private void filterGeneratedDefinitions(Set<OWLClassExpression> generatedDefinitions, Set<OWLClassExpression> exising) {
        Set<OWLClassExpression> remove = new HashSet<>(exising);
        for(OWLClassExpression ce:generatedDefinitions) {
            if (containsDisjunctionWithOWLThing(ce)) {
                remove.add(ce);
            } /*else if(!containsDomainClasses(ce)){
                remove.add(ce);
            } */
        }
        generatedDefinitions.removeAll(remove);
    }

    /*
    private boolean containsDomainClasses(OWLClassExpression rewritten) {
        //return  !Collections.disjoint(rewritten.getClassesInSignature(), allClassesInBranches);
        return rewritten.getClassesInSignature().stream().anyMatch(branches.getAllClassesInBranches()::contains);
    }
    */

    private boolean containsDisjunctionWithOWLThing(OWLClassExpression rewritten) {
        for(OWLClassExpression ce:rewritten.getNestedClassExpressions()) {
            if(ce instanceof OWLObjectUnionOf) {
                if(ce.asDisjunctSet().contains(df.getOWLThing())) {
                    return true;
                }
            }
        }
        return false;
    }


    private Set<OWLClass> fetchSuperClasses(OWLReasoner r, OWLClass c) {
        if(superClasses.containsKey(c)) {
            return superClasses.get(c);
        } else {
            Set<OWLClass> superclasses = new HashSet<>();
            if(!c.equals(df.getOWLThing())) {
                superclasses.addAll(r.getSuperClasses(c, true).getFlattened());
                if(superclasses.isEmpty()) {
                    superclasses.add(df.getOWLThing());
                }
                superclasses.remove(c);
            }
            //superclasses.removeAll(allClassesInBranches);
            superClasses.put(c, superclasses);
            return superclasses;
        }
    }

    private Set<OWLEquivalentClassesAxiom> getDefinitionAxioms(Set<OWLAxiom> axioms) {
        Set<OWLEquivalentClassesAxiom> allDefinitions = new HashSet<>();
        for (OWLAxiom owlAxiom : axioms) {
            if (owlAxiom instanceof OWLEquivalentClassesAxiom) {
                allDefinitions.add((OWLEquivalentClassesAxiom) owlAxiom);
            }
        }
        return allDefinitions;
    }
}
