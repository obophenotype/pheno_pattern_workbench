package monarch.ontology.phenoworkbench.util;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class IRIManager {

    private final String OBONS = "http://purl.obolibrary.org/obo/";
    private final Pattern p = Pattern.compile("[a-zA-Z]+[_]+[0-9]+");
    private final Map<String,Set<String>> pre2ns = new HashMap<>();
    private final Map<String,String> ns2pre = new HashMap<>();
    private int NAMESPACECOUNTER = 0;
    private boolean strict = false;

    public IRIManager(Map<String,String> ns2prefixMap) {
        ns2prefixMap.forEach((ns,pre)->addNamespaceToPrefix(pre,ns));
        new DefaultPrefixManager().getPrefixName2PrefixMap().forEach((pre,ns)->addNamespaceToPrefix(pre,ns));
    }

    private void addNamespaceToPrefix(String prefix, String namespace) {
        if(!prefix.endsWith(":")) {
            prefix = prefix + ":";
        }

        ns2pre.put(namespace,prefix);
        if(!pre2ns.containsKey(prefix)) {
            pre2ns.put(prefix,new HashSet<>());
        }
        pre2ns.get(prefix).add(namespace);
    }

    private String getPrefix(IRI iri) {
        String ns = getNamespace(iri);
        if(!ns2pre.containsKey(ns)) {
            if(isOBOesque(iri)) {

                    String obo = ns.substring(0,ns.lastIndexOf("/")+1);
                    //System.out.println(obo);
                    String init = ns.replaceAll(obo,"");
                    //System.out.println(init);
                    String prefix = init.replaceAll("_","") + ":";
                    if(pre2ns.containsKey(prefix)) {
                        System.err.println(prefix+" prefix for uri "+ns+" was already present for differnt uris: "+pre2ns.get(prefix));
                        prefix = "ns" + NAMESPACECOUNTER + ":";
                        NAMESPACECOUNTER++;

                    }
                addNamespaceToPrefix(prefix,ns);

            } else {
                String prefix = "ns" + NAMESPACECOUNTER + ":";
                NAMESPACECOUNTER++;
                addNamespaceToPrefix(prefix,ns);
            }
        }
        return ns2pre.get(ns);
    }

    public String getCurie(OWLEntity e) {
        String shortform = getShortForm(e.getIRI());
        if(isOBOesque(e.getIRI())) {
            return shortform.replaceAll("_",":");
        } else {
            String prefix = getPrefix(e.getIRI());
            return prefix + shortform;
        }
    }


    public String getSafeLabel(OWLEntity e, OWLOntology o) {
        String label = getLabel(e,o);
        String sl = label.replaceAll("[^A-Za-z0-9]","_"); //replace non-alphanumeric
        sl = sl.replaceAll("^_+", "").replaceAll("_+$", ""); //replace leading or trailing underscores
        return sl;
    }

    public String getQualifiedSafeLabel(OWLEntity e, OWLOntology o) {
        return getSafeLabel(e,o)+"_"+getPrefix(e.getIRI()).replaceAll(":","");
    }

    public String getShortForm(IRI e) {
       return e.getShortForm();
    }


    public String getLabel(OWLEntity e, OWLOntology o) {
        for(String l:OntologyUtils.getLabelsRDFS(e,o)) {
            return l;
        }
        if(strict) {
            throw new RuntimeException("No label for entity "+e.getIRI()+", which is not allowed in 'strict' mode!");
        }
        String shortform = getShortForm(e.getIRI());
        if(shortform==null||shortform.isEmpty()) {
            return e.getIRI().toString().replaceAll("[^A-Za-z0-9]","");
        } else {
            return shortform;
        }
    }

    public String getNamespace(IRI e) {
        if(isOBOesque(e)) {
            String remain = getShortForm(e);
            String id = remain.substring(remain.indexOf("_") + 1);
            return e.toString().replaceAll(id+"$","");
        } else {
            return e.toString().replaceAll(getShortForm(e), "");
        }
    }


    private boolean isOBOesque(IRI e) {
        String s = e.toString();
        if(s.startsWith(OBONS)) {
            String remain = s.replaceAll(OBONS,"");
            return p.matcher(remain).matches();
        }
        return false;
    }

    public void setStrict(boolean strict) {
        this.strict = strict;
    }


}
