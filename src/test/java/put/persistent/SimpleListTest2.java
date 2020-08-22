package put.persistent;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import put.example.list.SimplePersistentList;

import java.io.File;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SimpleListTest2 {


    final static String pathToHeap = "listHeap2.pool";
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
    public void shouldAddManyElementsToList() {
        var heap = new FileHeap(Paths.get(pathToHeap));
        var lst = new SimplePersistentList(listName, heap);
        lst.add(1);
        lst.add(2);
        lst.add(3);
        lst.add(4);
        lst.add(5);

        int e1 = lst.get(0);
        int e2 = lst.get(1);
        int e3 = lst.get(2);
        int e4 = lst.get(3);
        int e5 = lst.get(4);

        assertEquals(1, e1);
        assertEquals(2, e2);
        assertEquals(3, e3);
        assertEquals(4, e4);
        assertEquals(5, e5);
    }

    @Test
    public void shouldCreateManyListsAtHeap() {
        var heap = new FileHeap(Paths.get(pathToHeap));
        var lst1 = new SimplePersistentList("lst1", heap);
        var lst2 = new SimplePersistentList("lst2", heap);
        var lst3 = new SimplePersistentList("lst3", heap);


        lst1.add(1); lst1.add(2); lst1.add(3);
        lst2.add(4); lst2.add(5); lst2.add(6);
        lst3.add(7); lst3.add(8); lst3.add(9);

        int e1 = lst1.get(0);
        int e2 = lst1.get(1);
        int e3 = lst1.get(2);

        int e4 = lst2.get(0);
        int e5 = lst2.get(1);
        int e6 = lst2.get(2);

        int e7 = lst3.get(0);
        int e8 = lst3.get(1);
        int e9 = lst3.get(2);

        assertEquals(1, e1);
        assertEquals(2, e2);
        assertEquals(3, e3);
        assertEquals(4, e4);
        assertEquals(5, e5);
        assertEquals(6, e6);
        assertEquals(7, e7);
        assertEquals(8, e8);
        assertEquals(9, e9);


    }
}
