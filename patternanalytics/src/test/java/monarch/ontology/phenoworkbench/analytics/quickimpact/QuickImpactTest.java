package monarch.ontology.phenoworkbench.analytics.quickimpact;

import monarch.ontology.phenoworkbench.util.DefinedClass;
import monarch.ontology.phenoworkbench.util.OntologyClass;
import monarch.ontology.phenoworkbench.analytics.pattern.impact.ImpactMode;
import monarch.ontology.phenoworkbench.analytics.pattern.impact.OntologyClassImpact;
import monarch.ontology.phenoworkbench.util.OntologyEntry;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class QuickImpactTest {

    QuickImpact impact;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        Set<OntologyEntry> corpus = Collections.singleton(new OntologyEntry("test","https://raw.githubusercontent.com/matentzn/ontologies/master/smalltest.owl"));
        String patternsiri = "https://raw.githubusercontent.com/obophenotype/upheno/master/src/patterns_out/patterns_merged.owl";
        impact = new QuickImpact(corpus,patternsiri,ImpactMode.EXTERNAL);

    }

    @org.junit.jupiter.api.Test
    void getSubsumedGrammars() {
    }

    @org.junit.jupiter.api.Test
    void getAllPatterns() {
        assertEquals(impact.getAllDefinedClasses().size(),52);
    }

    @org.junit.jupiter.api.Test
    void extractPatternsAmongDefinedClasses() {
        assertEquals(impact.getPatternsAmongDefinedClasses().size(),46);
    }

    @org.junit.jupiter.api.Test
    void getTopPatterns() {
        assertEquals(impact.getTopPatterns().size(),34);
    }

    @org.junit.jupiter.api.Test
    void getImpact() {
        int totalimpact = 0;
        for(DefinedClass subC:impact.getAllDefinedClasses()) {
            Optional<OntologyClassImpact> imp = impact.getImpact(subC);
            assertTrue(imp.isPresent());
            totalimpact += imp.get().getDirectImpact();
        }
        assertEquals(totalimpact,16);
    }

    @org.junit.jupiter.api.Test
    void getSubsumptionExplanationRendered() {
        boolean tested = false;
        for (OntologyClass subC : impact.getAllDefinedClasses()) {
            if (subC.getOWLClass().getIRI().toString().equals("http://upheno.com#UPHENO_TEST_19")) {
                for (OntologyClass superC : impact.getAllDefinedClasses()) {
                    if (superC.getOWLClass().getIRI().toString().equals("http://upheno.com#UPHENO_TEST_43")) {
                        assertTrue(impact.getExplanationProvider().getSubsumptionExplanationRendered(subC, superC).isPresent());
                        assertFalse(impact.getExplanationProvider().getSubsumptionExplanationRendered(superC, subC).isPresent());
                        tested = true;
                    }
                }
            }
        }
        assertTrue(tested);
    }
}