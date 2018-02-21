package monarch.ontology.phenoworkbench.browser.analytics;

public class Timer {
    long start = System.currentTimeMillis();
    public String getTimeElapsed() {
        long current = System.currentTimeMillis();
        long duration = current - start;
        start = current;
        return " (T: " + (duration / 1000) + " sec)";
    }
}
