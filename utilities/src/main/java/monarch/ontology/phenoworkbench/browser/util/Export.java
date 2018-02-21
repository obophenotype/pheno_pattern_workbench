package monarch.ontology.phenoworkbench.browser.util;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

public class Export {

    Pattern p = Pattern.compile("");

    public static void writeCSV(List<Map<String,String>> data, File out) {
        Set<String> header = new HashSet<>();
        data.forEach(rec->rec.keySet().forEach(k->header.add(k)));
        List<String> columns = new ArrayList<>(header);
        Collections.shuffle(columns);
        StringBuilder sb = new StringBuilder();
        columns.forEach(c->sb.append(c+","));
        sb.append("\n");
        for(Map<String,String> rec:data) {
            for (String col : columns) {
                if (rec.containsKey(col)) {
                    String value = rec.get(col);
                    if(value.contains(",")||value.contains("\n")||value.contains("\r")) {
                        sb.append("\""+value+"\"");
                    } else {
                        sb.append(value);
                    }

                }
                sb.append(",");
            }
            sb.append("\n");
        }
        try {
            FileUtils.write(out, sb.toString(),"utf-8");
            OntologyUtils.p("...done exporting to csv");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
