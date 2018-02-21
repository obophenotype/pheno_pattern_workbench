package monarch.ontology.phenoworkbench.browser.util;

import java.util.Comparator;
import java.util.HashMap;

public class OWLEntityMapSorter implements Comparator<Object> {

    HashMap<Object, Integer> map = new HashMap<>();

    protected OWLEntityMapSorter(HashMap<? extends Object, Integer> map){
        this.map.putAll(map);
    }

    @Override
    public int compare(Object s1, Object s2) {
        if(map.get(s1) >= map.get(s2)){
            return -1;
        }else{
            return 1;
        }
    }
}

