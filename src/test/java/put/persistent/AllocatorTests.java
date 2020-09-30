package put.persistent;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AllocatorTests {

    final static String pathToHeap = "allocHeap.pool";

    @AfterAll
    public static void cleanup() {
        File f = new File(pathToHeap);
        if (f.exists()) {
            f.delete();
        }
    }

    @Test
    public void shouldAddEmployee() {
        var object = "THIS IS VERY LONG STRING";

        final var heap = new FileHeap(Paths.get(pathToHeap));

        int address = heap.allocate("text", object.getBytes());
        int secondAddress = heap.allocate("text2", object.getBytes());

        heap.freeObject("text");

        int newAddress = heap.allocate("text", object.getBytes());

        assertEquals(address, newAddress);
    }

    // todo: napisać test dla obiektu wiekszego od bucketa
}
