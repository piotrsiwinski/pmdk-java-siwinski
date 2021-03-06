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
        // Zapisujemy dwa długie napisy
        // zwalniamy pierwszy
        // sprawdzamy, czy alokator zaalokuje wolne miejsce na poczatku sterty
        var longText = "THIS IS VERY LONG STRING";

        final var heap = new FileHeap(Paths.get(pathToHeap));

        int address = heap.putObject("text", longText);
        int longTextAddress = heap.putObject("longText", longText);

        heap.freeObject("text");

        int aAddress = heap.putObject("A", "A");
        int bAddress = heap.putObject("B", "B");
        int cAddress = heap.putObject("C", "C");

        assertEquals(address, aAddress);
        assertTrue(bAddress < longTextAddress);
        assertTrue(cAddress < longTextAddress);

        assertEquals("A", heap.getObject("A", String.class));
        assertEquals("B", heap.getObject("B", String.class));
        assertEquals("C", heap.getObject("C", String.class));
        assertEquals(longText, heap.getObject("longText", String.class));

    }
}
