package put.persistent;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TransactionTest1 {

    final static String pathToHeap = "transactionTest1.pool";
    final static String listName = "my-numbers";

    @AfterEach
    public void cleanup() {
        File f = new File(pathToHeap);
        if (f.exists()) {
            // f.delete();
        }
    }

    @Test
    public void shouldAddToEmptyList() {
        var heap = new FileHeap(Paths.get(pathToHeap));
        Employee emp1 = new Employee(1, "John", "Doe", "john.doe@mail.com");
        heap.putObject("emp1", emp1);

        Employee expectedEmp1 = heap.getObject("emp1", Employee.class);


        assertEquals(emp1, expectedEmp1);
    }
}
