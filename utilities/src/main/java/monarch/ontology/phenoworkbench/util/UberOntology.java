package monarch.ontology.phenoworkbench.util;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.ChangeApplied;
import org.semanticweb.owlapi.model.parameters.Imports;

import java.util.*;

public class UberOntology {

    private final KB kb = KB.getInstance();

    private final Map<String, Set<OWLAxiom>> allAxiomsAcrossOntologies = new HashMap<>();
    private final Map<OWLAxiom, Set<String>> allOntologiesAcrossAxioms = new HashMap<>();

    private final Map<String, Set<OWLEntity>> allSignaturesAcrossOntologies = new HashMap<>();
    private final Map<OWLEntity, Set<String>> allOntologiesAcrossSignature = new HashMap<>();

    private final Imports imports;
    private final Map<String, String> map_oid_name = new HashMap<>();
    private final Set<OntologyEntry> ontologyEntries = new HashSet<>();

    private final RenderManager render = new RenderManager();

    public UberOntology(Imports imports, Set<OntologyEntry> iris) {
        this.imports = imports;
        processOntologies(iris);
    }

    private void processOntology(Imports imports, OWLOntology o, OntologyEntry e) {
        String oid = e.getOid();
        map_oid_name.put(oid, e.getIri());

        try {
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
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        ontologyEntries.add(e);
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

    private void processOntologies(Set<OntologyEntry> iris) {
        iris.forEach(iri->kb.getOntology(iri.getIri()).ifPresent(o->processOntology(imports, o,iri)));
    }

    public Optional<OWLOntology> createNewUberOntology() {
        Timer.start("UberOntology::createNewUberOntology");
        OWLOntology o = null;
        try {
            o = createEmptyUnionOntology();
            addAxiomsForOids(o);
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }
        Timer.end("UberOntology::createNewUberOntology");
        return Optional.ofNullable(o);
    }

    private OWLOntology createEmptyUnionOntology() throws OWLOntologyCreationException {
        return OWLManager.createOWLOntologyManager().createOntology(IRI.create(BaseIRIs.EBIBASE.replaceAll("#","")+"UnionOntology"));
    }

    private void addAxiomsForOids(OWLOntology o) {
        allAxiomsAcrossOntologies.keySet().forEach(oid -> addAxioms(o, oid));
    }

    private ChangeApplied addAxioms(OWLOntology o, String oid) {
        return o.getOWLOntologyManager().addAxioms(o, allAxiomsAcrossOntologies.get(oid));
    }

    public Set<String> getOids() {
        return allAxiomsAcrossOntologies.keySet();
    }

    public Set<OWLEntity> getSignature(String oid) {
       // Set<OWLEntity> e = new HashSet<>();
        if(allSignaturesAcrossOntologies.containsKey(oid)) {
           return allSignaturesAcrossOntologies.get(oid);
        }
        return new HashSet<>();
    }

    public RenderManager getRender() {
        return render;
    }

    public Set<OWLAxiom> getAxioms(String oid) {
        if(allAxiomsAcrossOntologies.containsKey(oid)) {
            return allAxiomsAcrossOntologies.get(oid);
        }
        return new HashSet<>();
    }

    public Collection<? extends OntologyEntry> getOntologyEntries() {
        return ontologyEntries;
    }
}
