package put.persistent;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AllocatorTests {

    final static String pathToHeap = "allocHeap.pool";

    @BeforeEach
    public void cleanup() {
        File f = new File(pathToHeap);
        if (f.exists()) {
            f.delete();
        }
    }

    @AfterEach
    public void cleanupAfter() {
        File f = new File(pathToHeap);
        if (f.exists()) {
            f.delete();
        }
    }

    @Test
    public void shouldAllocateLongString() {
        var object = "THIS IS VERY LONG STRING";

        final var heap = new FileHeap(Paths.get(pathToHeap));

        int address = heap.allocate("text", object.getBytes());
        int secondAddress = heap.allocate("text2", object.getBytes());

        heap.freeObject("text");

        int newAddress = heap.allocate("text", object.getBytes());

        assertEquals(address, newAddress);
    }

    @Test
    public void shouldAllocateForSecondTime() {
        var str = "Lorem ipsum dolor sit amet";

        var heap = new FileHeap(Paths.get(pathToHeap));

        int address = heap.allocate("str", str.getBytes());
        int secondAddr = heap.allocate("str2", str.getBytes());
        heap.freeObject("str");

        heap = new FileHeap(Paths.get(pathToHeap));
        int expectedAddress = heap.allocate("str", str.getBytes());
        System.out.println("address: " + address);
        System.out.println("second address: " + secondAddr);

        assertEquals(address, expectedAddress);
    }

    @Test
    public void shouldAllocateLongString2() {
        var object = "THIS IS VERY LONG STRING";

        final var heap = new FileHeap(Paths.get(pathToHeap));

        int address = heap.allocate("text", object.getBytes());
        int secondAddress = heap.allocate("text2", object.getBytes());

        heap.freeObject("text");

        int newAddress = heap.allocate("A", "A".getBytes());
        int bAddress = heap.allocate("B", "B".getBytes());
        int cAddress = heap.allocate("C", "C".getBytes());

        assertEquals(address, newAddress);
        assertTrue(bAddress < secondAddress);
        assertTrue(cAddress < secondAddress);
    }
}
