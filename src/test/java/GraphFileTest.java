import algorithms.DagShortestPath;
import algorithms.Kahn;
import algorithms.Kosaraju;
import algorithms.MST;
import graph.Graph;
import metrics.Metrics;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GraphFileTest {

    @Test
    void disconnectedFile() throws Exception {
        Graph graph = Graph.read("data/disconnected.txt");

        assertEquals(6, graph.n);
        assertEquals(5, graph.edges.size());
        assertFalse(graph.directed);

        for (MST.Result result : List.of(
                MST.kruskal(graph, new Metrics()),
                MST.prim(graph, new Metrics())
        )) {
            assertEquals(3L, result.weight());
            assertEquals(3, result.components());
            assertEquals(3, result.edges().size());
        }
    }

    @Test
    void cyclicFile() throws Exception {
        Graph graph = Graph.read("data/cyclic.txt");

        assertEquals(5, graph.n);
        assertEquals(6, graph.edges.size());
        assertTrue(graph.directed);

        Kosaraju.Result scc =
                Kosaraju.run(graph, new Metrics());

        assertEquals(3, scc.count());

        assertEquals(
                scc.component()[0],
                scc.component()[1]
        );

        assertEquals(
                scc.component()[2],
                scc.component()[3]
        );

        assertNotEquals(
                scc.component()[0],
                scc.component()[2]
        );

        Kahn.Result order = Kahn.run(graph, new Metrics());

        assertTrue(order.cycle());
        assertFalse(Kahn.valid(graph, order));
    }

    @Test
    void dagFile() throws Exception {
        Graph graph = Graph.read("data/dag.txt");

        assertEquals(6, graph.n);
        assertEquals(6, graph.edges.size());
        assertTrue(graph.directed);

        Kahn.Result order = Kahn.run(graph, new Metrics());

        assertFalse(order.cycle());
        assertTrue(Kahn.valid(graph, order));

        DagShortestPath.Result result =
                DagShortestPath.run(
                        graph, order, new Metrics()
                );

        assertArrayEquals(
                new long[]{0, 0, -4, -2, -3, 0},
                result.distance()
        );

        assertEquals(
                List.of(1, 2, 3, 4),
                result.pathTo(4)
        );

        assertEquals(-9L, result.sum());
    }
}