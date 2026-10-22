package algorithms;

import graph.Graph;
import graph.Graph.Edge;
import metrics.Metrics;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class MST {

    public record Result(
            long weight,
            List<Edge> edges,
            int components
    ) {}

    private static void check(Graph graph) {
        if (graph.directed) {
            throw new IllegalArgumentException(
                    "MST needs an undirected graph"
            );
        }
    }

    public static Result kruskal(Graph graph, Metrics metrics) {
        check(graph);

        List<Edge> sorted = new ArrayList<>(graph.edges);
        List<Edge> tree = new ArrayList<>();

        sorted.sort((a, b) -> {
            metrics.comparisons++;
            return Integer.compare(a.w(), b.w());
        });

        DSU dsu = new DSU(graph.n, metrics);
        long weight = 0;

        for (Edge edge : sorted) {
            if (dsu.components() <= 1) {
                break;
            }

            metrics.edgeScans++;

            if (dsu.find(edge.u()) != dsu.find(edge.v())) {
                dsu.union(edge.u(), edge.v());

                tree.add(edge);
                weight += edge.w();
            }
        }

        return new Result(weight, tree, dsu.components());
    }

    public static Result prim(Graph graph, Metrics metrics) {
        check(graph);

        boolean[] used = new boolean[graph.n];
        List<Edge> tree = new ArrayList<>();

        long weight = 0;
        int components = 0;

        PriorityQueue<Edge> queue = new PriorityQueue<>((a, b) -> {
            metrics.comparisons++;
            return Integer.compare(a.w(), b.w());
        });

        for (int start = 0; start < graph.n; start++) {
            if (used[start]) {
                continue;
            }

            components++;
            queue.add(new Edge(-1, start, 0));

            while (!queue.isEmpty()) {
                Edge edge = queue.remove();
                int vertex = edge.v();

                if (used[vertex]) {
                    continue;
                }

                used[vertex] = true;

                if (edge.u() != -1) {
                    tree.add(edge);
                    weight += edge.w();
                }

                for (Edge next : graph.adj.get(vertex)) {
                    metrics.edgeScans++;

                    if (!used[next.v()]) {
                        queue.add(next);
                    }
                }
            }
        }

        return new Result(weight, tree, components);
    }
}