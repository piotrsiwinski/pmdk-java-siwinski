package put.example.employeeStore;

import put.persistent.FileHeap;
import put.persistent.Heap;

import java.nio.file.Paths;

class WriteEmployee {
  public static void main(String[] args) {
    final Heap heap = new FileHeap(Paths.get("employeeHeap.pool"));
    Employee emp = new Employee(1L, "John", "Doe", "john.doe@company.com");
    heap.putObject("emp", emp);
    heap.close();
  }
}
