package graph;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Graph {

    public record Edge(int u, int v, int w) {}

    public final int n;
    public final boolean directed;

    public final List<Edge> edges = new ArrayList<>();
    public final List<List<Edge>> adj = new ArrayList<>();

    public Graph(int n, boolean directed) {
        if (n < 0) {
            throw new IllegalArgumentException("Negative size");
        }

        this.n = n;
        this.directed = directed;

        for (int i = 0; i < n; i++) {
            adj.add(new ArrayList<>());
        }
    }

    public void add(int u, int v, int weight) {
        if (u < 0 || v < 0 || u >= n || v >= n) {
            throw new IllegalArgumentException("Invalid vertex");
        }

        Edge edge = new Edge(u, v, weight);

        edges.add(edge);
        adj.get(u).add(edge);

        if (!directed) {
            adj.get(v).add(new Edge(v, u, weight));
        }
    }

    public Graph reverse() {
        Graph reversed = new Graph(n, directed);

        for (Edge edge : edges) {
            reversed.add(edge.v(), edge.u(), edge.w());
        }

        return reversed;
    }

    public static Graph read(String file) throws IOException {
        try (Scanner scanner = new Scanner(Path.of(file))) {
            int n = scanner.nextInt();
            int m = scanner.nextInt();
            String type = scanner.next();

            if (!type.equals("directed") &&
                    !type.equals("undirected")) {
                throw new IllegalArgumentException("Unknown graph type");
            }

            Graph graph = new Graph(n, type.equals("directed"));

            for (int i = 0; i < m; i++) {
                int u = scanner.nextInt();
                int v = scanner.nextInt();
                int weight = scanner.nextInt();

                graph.add(u, v, weight);
            }

            if (scanner.hasNext()) {
                throw new IllegalArgumentException("Extra input");
            }

            return graph;
        }
    }
}