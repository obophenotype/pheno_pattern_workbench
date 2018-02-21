package monarch.ontology.phenoworkbench.util;

import org.semanticweb.owlapi.dlsyntax.renderer.DLSyntaxObjectRenderer;
import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.manchestersyntax.renderer.ManchesterOWLSyntaxOWLObjectRendererImpl;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import java.util.*;

public class RenderManager {

    private OWLObjectRenderer ren = new DLSyntaxObjectRenderer();
    private OWLObjectRenderer renManchester = new ManchesterOWLSyntaxOWLObjectRendererImpl();

    private Map<OWLEntity, String> labels = new HashMap<>();

    public void addLabel(OWLOntology o) {
        o.getSignature(Imports.INCLUDED).forEach(s -> OntologyUtils.getLabels(s, o).forEach(l -> labels.put(s, l)));
    }

    public String render(OWLObject ax) {
        String s = ren.render(ax);
        for (OWLEntity k : ax.getSignature()) {
            String l = getLabel(k);
            s = s.replaceAll(k.getIRI().getRemainder().or(""), l);
        }
        return s;
    }

    public String renderManchester(OWLObject ax) {
        String s = renManchester.render(ax);
        for (OWLEntity k : ax.getSignature()) {
            String l = getLabel(k);
            s = s.replaceAll(k.getIRI().getRemainder().or(""), l);
        }
        return s;
    }

    public String getLabel(OWLEntity k) {
        return labels.get(k) == null ? k.getIRI().getRemainder().or("") : labels.get(k);
    }

    public String renderForMarkdown(OWLObject ax) {
        return renderManchester(ax).replaceAll("(\n|\r\n|\r)", "  \n");
    }

    public void renderTreeForMarkdown(OWLClass c, OWLReasoner r, List<String> sb, int level, Set<OWLEntity> k, Map<OWLClass,OWLClassExpression> g, Set<OWLClass> u) {
        for (OWLClass sub : r.getSubClasses(c, true).getFlattened()) {
            String repeated = new String(new char[level]).replace("\0", "  ");
            sb.add(repeated+  " * " + renderTreeEntity(sub,k,g,u));
            renderTreeForMarkdown(sub, r, sb, level + 1,k,g,u);
        }
    }

    public void renderTreeForMarkdown(OWLClass c, OWLReasoner r, List<String> sb, int level) {
        for (OWLClass sub : r.getSubClasses(c, true).getFlattened()) {
            String repeated = new String(new char[level]).replace("\0", "  ");
            sb.add(repeated+  " * " + renderTreeEntity(sub,new HashSet<>(),new HashMap<>(),new HashSet<>()));
            renderTreeForMarkdown(sub, r, sb, level + 1,new HashSet<>(),new HashMap<>(),new HashSet<>());
        }
    }

    public String renderTreeEntity(OWLClass sub, Set<OWLEntity> keyentities, Map<OWLClass,OWLClassExpression> generated, Set<OWLClass> unsatisfiable) {
        String base = getLabel(sub);
        if (keyentities.contains(sub)) {
            base = "{" + base + "}";
        }
        if (generated.containsKey(sub)) {
            base = base+" ("+ render(generated.get(sub)) + ")";
        }
        if (unsatisfiable.contains(sub)) {
            base = "[[" + base + "]]";
        }
        return base;
    }
}
