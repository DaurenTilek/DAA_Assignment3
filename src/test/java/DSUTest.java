import algorithms.DSU;
import metrics.Metrics;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class DSUTest {

    @Test
    void randomUnionsMatchReference() {
        int n = 50;

        Metrics metrics = new Metrics();
        DSU dsu = new DSU(n, metrics);

        int[] group = new int[n];

        for (int i = 0; i < n; i++) {
            group[i] = i;
        }

        Random random = new Random(42);

        for (int step = 0; step < 300; step++) {
            int a = random.nextInt(n);
            int b = random.nextInt(n);

            int oldGroup = group[b];
            int newGroup = group[a];

            boolean expectedUnion = oldGroup != newGroup;

            assertEquals(expectedUnion, dsu.union(a, b));

            for (int i = 0; i < n; i++) {
                if (group[i] == oldGroup) {
                    group[i] = newGroup;
                }
            }

            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    assertEquals(
                            group[i] == group[j],
                            dsu.find(i) == dsu.find(j)
                    );
                }
            }

            assertEquals(
                    Arrays.stream(group).distinct().count(),
                    (long) dsu.components()
            );
        }

        assertEquals(
                (long) n - dsu.components(),
                metrics.successfulUnions
        );
    }

    @Test
    void countersAndReset() {
        Metrics metrics = new Metrics();
        DSU dsu = new DSU(4, metrics);

        assertEquals(4, dsu.components());

        assertTrue(dsu.union(0, 1));
        assertFalse(dsu.union(0, 1));

        assertEquals(4L, metrics.findCalls);
        assertEquals(1L, metrics.successfulUnions);
        assertEquals(5L, metrics.dsuOps());
        assertEquals(3, dsu.components());

        dsu.makeSet(4);

        assertEquals(4, dsu.components());
        assertNotEquals(dsu.find(0), dsu.find(1));
    }
}