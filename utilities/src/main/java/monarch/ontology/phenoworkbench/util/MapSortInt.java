package monarch.ontology.phenoworkbench.util;

import java.util.Comparator;
import java.util.HashMap;

public class MapSortInt implements Comparator<Object> {

    HashMap<Object, Integer> map = new HashMap<>();

    protected MapSortInt(HashMap<? extends Object, Integer> map){
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

