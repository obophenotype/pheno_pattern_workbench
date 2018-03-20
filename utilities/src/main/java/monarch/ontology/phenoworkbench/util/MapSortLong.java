package monarch.ontology.phenoworkbench.util;

import java.util.Comparator;
import java.util.HashMap;

public class MapSortLong implements Comparator<Object> {

    HashMap<Object, Long> map = new HashMap<>();

    protected MapSortLong(HashMap<? extends Object, Long> map){
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

