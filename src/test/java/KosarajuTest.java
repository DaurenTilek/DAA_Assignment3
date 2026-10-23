import algorithms.Kosaraju;
import graph.Graph;
import metrics.Metrics;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class KosarajuTest {
    private boolean[] reachable(Graph graph, int start) {
        boolean[] seen = new boolean[graph.n];
        ArrayDeque<Integer> queue = new ArrayDeque<>();

        seen[start] = true;
        queue.add(start);

        while (!queue.isEmpty()) {
            int vertex = queue.remove();

            for (Graph.Edge edge : graph.adj.get(vertex)) {
                if (!seen[edge.v()]) {
                    seen[edge.v()] = true;
                    queue.add(edge.v());
                }
            }
        }

        return seen;
    }

    @Test
    void randomGraphsMatchBfs() {
        Random random = new Random(42);

        for (int test = 0; test < 30; test++) {
            int n = 1 + random.nextInt(200);
            Graph graph = new Graph(n, true);

            for (int i = 0; i < 3 * n; i++) {
                graph.add(
                        random.nextInt(n),
                        random.nextInt(n),
                        1
                );
            }

            Metrics metrics = new Metrics();

            Kosaraju.Result result =
                    Kosaraju.run(graph, metrics);

            Graph reversed = graph.reverse();

            for (int vertex = 0; vertex < n; vertex++) {
                boolean[] forward = reachable(graph, vertex);
                boolean[] backward = reachable(reversed, vertex);

                for (int other = 0; other < n; other++) {
                    boolean expected =
                            forward[other] && backward[other];

                    boolean actual =
                            result.component()[vertex] ==
                                    result.component()[other];

                    assertEquals(expected, actual);
                }
            }

            assertEquals(
                    Arrays.stream(result.component())
                            .distinct()
                            .count(),
                    (long) result.count()
            );

            assertEquals(
                    2L * graph.edges.size(),
                    metrics.edgeScans
            );
        }
    }

    @Test
    void knownComponentsAndIsolatedVertex() {
        Graph graph = new Graph(6, true);

        graph.add(0, 1, 1);
        graph.add(1, 0, 1);

        graph.add(1, 2, 1);

        graph.add(2, 3, 1);
        graph.add(3, 2, 1);

        graph.add(3, 4, 1);
        graph.add(4, 4, 1);

        Metrics metrics = new Metrics();

        Kosaraju.Result result =
                Kosaraju.run(graph, metrics);

        int[] component = result.component();

        assertEquals(4, result.count());

        assertEquals(component[0], component[1]);
        assertEquals(component[2], component[3]);

        assertNotEquals(component[0], component[2]);
        assertNotEquals(component[2], component[4]);
        assertNotEquals(component[4], component[5]);
        assertNotEquals(component[0], component[5]);

        assertEquals(
                2L * graph.edges.size(),
                metrics.edgeScans
        );
    }

    @Test
    void pathOfHundredThousandVertices() {
        Graph graph = new Graph(100000, true);

        for (int i = 1; i < graph.n; i++) {
            graph.add(i - 1, i, 1);
        }

        Metrics metrics = new Metrics();

        Kosaraju.Result result =
                Kosaraju.run(graph, metrics);

        assertEquals(100000, result.count());
        assertEquals(100000, metrics.maxDepth);
        assertEquals(199998L, metrics.edgeScans);
    }

    @Test
    void emptyGraphAndUndirectedInput() {
        Graph empty = new Graph(0, true);

        Kosaraju.Result result =
                Kosaraju.run(empty, new Metrics());

        assertEquals(0, result.count());
        assertEquals(0, result.component().length);

        Graph undirected = new Graph(2, false);
        undirected.add(0, 1, 1);

        assertThrows(
                IllegalArgumentException.class,
                () -> Kosaraju.run(undirected, new Metrics())
        );
    }
}