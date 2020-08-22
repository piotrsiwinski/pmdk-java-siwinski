package put.persistent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ListTest {


    final static String pathToHeap = "listHeap.pool";
    final static String listName = "my-numbers";

    @AfterAll
    public static void cleanup() {
        File f = new File(pathToHeap);
        if (f.exists()) {
            // f.delete();
        }
    }

    @Test
    @Order(1)
    public void shouldAddToEmptyList() {
        var heap = new FileHeap(Paths.get(pathToHeap));
        var lst = new PersistentList(listName, heap);
        lst.add(1);

        int e1 = lst.get(0);

        assertEquals(1, e1);
    }

    @Test
    @Order(2)
    public void shouldGetElementFromList() {
        var heap = new FileHeap(Paths.get(pathToHeap));
        var lst = new PersistentList(listName, heap);

        int e1 = lst.get(0);

        assertEquals(1, e1);
    }

    @Test
    @Order(3)
    public void shouldAddAnotherElements() {
        var heap = new FileHeap(Paths.get(pathToHeap));
        var lst = new PersistentList(listName, heap);

        lst.add(2);
        lst.add(3);
        lst.add(4);

        int e1 = lst.get(0);
        int e2 = lst.get(1);
        int e3 = lst.get(2);
        int e4 = lst.get(3);

        assertEquals(1, e1);
        assertEquals(2, e2);
        assertEquals(3, e3);
        assertEquals(4, e4);
    }

    @Test
    @Order(3)
    public void shouldGetAllElements() {
        var heap = new FileHeap(Paths.get(pathToHeap));
        var lst = new PersistentList(listName, heap);

        int e1 = lst.get(0);
        // wywala sie, bo musi doczytac z pmem kolejny element, a nie ma w pamieci
        int e2 = lst.get(1);
        int e3 = lst.get(2);
        int e4 = lst.get(3);

        assertEquals(1, e1);
        assertEquals(2, e2);
        assertEquals(3, e3);
        assertEquals(4, e4);
    }


    private static class PersistentList {
        private static int nodeId = 1;
        private final Heap heap;
        private final ListMetadata listMetadata;
        private Node head;

        public PersistentList(String name, Heap heap) {
            this.heap = heap;

            ListMetadata metadata = heap.getObject(name, ListMetadata.class);
            if (metadata == null) {
                // first creation of list
                listMetadata = new ListMetadata(name, null);
                heap.putObject(name, listMetadata);
            } else {
                // list was already created
                listMetadata = metadata;
                if(metadata.head != null) { // but can be empty, so null is possible
                    head = listMetadata.head;
                }
            }
        }

        public void add(int element) {
            Transaction.run(() -> {
                if (head == null) {
                    head = new Node("node-" + nodeId++, element);
                    heap.putObject(head.id, head);
                    listMetadata.head = head;
                    heap.putObject(listMetadata.name, listMetadata);
                } else {
                    Node help = head;
                    while (help.next != null) {
                        help = help.next;
                    }
                    Node newNode = new Node("node-" + nodeId++, element);
                    help.next = newNode;
                    heap.putObject(newNode.id, newNode);
                    heap.putObject(help.id, help);
                }
            });

        }


        public int get(int index) {
            if (head == null) {
                throw new ArrayIndexOutOfBoundsException("List is empty");
            }
            Node tmp = head; // trzeba doczytac kolejne elementy z pamieci
            for (int i = 0; i < index; i++) {
                tmp = tmp.next;
                if(tmp == null){
                    throw new RuntimeException("Cannot get element...");
                }
            }
            return tmp.element;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        static class Node {
            private String id;
            private int element;
            private Node next;

            public Node(String id, int element) {
                this.element = element;
                this.id = id;
            }
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        static class ListMetadata {
            private String name;
            private Node head;
        }
    }
}
