package monarch.ontology.phenoworkbench.browser.analytics;

import monarch.ontology.phenoworkbench.browser.util.OntologyFileExtension;
import monarch.ontology.phenoworkbench.browser.util.OntologyUtils;
import monarch.ontology.phenoworkbench.browser.util.RenderManager;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;

import java.io.File;
import java.util.*;

public class UberOntology {



    private final Map<String, Set<OWLAxiom>> allAxiomsAcrossOntologies = new HashMap<>();
    private final Map<OWLAxiom, Set<String>> allOntologiesAcrossAxioms = new HashMap<>();

    private final Map<String, Set<OWLEntity>> allSignaturesAcrossOntologies = new HashMap<>();
    private final Map<OWLEntity, Set<String>> allOntologiesAcrossSignature = new HashMap<>();


    private final Imports imports;
    Map<String, String> map_oid_name = new HashMap<>();
    private final File pd;
    int o_ct = 1;

    private final RenderManager render = new RenderManager();

    public UberOntology(Imports imports, File pd) {
        this.imports = imports;
        this.pd = pd;
        processOntologies();
    }

    private void processOntology(Imports imports, File ofile) {
        String name = ofile.getName();
        String oid = "o" + o_ct;
        o_ct++;
        map_oid_name.put(oid, name);

        try {
            OWLOntology o = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(ofile);
            Set<OWLAxiom> axioms = new HashSet<>(o.getAxioms(imports));
            Set<OWLEntity> signature = new HashSet<>(o.getSignature(imports));
            allAxiomsAcrossOntologies.put(oid, axioms);
            allSignaturesAcrossOntologies.put(oid, signature);
            for (OWLAxiom ax : axioms) {
                if (!allOntologiesAcrossAxioms.containsKey(ax)) {
                    allOntologiesAcrossAxioms.put(ax, new HashSet<>());
                }
                allOntologiesAcrossAxioms.get(ax).add(oid);
            }
            for (OWLEntity ax : signature) {
                if (!allOntologiesAcrossSignature.containsKey(ax)) {
                    allOntologiesAcrossSignature.put(ax, new HashSet<>());
                }
                allOntologiesAcrossSignature.get(ax).add(oid);
            }
            getRender().addLabel(o);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Set<OWLAxiom> getAllAxioms() {
        return allOntologiesAcrossAxioms.keySet();
    }

    public Collection<? extends String> getAllOntologiesAcrossAxioms(OWLAxiom ax) {
        return allOntologiesAcrossAxioms.get(ax);
    }

    public Map<String,String> getMap_oid_name() {
        return map_oid_name;
    }

    public Map<String, Set<OWLEntity>> getAllSignaturesAcrossOntologies() {
        return allSignaturesAcrossOntologies;
    }

    public Map<String,Set<OWLAxiom>> getAllAxiomsAcrossOntologies() {
        return allAxiomsAcrossOntologies;
    }

    private void processOntologies() {
        for (File ofile : pd.listFiles(new OntologyFileExtension())) {
            OntologyUtils.p(ofile.getName());
            processOntology(imports, ofile);
        }
    }

    public OWLOntology createNewUberOntology() {
        try {
            OWLOntology o = OWLManager.createOWLOntologyManager().createOntology(IRI.create(PatternIRIs.EBIBASE.replaceAll("#","")+"UnionOntology"));
            allAxiomsAcrossOntologies.keySet().forEach(oid -> o.getOWLOntologyManager().addAxioms(o, allAxiomsAcrossOntologies.get(oid)));
            return o;
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Set<String> getOids() {
        return allAxiomsAcrossOntologies.keySet();
    }

    public Set<OWLEntity> getSignature(String oid) {
        Set<OWLEntity> e = new HashSet<>();
        if(allSignaturesAcrossOntologies.containsKey(oid)) {
           e.addAll(allSignaturesAcrossOntologies.get(oid));
        }
        return e;
    }

    public RenderManager getRender() {
        return render;
    }
}
