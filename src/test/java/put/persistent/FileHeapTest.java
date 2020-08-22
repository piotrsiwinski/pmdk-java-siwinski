package put.persistent;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.File;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

@TestMethodOrder(OrderAnnotation.class)
class FileHeapTest {

    @AfterAll
    public static void cleanup(){
        File f = new File("employeeHeap.pool");
        if(f.exists()){
            f.delete();
        }
    }

    @Test
    @Order(1)
    public void shouldAddEmployee() {

        var emp1 = new Employee(1, "John", "Doe", "john.doe@mail.com");
        var emp2 = new Employee(2, "John", "Doe", "john.doe@mail.com");
        var emp3 = new Employee(3, "John", "Doe", "john.doe@mail.com");


        final var heap = new FileHeap(Paths.get("employeeHeap.pool"));
        heap.putObject("emp1", emp1);
        heap.putObject("emp2", emp2);
        heap.putObject("emp3", emp3);
        heap.close();

        var expectedEmp1 = heap.getObject("emp1", Employee.class);
        var expectedEmp2 = heap.getObject("emp2", Employee.class);
        var expectedEmp3 = heap.getObject("emp3", Employee.class);

        assertEquals(emp1, expectedEmp1);
        assertEquals(emp2, expectedEmp2);
        assertEquals(emp3, expectedEmp3);
    }

    @Test
    @Order(2)
    public void shouldAppendNewEmployees(){
        // old employees
        var emp1 = new Employee(1, "John", "Doe", "john.doe@mail.com");
        var emp2 = new Employee(2, "John", "Doe", "john.doe@mail.com");
        var emp3 = new Employee(3, "John", "Doe", "john.doe@mail.com");

        // employees to append
        var emp4 = new Employee(4, "John", "Doe", "john.doe@mail.com");
        var emp5 = new Employee(5, "John", "Doe", "john.doe@mail.com");
        var emp6 = new Employee(6, "John", "Doe", "john.doe@mail.com");

        // put new employees
        final var heap = new FileHeap(Paths.get("employeeHeap.pool"));
        heap.putObject("emp4", emp4);
        heap.putObject("emp5", emp5);
        heap.putObject("emp6", emp6);
        heap.close();

        // all employees should be stored at heap
        var expectedEmp1 = heap.getObject("emp1", Employee.class);
        var expectedEmp2 = heap.getObject("emp2", Employee.class);
        var expectedEmp3 = heap.getObject("emp3", Employee.class);
        var expectedEmp4 = heap.getObject("emp4", Employee.class);
        var expectedEmp5 = heap.getObject("emp5", Employee.class);
        var expectedEmp6 = heap.getObject("emp6", Employee.class);


        assertEquals(emp1, expectedEmp1);
        assertEquals(emp2, expectedEmp2);
        assertEquals(emp3, expectedEmp3);
        assertEquals(emp4, expectedEmp4);
        assertEquals(emp5, expectedEmp5);
        assertEquals(emp6, expectedEmp6);
    }


    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    static class Employee {
        private long id;
        private String name;
        private String surname;
        private String email;
    }


}