package benchmark;

import graph.Graph;

import java.util.Random;

public class Generator {

    public static Graph create(int n, int m, String kind) {
        Random random = new Random(42);

        boolean undirected = kind.equals("undirected");
        boolean dag = kind.equals("dag");

        Graph graph = new Graph(n, !undirected);

        int[] rank = new int[n];

        for (int i = 0; i < n; i++) {
            rank[i] = i;
        }

        if (undirected || dag) {
            for (int i = n - 1; i > 0; i--) {
                int j = random.nextInt(i + 1);

                int temp = rank[i];
                rank[i] = rank[j];
                rank[j] = temp;
            }
        }

        if (undirected) {
            for (int i = 1; i < n; i++) {
                int a = rank[random.nextInt(i)];
                int b = rank[i];
                int weight = 1 + random.nextInt(1000);

                graph.add(a, b, weight);
            }
        }

        while (graph.edges.size() < m) {
            int a = random.nextInt(n);
            int b = random.nextInt(n);

            while (a == b) {
                b = random.nextInt(n);
            }

            if (dag && rank[a] > rank[b]) {
                int temp = a;
                a = b;
                b = temp;
            }

            int weight = 1 + random.nextInt(1000);

            graph.add(a, b, weight);
        }

        return graph;
    }
}