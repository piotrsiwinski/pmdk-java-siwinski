package put.persistent;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class HeapCreationTest {

    final static String heapName = "testHeap.pool";

    @AfterAll
    public static void cleanup() {
        File f = new File(heapName);
        if (f.exists()) {
            f.delete();
        }
    }

    @Test
    public void shouldCreateNewHeap() {

        final var heapName = "testHeap.pool";

        final var heap = new FileHeap(Paths.get(heapName));

        File file = new File(heapName);
        assertTrue(file.exists());
    }
}
