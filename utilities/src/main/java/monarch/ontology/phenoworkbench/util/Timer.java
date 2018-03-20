package monarch.ontology.phenoworkbench.util;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class Timer {
    static Map<String,Long> processStart = new HashMap<>();
    static HashMap<String,Long> processDuration = new HashMap<>();

    public static String getSecondsElapsed(String process) {
        long current = System.currentTimeMillis();
        if(processStart.containsKey(process)) {
            long s = processStart.get(process);
            long duration = (current-s);
            return " (T: " + sec(duration) + " sec)";
        }
        else {
            start(process);
            return process+" not logged yet, starting now at 0 sec";
        }
    }

    private static double sec(long duration) {
        return (double)duration / 1000.0;
    }


    public static void start(String process) {
        processStart.put(process,System.currentTimeMillis());
    }

    public static void end(String process) {

        if(processStart.containsKey(process)) {
            long s = processStart.get(process);
            long e = System.currentTimeMillis();
            if(!processDuration.containsKey(process)) {
                processDuration.put(process,0l);
            }
            processDuration.put(process,(processDuration.get(process)+(e-s)));
        }

    }

    public static void printTimings() {
        TreeMap<Object, Long> sortedMap = OntologyUtils.sortMapByLongValue(processDuration);
        sortedMap.forEach((k,v)->System.out.println(k+": "+sec(v)));
    }
}
