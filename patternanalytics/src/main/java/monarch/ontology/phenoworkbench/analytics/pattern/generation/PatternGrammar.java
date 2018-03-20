package monarch.ontology.phenoworkbench.analytics.pattern.generation;

import java.util.Objects;

public class PatternGrammar {

    private final String original;
    private final String grammar;

    PatternGrammar(String s) {
        this.original = s;
        this.grammar = s.replaceAll("[^a-zA-Z0-9]","").toLowerCase();

    }

    public String getOriginal() {
        return original;
    }

    public String getGrammarSignature() {
        return grammar;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PatternGrammar)) return false;
        PatternGrammar that = (PatternGrammar) o;
        return Objects.equals(original, that.original) &&
                Objects.equals(grammar, that.grammar);
    }

    @Override
    public int hashCode() {

        return Objects.hash(original, grammar);
    }
}
