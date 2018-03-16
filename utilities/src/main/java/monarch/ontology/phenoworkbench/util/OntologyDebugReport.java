package monarch.ontology.phenoworkbench.util;

import java.util.ArrayList;
import java.util.List;

public class OntologyDebugReport {

    private List<String> lines = new ArrayList<>();

    public void addLine(Object vio) {
        lines.add(vio.toString());
    }

    public void clear() {
        lines.clear();
    }

    public void addEmptyLine() {
        lines.add("");
    }

    public void addLines(List<? extends Object> lines) {
        lines.forEach(l->this.lines.add(l.toString()));
    }

    public List<String> getLines() {
        return lines;
    }
}
