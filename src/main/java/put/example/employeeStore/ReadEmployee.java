package put.example.employeeStore;

import put.persistent.FileHeap;
import put.persistent.Heap;

import java.nio.file.Paths;

class ReadEmployee {
    public static void main(String[] args) {
        final Heap heap = new FileHeap(Paths.get("employeeHeap.pool"));
        Employee emp = heap.getObject("emp", Employee.class);

        System.out.println(emp);

        heap.close();
    }
}
