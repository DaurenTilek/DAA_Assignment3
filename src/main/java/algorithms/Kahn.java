package algorithms;

import graph.Graph;
import metrics.Metrics;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Kahn {
    public record Result(List<Integer> order, boolean cycle) {}

    public static int[] indegrees(Graph graph) {
        int[] degree = new int[graph.n];

        for (Graph.Edge edge : graph.edges) {
            degree[edge.v()]++;
        }

        return degree;
    }

    public static Result run(Graph graph, Metrics metrics) {
        if (!graph.directed) {
            throw new IllegalArgumentException(
                    "Directed graph required"
            );
        }

        int[] degree = indegrees(graph);

        ArrayDeque<Integer> queue = new ArrayDeque<>();
        List<Integer> order = new ArrayList<>();

        for (int vertex = 0; vertex < graph.n; vertex++) {
            if (degree[vertex] == 0) {
                queue.add(vertex);
            }
        }

        while (!queue.isEmpty()) {
            int vertex = queue.remove();
            order.add(vertex);

            for (Graph.Edge edge : graph.adj.get(vertex)) {
                metrics.edgeScans++;

                degree[edge.v()]--;

                if (degree[edge.v()] == 0) {
                    queue.add(edge.v());
                }
            }
        }

        boolean cycle = order.size() != graph.n;

        return new Result(order, cycle);
    }

    public static boolean valid(Graph graph, Result result) {
        if (result.cycle() || result.order().size() != graph.n) {
            return false;
        }

        int[] position = new int[graph.n];
        Arrays.fill(position, -1);

        for (int i = 0; i < graph.n; i++) {
            int vertex = result.order().get(i);

            if (vertex < 0 ||
                    vertex >= graph.n ||
                    position[vertex] != -1) {
                return false;
            }

            position[vertex] = i;
        }

        for (Graph.Edge edge : graph.edges) {
            if (position[edge.u()] >= position[edge.v()]) {
                return false;
            }
        }

        return true;
    }
}