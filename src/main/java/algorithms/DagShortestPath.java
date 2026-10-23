package algorithms;

import graph.Graph;
import metrics.Metrics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DagShortestPath {

    public record Result(long[] distance, int[] parent) {

        public long sum() {
            long sum = 0;

            for (long value : distance) {
                sum += value;
            }

            return sum;
        }

        public List<Integer> pathTo(int vertex) {
            List<Integer> path = new ArrayList<>();

            for (int current = vertex;
                 current != -1;
                 current = parent[current]) {

                path.add(current);
            }

            Collections.reverse(path);

            return path;
        }
    }

    public static Result run(
            Graph graph,
            Kahn.Result order,
            Metrics metrics
    ) {
        if (!graph.directed ||
                order.cycle() ||
                order.order().size() != graph.n) {

            throw new IllegalArgumentException(
                    "A DAG topological order is required"
            );
        }

        long[] distance = new long[graph.n];
        Arrays.fill(distance, Long.MAX_VALUE);

        int[] parent = new int[graph.n];
        Arrays.fill(parent, -1);

        int[] degree = Kahn.indegrees(graph);

        for (int vertex = 0; vertex < graph.n; vertex++) {
            if (degree[vertex] == 0) {
                distance[vertex] = 0;
            }
        }

        for (int vertex : order.order()) {
            for (Graph.Edge edge : graph.adj.get(vertex)) {
                metrics.edgeScans++;

                long candidate = distance[vertex] + edge.w();

                if (candidate < distance[edge.v()]) {
                    distance[edge.v()] = candidate;
                    parent[edge.v()] = vertex;
                }
            }
        }

        return new Result(distance, parent);
    }

    public static Result run(Graph graph, Metrics metrics) {
        Kahn.Result order = Kahn.run(graph, new Metrics());

        return run(graph, order, metrics);
    }
}