package put.persistent;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import put.example.list.SimplePersistentList;

import java.io.File;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SimpleListTest1 {


    final static String pathToHeap = "listHeap.pool";
    final static String listName = "my-numbers";

    @AfterEach
    public void cleanup() {
        File f = new File(pathToHeap);
        if (f.exists()) {
            f.delete();
        }
    }

    @Test
    public void shouldAddToEmptyList() {
        var heap = new FileHeap(Paths.get(pathToHeap));
        var lst = new SimplePersistentList(listName, heap);
        lst.add(1);

        int e1 = lst.get(0);

        assertEquals(1, e1);
    }

    @Test
    public void shouldGetElementFromList() {
        var heap = new FileHeap(Paths.get(pathToHeap));
        var lst = new SimplePersistentList(listName, heap);
        lst.add(1);

        var anotherHeap = new FileHeap(Paths.get(pathToHeap));
        var anotherLst = new SimplePersistentList(listName, anotherHeap);

        int e1 = anotherLst.get(0);
        assertEquals(1, e1);
    }

    @Test
    public void shouldAddAnotherElements() {
        var heap = new FileHeap(Paths.get(pathToHeap));
        var lst = new SimplePersistentList(listName, heap);

        lst.add(1);
        lst.add(2);
        lst.add(3);
        lst.add(4);


        var anotherHeap = new FileHeap(Paths.get(pathToHeap));
        var anotherLst = new SimplePersistentList(listName, anotherHeap);
        int e1 = anotherLst.get(0);
        int e2 = anotherLst.get(1);
        int e3 = anotherLst.get(2);
        int e4 = anotherLst.get(3);

        assertEquals(1, e1);
        assertEquals(2, e2);
        assertEquals(3, e3);
        assertEquals(4, e4);
    }
}
