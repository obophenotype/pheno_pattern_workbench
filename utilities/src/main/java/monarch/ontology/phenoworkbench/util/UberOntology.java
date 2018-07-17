package monarch.ontology.phenoworkbench.util;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.ChangeApplied;
import org.semanticweb.owlapi.model.parameters.Imports;

import java.util.*;

public class UberOntology {

    private final KB kb = KB.getInstance();
    private final RenderManager render = RenderManager.getInstance();

    private final Map<String, Set<OWLAxiom>> oid_axioms = new HashMap<>();
    private final Map<OWLAxiom, Set<String>> axioms_oid = new HashMap<>();

    private final Map<String, Set<OWLEntity>> oid_signature = new HashMap<>();
    private final Map<OWLEntity, Set<String>> signature_oid = new HashMap<>();
    private final Map<String, Reasoner> oid_reasoner = new HashMap<>();
    private final Map<String, BranchLoader> oid_branches = new HashMap<>();


    private final Map<String, String> oid_name = new HashMap<>();
    private final Set<OntologyEntry> ontologyEntries = new HashSet<>();

    private static UberOntology instance = null;

    private UberOntology() {
    }

    public static UberOntology instance() {
        if(instance==null) {
            instance = new UberOntology();
        }
        return instance;
    }


    public void reset() {
        instance = new UberOntology();
    }

    private void processOntology(Imports imports, OWLOntology o, OntologyEntry e) {
        String oid = e.getOid();
        if(oid_axioms.containsKey(oid)) {
            System.out.println(oid+" already loaded, aborting.");
            return;
        }
        oid_name.put(oid, e.getIri());

        try {
            Reasoner r = new Reasoner(o);
            BranchLoader branchLoader = new BranchLoader();
            branchLoader.loadBranches(e.getRoots(),o.getClassesInSignature(imports),false,r.getOWLReasoner());

            Set<OWLAxiom> axioms = new HashSet<>(o.getAxioms(imports));
            Set<OWLEntity> signature = new HashSet<>(o.getSignature(imports));
            oid_reasoner.put(oid, r);
            oid_branches.put(oid, branchLoader);
            oid_axioms.put(oid, axioms);
            oid_signature.put(oid, signature);
            for (OWLAxiom ax : axioms) {
                if (!axioms_oid.containsKey(ax)) {
                    axioms_oid.put(ax, new HashSet<>());
                }
                axioms_oid.get(ax).add(oid);
            }
            for (OWLEntity ax : signature) {
                if (!signature_oid.containsKey(ax)) {
                    signature_oid.put(ax, new HashSet<>());
                }
                signature_oid.get(ax).add(oid);
            }
            getRender().addLabel(o);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        ontologyEntries.add(e);
    }

    public Set<OWLAxiom> getAllAxioms() {
        return axioms_oid.keySet();
    }

    public Collection<? extends String> getAllOntologiesAcrossAxioms(OWLAxiom ax) {
        return axioms_oid.get(ax);
    }

    public Map<String,String> getOid_name() {
        return oid_name;
    }

    public void processOntologies(Set<OntologyEntry> iris, Imports imports) {
        iris.forEach(entry->processOntology(entry,imports));
    }

    public void processOntology(OntologyEntry e, Imports imports) {
        kb.getOntology(e.getIri()).ifPresent(o->processOntology(imports, o,e));
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
        oid_axioms.keySet().forEach(oid -> addAxioms(o, oid));
    }

    private ChangeApplied addAxioms(OWLOntology o, String oid) {
        return o.getOWLOntologyManager().addAxioms(o, oid_axioms.get(oid));
    }

    public Set<String> getOids() {
        return oid_axioms.keySet();
    }

    public Set<OWLEntity> getSignature(String oid) {
       // Set<OWLEntity> e = new HashSet<>();
        if(oid_signature.containsKey(oid)) {
           return oid_signature.get(oid);
        }
        return new HashSet<>();
    }

    public RenderManager getRender() {
        return render;
    }

    public Set<OWLAxiom> getAxioms(String oid) {
        if(oid_axioms.containsKey(oid)) {
            return oid_axioms.get(oid);
        }
        return new HashSet<>();
    }

    public Collection<? extends OntologyEntry> getOntologyEntries() {
        return ontologyEntries;
    }

    public Set<OWLClass> getBranches(String oid) {
        if(oid_branches.containsKey(oid)) {
            return oid_branches.get(oid).getAllClassesInBranches();
        }
        return new HashSet<>();
    }
}
