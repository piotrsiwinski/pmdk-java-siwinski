package put.example.list;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import put.persistent.Heap;
import put.persistent.Transaction;



public class SimplePersistentList {
    private final Heap heap;
    private Node head;
    private String listName;

    public SimplePersistentList(String name, Heap heap) {
        this.heap = heap;
        this.head = heap.getObject(name, Node.class);
        this.listName = name;
    }

    public void add(int element) {
        Transaction.run(heap, () -> {
            if (head == null) {
                head = new Node(element);
                heap.putObject(listName, head);
            } else {
                Node help = head;
                while (help.next != null) {
                    help = help.next;
                }
                help.next = new Node(element);
                heap.putObject(listName, head);
            }
        });
    }


    public int get(int index) {
        if (head == null) {
            throw new ArrayIndexOutOfBoundsException("List is empty");
        }
        Node tmp = head;
        for (int i = 0; i < index; i++) {
            if (tmp.next == null) {
                throw new RuntimeException("Cannot get element...");
            }
            tmp = tmp.next;

        }
        return tmp.element;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class Node {
        private int element;
        private Node next;

        public Node(int element) {
            this.element = element;
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