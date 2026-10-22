import algorithms.DSU;
import algorithms.MST;
import graph.Graph;
import metrics.Metrics;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class MSTTest {

    @Test
    void hundredRandomConnectedGraphs() {
        Random random = new Random(42);

        for (int test = 0; test < 100; test++) {
            int n = 2 + random.nextInt(30);
            Graph graph = new Graph(n, false);

            for (int v = 1; v < n; v++) {
                graph.add(
                        random.nextInt(v),
                        v,
                        random.nextInt(41) - 20
                );
            }

            for (int i = 0; i < 4 * n; i++) {
                graph.add(
                        random.nextInt(n),
                        random.nextInt(n),
                        random.nextInt(41) - 20
                );
            }

            MST.Result kruskal =
                    MST.kruskal(graph, new Metrics());

            MST.Result prim =
                    MST.prim(graph, new Metrics());

            assertEquals(kruskal.weight(), prim.weight());

            for (MST.Result result : List.of(kruskal, prim)) {
                assertEquals(n - 1, result.edges().size());
                assertEquals(1, result.components());

                DSU check = new DSU(n, new Metrics());
                long weight = 0;

                for (Graph.Edge edge : result.edges()) {
                    assertTrue(check.union(edge.u(), edge.v()));
                    assertTrue(graph.edges.contains(edge) ||
                            graph.edges.contains(new Graph.Edge(
                                    edge.v(), edge.u(), edge.w()
                            )));

                    weight += edge.w();
                }

                assertEquals(1, check.components());
                assertEquals(result.weight(), weight);
            }
        }
    }

    @Test
    void disconnectedGraphReturnsForest() {
        Graph graph = new Graph(6, false);

        graph.add(0, 1, 4);
        graph.add(1, 2, 1);
        graph.add(0, 2, 8);
        graph.add(3, 4, -2);
        graph.add(3, 3, -100);

        for (MST.Result result : List.of(
                MST.kruskal(graph, new Metrics()),
                MST.prim(graph, new Metrics())
        )) {
            assertEquals(3L, result.weight());
            assertEquals(3, result.edges().size());
            assertEquals(3, result.components());
        }
    }

    @Test
    void parallelEdgesAndSelfLoop() {
        Graph graph = new Graph(3, false);

        graph.add(0, 0, -100);
        graph.add(0, 1, 9);
        graph.add(0, 1, -2);
        graph.add(1, 2, 3);
        graph.add(0, 2, 10);

        for (MST.Result result : List.of(
                MST.kruskal(graph, new Metrics()),
                MST.prim(graph, new Metrics())
        )) {
            assertEquals(1L, result.weight());
            assertEquals(2, result.edges().size());
            assertEquals(1, result.components());
        }
    }

    @Test
    void emptyGraphAndDirectedInput() {
        Graph empty = new Graph(0, false);

        for (MST.Result result : List.of(
                MST.kruskal(empty, new Metrics()),
                MST.prim(empty, new Metrics())
        )) {
            assertEquals(0L, result.weight());
            assertEquals(0, result.edges().size());
            assertEquals(0, result.components());
        }

        Graph directed = new Graph(2, true);
        directed.add(0, 1, 5);

        assertThrows(
                IllegalArgumentException.class,
                () -> MST.kruskal(directed, new Metrics())
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> MST.prim(directed, new Metrics())
        );
    }
}