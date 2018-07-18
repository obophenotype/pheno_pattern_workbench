package monarch.ontology.phenoworkbench.util;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.parameters.Imports;

import java.io.File;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public abstract class PhenoAnalysisRunner {

    private final Collection<OntologyEntry> corpus;
    private Imports imports = Imports.INCLUDED;
    private UberOntology o;

    public PhenoAnalysisRunner(Collection<OntologyEntry> corpus) {
        this.corpus = corpus;
    }

    protected void log(Object o) {
        System.out.println(o);
    }

    protected void log(String s, String process) {
        log(s+Timer.getSecondsElapsed(process));
    }

    protected void err(Object o) {
        System.err.println(o);
    }

    public abstract void runAnalysis();

    protected void prepareUberOntology() {
        Timer.start("PhenoAnalysisRunner::prepareUberOntology()");
        o = UberOntology.instance();
        o.processOntologies(getCorpus(), getImports());
        Timer.end("PhenoAnalysisRunner::prepareUberOntology()");
    }

    protected Optional<OWLOntology> createUnionOntology() {
        return getO().createNewUberOntology();
    }

    public Collection<OntologyEntry> getCorpus() {
        return corpus;
    }

    public Imports getImports() {
        return imports;
    }

    public void setImports(Imports imports) {
        this.imports = imports;
    }

    public UberOntology getO() {
        if(o==null) {
            prepareUberOntology();
        }
        return o;
    }

    protected RenderManager getRenderManager() {
        return getO().getRender();
    }

}
