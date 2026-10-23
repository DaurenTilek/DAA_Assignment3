import algorithms.DagShortestPath;
import algorithms.Kahn;
import graph.Graph;
import metrics.Metrics;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class DagShortestPathTest {
    private long[] bellmanFord(Graph graph) {
        long[] distance = new long[graph.n];
        Arrays.fill(distance, Long.MAX_VALUE);

        int[] degree = Kahn.indegrees(graph);

        for (int v = 0; v < graph.n; v++) {
            if (degree[v] == 0) {
                distance[v] = 0;
            }
        }

        for (int pass = 0; pass < graph.n - 1; pass++) {
            for (Graph.Edge edge : graph.edges) {
                if (distance[edge.u()] != Long.MAX_VALUE) {
                    distance[edge.v()] = Math.min(
                            distance[edge.v()],
                            distance[edge.u()] + edge.w()
                    );
                }
            }
        }

        return distance;
    }

    private void checkPaths(
            Graph graph,
            DagShortestPath.Result result
    ) {
        int[] degree = Kahn.indegrees(graph);

        for (int vertex = 0; vertex < graph.n; vertex++) {
            List<Integer> path = result.pathTo(vertex);

            assertFalse(path.isEmpty());
            assertTrue(path.size() <= graph.n);

            assertEquals(
                    vertex,
                    path.get(path.size() - 1).intValue()
            );

            assertEquals(0, degree[path.get(0)]);

            long cost = 0;

            for (int i = 1; i < path.size(); i++) {
                int from = path.get(i - 1);
                int to = path.get(i);

                boolean found = false;

                for (Graph.Edge edge : graph.adj.get(from)) {
                    if (edge.v() == to &&
                            result.distance()[from] + edge.w() ==
                                    result.distance()[to]) {

                        cost += edge.w();
                        found = true;
                        break;
                    }
                }

                assertTrue(found);
            }

            assertEquals(result.distance()[vertex], cost);
        }
    }

    @Test
    void randomDagsMatchBellmanFord() {
        Random random = new Random(42);

        for (int test = 0; test < 100; test++) {
            int n = 2 + random.nextInt(35);
            Graph graph = new Graph(n, true);

            List<Integer> permutation = new ArrayList<>();

            for (int i = 0; i < n; i++) {
                permutation.add(i);
            }

            Collections.shuffle(permutation, random);

            for (int a = 0; a < n; a++) {
                for (int b = a + 1; b < n; b++) {
                    if (random.nextInt(4) == 0) {
                        graph.add(
                                permutation.get(a),
                                permutation.get(b),
                                random.nextInt(31) - 15
                        );
                    }
                }
            }

            Metrics metrics = new Metrics();

            DagShortestPath.Result result =
                    DagShortestPath.run(graph, metrics);

            assertArrayEquals(
                    bellmanFord(graph),
                    result.distance()
            );

            assertEquals(
                    (long) graph.edges.size(),
                    metrics.edgeScans
            );

            checkPaths(graph, result);
        }
    }

    @Test
    void negativeWeightsAndMultipleSources() {
        Graph graph = new Graph(6, true);

        graph.add(0, 2, 3);
        graph.add(1, 2, -4);
        graph.add(2, 3, 2);
        graph.add(1, 4, 7);
        graph.add(3, 4, -1);
        graph.add(0, 2, 5);

        DagShortestPath.Result result =
                DagShortestPath.run(graph, new Metrics());

        assertArrayEquals(
                new long[]{0, 0, -4, -2, -3, 0},
                result.distance()
        );

        assertEquals(List.of(1, 2, 3, 4), result.pathTo(4));
        assertEquals(List.of(5), result.pathTo(5));
        assertEquals(-9L, result.sum());

        checkPaths(graph, result);
    }

    @Test
    void emptyAndEdgelessGraphs() {
        Graph empty = new Graph(0, true);

        DagShortestPath.Result emptyResult =
                DagShortestPath.run(empty, new Metrics());

        assertEquals(0, emptyResult.distance().length);
        assertEquals(0L, emptyResult.sum());

        Graph isolated = new Graph(3, true);

        DagShortestPath.Result result =
                DagShortestPath.run(isolated, new Metrics());

        assertArrayEquals(
                new long[]{0, 0, 0},
                result.distance()
        );

        checkPaths(isolated, result);
    }

    @Test
    void rejectsCyclesAndUndirectedGraphs() {
        Graph cycle = new Graph(2, true);

        cycle.add(0, 1, 1);
        cycle.add(1, 0, -2);

        assertThrows(
                IllegalArgumentException.class,
                () -> DagShortestPath.run(cycle, new Metrics())
        );

        Graph undirected = new Graph(2, false);
        undirected.add(0, 1, 1);

        assertThrows(
                IllegalArgumentException.class,
                () -> DagShortestPath.run(
                        undirected, new Metrics()
                )
        );
    }
}