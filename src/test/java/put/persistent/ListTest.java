package put.persistent;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ListTest {


    final static String pathToHeap = "listHeap.pool";

    @AfterAll
    public static void cleanup() {
        File f = new File(pathToHeap);
        if (f.exists()) {
            // f.delete();
        }

        var lst = new ArrayList<Integer>();

        lst.add(1);
        lst.get(0);
    }


    @Test
    @Order(1)
    public void shouldAddToEmptyList(){
        var heap = new FileHeap(Paths.get(pathToHeap));
        var lst = new PersistentList(heap);


        lst.add(1);
        lst.add(2);
        lst.add(3);
        lst.add(4);


        int first = lst.get(0);


        assertEquals(1, first);

    }


    private static class PersistentList {
        private static int nodeId = 1;
        private static final String headName = "head";

        private final Heap heap;
        private Node head;

        public PersistentList(Heap heap) {
            this.heap = heap;
        }

        public void add(int element) {
            Transaction.run(() -> {
                if (head == null) {
                    head = new Node("node-" + nodeId++, element);
                    heap.putObject(head.id, head);
                } else {
                    Node help = head;
                    while (help.next != null){
                        help = help.next;
                    }
                    Node newNode = new Node("node-" + nodeId++, element);
                    help.next = newNode;
                    heap.putObject(newNode.id, newNode);
                    heap.putObject(help.id, help);
                }
            });

        }


        public int get(int index){
            Node tmp = head;
            for(int i =0; i < index; i++){
                tmp = tmp.next;
            }
            return tmp.element;
        }

        @Getter
        @Setter
        @NoArgsConstructor
        static class Node {
            private String id;
            private int element;
            private Node next;

            public Node(String id, int element) {
                this.element = element;
                this.id = id;
            }
        }
    }
}
