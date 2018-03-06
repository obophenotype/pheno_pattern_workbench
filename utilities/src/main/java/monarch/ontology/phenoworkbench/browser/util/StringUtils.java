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
    /*
    https://stackoverflow.com/questions/537174/putting-char-into-a-java-string-for-each-n-characters
     */
    public static String insertPeriodically(
            String text, String insert, int period)
    {
        StringBuilder builder = new StringBuilder(
                text.length() + insert.length() * (text.length()/period)+1);

        int index = 0;
        String prefix = "";
        while (index < text.length())
        {
            // Don't put the insert in the very first iteration.
            // This is easier than appending it *after* each substring
            builder.append(prefix);
            prefix = insert;
            builder.append(text.substring(index,
                    Math.min(index + period, text.length())));
            index += period;
        }
        return builder.toString();
    }
}
