package benchmark;

import algorithms.*;
import graph.Graph;
import metrics.Metrics;

import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;

public class Benchmark {

    interface Work {
        long run(Metrics metrics);
    }

    static long measure(
            PrintWriter output,
            String algorithm,
            String graphName,
            Graph graph,
            Work work
    ) {
        double[] times = new double[5];

        Metrics first = null;
        long result = 0;

        for (int i = 0; i < 5; i++) {
            Metrics metrics = new Metrics();

            long start = System.nanoTime();
            long value = work.run(metrics);

            times[i] = (System.nanoTime() - start) / 1_000_000.0;

            if (i == 0) {
                first = metrics;
                result = value;
            } else {
                boolean different =
                        value != result ||
                                metrics.edgeScans != first.edgeScans ||
                                metrics.comparisons != first.comparisons ||
                                metrics.dsuOps() != first.dsuOps() ||
                                metrics.maxDepth != first.maxDepth;

                if (different) {
                    throw new AssertionError("Non-reproducible run");
                }
            }
        }

        Arrays.sort(times);

        output.printf(
                Locale.US,
                "%s,%s,%d,%d,%.4f,%d,%d,%d,%d,%d%n",
                algorithm,
                graphName,
                graph.n,
                graph.edges.size(),
                times[2],
                first.dsuOps(),
                first.edgeScans,
                first.comparisons,
                first.maxDepth,
                result
        );

        output.flush();

        return result;
    }

    public static void main(String[] args) throws Exception {
        Files.createDirectories(Path.of("results"));

        long[] expectedWeights = {
                147970, 30363,
                468911, 97589,
                1486139, 309550,
                4715515, 968517,
                15012094, 3056681
        };

        long[] expectedScc = {
                27, 1,
                137, 1,
                373, 1,
                1188, 1,
                3923, 1
        };

        long[] expectedSums = {
                396274, 252411,
                1271911, 723157,
                3941828, 2381342,
                12551891, 7376612,
                40096539, 23512087
        };

        int index = 0;
        int mismatches = 0;

        try (
                PrintWriter output =
                        new PrintWriter("results/results.csv");

                PrintWriter checks =
                        new PrintWriter("results/reference_check.txt")
        ) {
            output.println(
                    "algorithm,graph,n,m,time_ms,dsu_ops," +
                            "edge_scans,comparisons,max_depth,result"
            );

            int[] sizes = {1000, 3162, 10000, 31623, 100000};
            int[] densities = {4, 20};

            for (int n : sizes) {
                for (int density : densities) {
                    int m = n * density;

                    String name = density == 4 ? "sparse" : "dense";

                    Graph undirected =
                            Generator.create(n, m, "undirected");

                    long kruskal = measure(
                            output,
                            "Kruskal",
                            name,
                            undirected,
                            metrics -> MST.kruskal(
                                    undirected, metrics
                            ).weight()
                    );

                    long prim = measure(
                            output,
                            "Prim",
                            name,
                            undirected,
                            metrics -> MST.prim(
                                    undirected, metrics
                            ).weight()
                    );

                    if (kruskal != prim) {
                        throw new AssertionError("MST disagreement");
                    }

                    Graph directed =
                            Generator.create(n, m, "directed");

                    long components = measure(
                            output,
                            "Kosaraju",
                            name,
                            directed,
                            metrics -> {
                                long value = Kosaraju.run(
                                        directed, metrics
                                ).count();

                                if (metrics.edgeScans != 2L * m) {
                                    throw new AssertionError("SCC scans");
                                }

                                return value;
                            }
                    );

                    Graph dag = Generator.create(n, m, "dag");

                    Kahn.Result order =
                            Kahn.run(dag, new Metrics());

                    if (!Kahn.valid(dag, order)) {
                        throw new AssertionError("Invalid order");
                    }

                    measure(
                            output,
                            "Kahn",
                            name,
                            dag,
                            metrics -> {
                                Kahn.Result current =
                                        Kahn.run(dag, metrics);

                                if (!Kahn.valid(dag, current)) {
                                    throw new AssertionError(
                                            "Invalid Kahn order"
                                    );
                                }

                                if (metrics.edgeScans != m) {
                                    throw new AssertionError("Kahn scans");
                                }

                                return current.order().size();
                            }
                    );

                    long sum = measure(
                            output,
                            "DagShortestPath",
                            name,
                            dag,
                            metrics -> {
                                long value = DagShortestPath.run(
                                        dag, order, metrics
                                ).sum();

                                if (metrics.edgeScans != m) {
                                    throw new AssertionError("DAG scans");
                                }

                                return value;
                            }
                    );

                    String line = String.format(
                            "%d/%d: MST=%d (expected %d), " +
                                    "SCC=%d (expected %d), " +
                                    "DAG=%d (expected %d)",
                            n, m,
                            kruskal, expectedWeights[index],
                            components, expectedScc[index],
                            sum, expectedSums[index]
                    );

                    System.out.println(line);
                    checks.println(line);

                    if (kruskal != expectedWeights[index] ||
                            components != expectedScc[index] ||
                            sum != expectedSums[index]) {

                        mismatches++;
                    }

                    index++;
                }
            }

            checks.println("Reference mismatches: " + mismatches);
        }

        if (mismatches > 0) {
            throw new AssertionError(
                    "PDF reference mismatch. " +
                            "See results/reference_check.txt"
            );
        }
    }
}