import algorithms.Kahn;
import graph.Graph;
import metrics.Metrics;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class KahnTest {
    @Test
    void randomDagsHaveValidOrder() {
        Random random = new Random(42);

        for (int test = 0; test < 100; test++) {
            int n = 2 + random.nextInt(50);
            Graph graph = new Graph(n, true);

            for (int u = 0; u < n; u++) {
                for (int v = u + 1; v < n; v++) {
                    if (random.nextInt(5) == 0) {
                        graph.add(u, v, 1);
                    }
                }
            }

            Metrics metrics = new Metrics();
            Kahn.Result result = Kahn.run(graph, metrics);

            assertFalse(result.cycle());
            assertTrue(Kahn.valid(graph, result));
            assertEquals(n, result.order().size());

            assertEquals(
                    (long) graph.edges.size(),
                    metrics.edgeScans
            );
        }
    }

    @Test
    void detectsCycleAndSelfLoop() {
        Graph cycle = new Graph(3, true);

        cycle.add(0, 1, 1);
        cycle.add(1, 2, 1);
        cycle.add(2, 0, 1);

        Kahn.Result result = Kahn.run(cycle, new Metrics());

        assertTrue(result.cycle());
        assertFalse(Kahn.valid(cycle, result));

        Graph loop = new Graph(1, true);
        loop.add(0, 0, 1);

        assertTrue(Kahn.run(loop, new Metrics()).cycle());
    }

    @Test
    void handlesIsolatedVerticesAndParallelEdges() {
        Graph graph = new Graph(5, true);

        graph.add(0, 2, 1);
        graph.add(0, 2, 3);
        graph.add(1, 2, 2);
        graph.add(2, 3, 1);

        Metrics metrics = new Metrics();
        Kahn.Result result = Kahn.run(graph, metrics);

        assertFalse(result.cycle());
        assertTrue(Kahn.valid(graph, result));
        assertEquals(5, result.order().size());
        assertEquals(4L, metrics.edgeScans);
    }

    @Test
    void validatorRejectsWrongOrders() {
        Graph graph = new Graph(3, true);

        graph.add(0, 1, 1);
        graph.add(1, 2, 1);

        assertFalse(Kahn.valid(
                graph,
                new Kahn.Result(List.of(2, 1, 0), false)
        ));

        assertFalse(Kahn.valid(
                graph,
                new Kahn.Result(List.of(0, 0, 2), false)
        ));

        assertFalse(Kahn.valid(
                graph,
                new Kahn.Result(List.of(0, 1), false)
        ));
    }

    @Test
    void emptyGraphAndUndirectedInput() {
        Graph empty = new Graph(0, true);

        Kahn.Result result = Kahn.run(empty, new Metrics());

        assertFalse(result.cycle());
        assertTrue(result.order().isEmpty());
        assertTrue(Kahn.valid(empty, result));

        Graph undirected = new Graph(2, false);
        undirected.add(0, 1, 1);

        assertThrows(
                IllegalArgumentException.class,
                () -> Kahn.run(undirected, new Metrics())
        );
    }
}