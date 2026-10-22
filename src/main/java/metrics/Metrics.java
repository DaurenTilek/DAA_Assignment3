package metrics;

public class Metrics {

    public long findCalls;
    public long successfulUnions;
    public long edgeScans;
    public long comparisons;

    public int maxDepth;

    public long dsuOps() {
        return findCalls + successfulUnions;
    }

    public void depth(int size) {
        maxDepth = Math.max(maxDepth, size);
    }
}