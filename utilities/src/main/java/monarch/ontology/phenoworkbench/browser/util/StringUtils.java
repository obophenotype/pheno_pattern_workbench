package monarch.ontology.phenoworkbench.browser.util;

import java.util.List;

public class StringUtils {
    public static StringBuilder linesToStringBuilder(List<String> report) {
        StringBuilder sb = new StringBuilder();
        for (String line : report) {
            System.out.println(line);
            sb.append(line + "\n");
        }
        return sb;
    }
}
