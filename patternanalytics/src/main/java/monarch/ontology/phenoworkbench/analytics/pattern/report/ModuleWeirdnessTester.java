package monarch.ontology.phenoworkbench.analytics.pattern.report;

import monarch.ontology.phenoworkbench.util.*;
import monarch.ontology.phenoworkbench.util.Timer;
import org.apache.commons.io.FileUtils;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.dlsyntax.renderer.DLSyntaxObjectRenderer;
import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.*;


public class ModuleWeirdnessTester {

    private RenderManager render = RenderManager.getInstance();


    private ModuleWeirdnessTester(Set<String> uris, String id, Map<String,String> ns2pre) {

    }

    public void runAnalysis() {
        OntologyUtils.p("Process Ontologies" + Timer.getSecondsElapsed("PatternExtractor::runAnalysis"));





    }

    public static void main(String[] args) throws IOException {

        String uri = args[0];
        File seed_p = new File(args[1]);
        File whynot_p = new File(args[2]);

        OWLDataFactory df = OWLManager.getOWLDataFactory();
        OWLObjectRenderer ren = new DLSyntaxObjectRenderer();

        Set<OWLEntity> seed_classes = getOwlClasses(seed_p, df);
        Set<OWLEntity> whynot_classes = getOwlClasses(whynot_p, df);

        try {
            OWLOntology o = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(new File(uri));
            SyntacticLocalityModuleExtractor extractor = new SyntacticLocalityModuleExtractor(o.getOWLOntologyManager(),o, ModuleType.BOT);
            OWLOntology mod = OWLManager.createOWLOntologyManager().createOntology(extractor.extract(seed_classes));

            Set<OWLEntity> sig = mod.getSignature(Imports.INCLUDED);

            for (OWLEntity e : whynot_classes) {
                System.out.println("$$$$$:"+e);
            for(OWLAxiom ax:o.getAxioms(Imports.INCLUDED)) {


                    if(ax.containsEntityInSignature(e)) {
                        for(OWLClassExpression ce:ax.getNestedClassExpressions()) {
                            if(ce.isClassExpressionLiteral()) {
                                continue;
                            }
                            Set<OWLEntity> sigce = ce.getSignature();
                            sigce.removeAll(sig);
                            if(!sigce.isEmpty()) {
                                System.out.println(ren.render(ce));
                                System.out.println("signature: "+sigce);
                            }
                        }
                    }
                }
            }
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }


    }

    private static Set<OWLEntity> getOwlClasses(File seed_p, OWLDataFactory df) throws IOException {
        Set<OWLEntity> seed_classes = new HashSet<>();
        List<String> seeds = FileUtils.readLines(seed_p,"utf-8");
        for(String s:seeds) {
            try{
                seed_classes.add(df.getOWLClass(IRI.create(new URL(s).toURI())));
            } catch(Throwable e) {
                e.printStackTrace();
            }
        }
        return seed_classes;
    }



}
