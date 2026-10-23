package algorithms;

import graph.Graph;
import metrics.Metrics;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Kosaraju {

    public record Result(int[] component, int count) {}

    public static Result run(Graph graph, Metrics metrics) {
        if (!graph.directed) {
            throw new IllegalArgumentException(
                    "Directed graph required"
            );
        }

        boolean[] seen = new boolean[graph.n];
        int[] next = new int[graph.n];

        List<Integer> finish = new ArrayList<>();
        ArrayDeque<Integer> stack = new ArrayDeque<>();

        for (int root = 0; root < graph.n; root++) {
            if (seen[root]) {
                continue;
            }

            seen[root] = true;
            stack.push(root);
            metrics.depth(stack.size());

            while (!stack.isEmpty()) {
                int vertex = stack.peek();

                if (next[vertex] < graph.adj.get(vertex).size()) {
                    Graph.Edge edge = graph.adj.get(vertex)
                            .get(next[vertex]++);

                    metrics.edgeScans++;

                    int to = edge.v();

                    if (!seen[to]) {
                        seen[to] = true;

                        stack.push(to);
                        metrics.depth(stack.size());
                    }
                } else {
                    stack.pop();
                    finish.add(vertex);
                }
            }
        }

        Graph reversed = graph.reverse();

        int[] component = new int[graph.n];
        Arrays.fill(component, -1);

        int count = 0;

        for (int i = finish.size() - 1; i >= 0; i--) {
            int root = finish.get(i);

            if (component[root] != -1) {
                continue;
            }

            component[root] = count;
            stack.push(root);
            metrics.depth(stack.size());

            while (!stack.isEmpty()) {
                int vertex = stack.pop();

                for (Graph.Edge edge : reversed.adj.get(vertex)) {
                    metrics.edgeScans++;

                    int to = edge.v();

                    if (component[to] == -1) {
                        component[to] = count;

                        stack.push(to);
                        metrics.depth(stack.size());
                    }
                }
            }

            count++;
        }

        return new Result(component, count);
    }
}