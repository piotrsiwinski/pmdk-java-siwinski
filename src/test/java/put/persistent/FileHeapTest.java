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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

@TestMethodOrder(OrderAnnotation.class)
class FileHeapTest {

    final static String pathToHeap = "employeeHeap.pool";

    @AfterAll
    public static void cleanup() {
        File f = new File(pathToHeap);
        if (f.exists()) {
            f.delete();
        }
    }

    @Test
    @Order(1)
    public void shouldAddEmployee() {
        var emp1 = new Employee(1, "John", "Doe", "john.doe@mail.com");
        var emp2 = new Employee(2, "John", "Doe", "john.doe@mail.com");
        var emp3 = new Employee(3, "John", "Doe", "john.doe@mail.com");

        final var heap = new FileHeap(Paths.get(pathToHeap));
        heap.putObject("emp1", emp1);
        heap.putObject("emp2", emp2);
        heap.putObject("emp3", emp3);

        var expectedEmp1 = heap.getObject("emp1", Employee.class);
        var expectedEmp2 = heap.getObject("emp2", Employee.class);
        var expectedEmp3 = heap.getObject("emp3", Employee.class);

        heap.close();

        assertEquals(emp1, expectedEmp1);
        assertEquals(emp2, expectedEmp2);
        assertEquals(emp3, expectedEmp3);
    }

    @Test
    @Order(2)
    public void shouldAppendNewEmployees() {
        // old employees
        var emp1 = new Employee(1, "John", "Doe", "john.doe@mail.com");
        var emp2 = new Employee(2, "John", "Doe", "john.doe@mail.com");
        var emp3 = new Employee(3, "John", "Doe", "john.doe@mail.com");

        // employees to append
        var emp4 = new Employee(4, "John", "Doe", "john.doe@mail.com");
        var emp5 = new Employee(5, "John", "Doe", "john.doe@mail.com");
        var emp6 = new Employee(6, "John", "Doe", "john.doe@mail.com");

        // put new employees
        final var heap = new FileHeap(Paths.get(pathToHeap));
        heap.putObject("emp4", emp4);
        heap.putObject("emp5", emp5);
        heap.putObject("emp6", emp6);


        // all employees should be stored at heap
        var expectedEmp1 = heap.getObject("emp1", Employee.class);
        var expectedEmp2 = heap.getObject("emp2", Employee.class);
        var expectedEmp3 = heap.getObject("emp3", Employee.class);
        var expectedEmp4 = heap.getObject("emp4", Employee.class);
        var expectedEmp5 = heap.getObject("emp5", Employee.class);
        var expectedEmp6 = heap.getObject("emp6", Employee.class);
        heap.close();

        assertEquals(emp1, expectedEmp1);
        assertEquals(emp2, expectedEmp2);
        assertEquals(emp3, expectedEmp3);
        assertEquals(emp4, expectedEmp4);
        assertEquals(emp5, expectedEmp5);
        assertEquals(emp6, expectedEmp6);
    }

    @Test
    @Order(3)
    public void shouldDeleteFirstObject() {
        // create employees without 1st employee
        var emp2 = new Employee(2, "John", "Doe", "john.doe@mail.com");
        var emp3 = new Employee(3, "John", "Doe", "john.doe@mail.com");
        var emp4 = new Employee(4, "John", "Doe", "john.doe@mail.com");
        var emp5 = new Employee(5, "John", "Doe", "john.doe@mail.com");
        var emp6 = new Employee(6, "John", "Doe", "john.doe@mail.com");

        final var heap = new FileHeap(Paths.get(pathToHeap));

        // free first employee
        heap.freeObject("emp1");

        // all employees should be stored at heap except 1st one
        var expectedEmp1 = heap.getObject("emp1", Employee.class);
        var expectedEmp2 = heap.getObject("emp2", Employee.class);
        var expectedEmp3 = heap.getObject("emp3", Employee.class);
        var expectedEmp4 = heap.getObject("emp4", Employee.class);
        var expectedEmp5 = heap.getObject("emp5", Employee.class);
        var expectedEmp6 = heap.getObject("emp6", Employee.class);
        heap.close();

        assertNull(expectedEmp1);
        assertEquals(emp2, expectedEmp2);
        assertEquals(emp3, expectedEmp3);
        assertEquals(emp4, expectedEmp4);
        assertEquals(emp5, expectedEmp5);
        assertEquals(emp6, expectedEmp6);
    }

    @Test
    @Order(3)
    public void shouldDeleteMiddleObject() {
        // create employees without 4rd employee (1st should be already deleted)
        var emp2 = new Employee(2, "John", "Doe", "john.doe@mail.com");
        var emp3 = new Employee(3, "John", "Doe", "john.doe@mail.com");
        var emp5 = new Employee(5, "John", "Doe", "john.doe@mail.com");
        var emp6 = new Employee(6, "John", "Doe", "john.doe@mail.com");

        final var heap = new FileHeap(Paths.get(pathToHeap));


        // free first employee
        heap.freeObject("emp4");

        // all employees should be stored at heap except 1st one
        var expectedEmp1 = heap.getObject("emp1", Employee.class);
        var expectedEmp2 = heap.getObject("emp2", Employee.class);
        var expectedEmp3 = heap.getObject("emp3", Employee.class);
        var expectedEmp4 = heap.getObject("emp4", Employee.class);
        var expectedEmp5 = heap.getObject("emp5", Employee.class);
        var expectedEmp6 = heap.getObject("emp6", Employee.class);

        heap.close();

        assertNull(expectedEmp1);
        assertEquals(emp2, expectedEmp2);
        assertEquals(emp3, expectedEmp3);
        assertNull(expectedEmp4);
        assertEquals(emp5, expectedEmp5);
        assertEquals(emp6, expectedEmp6);
    }

    @Test
    @Order(4)
    public void shouldDeleteAllObjects() {
        final var heap = new FileHeap(Paths.get(pathToHeap));

        // free rest of employees (1st and 4rd already deleted)
        heap.freeObject("emp2");
        heap.freeObject("emp3");
        heap.freeObject("emp5");
        heap.freeObject("emp6");

        // get employees
        var expectedEmp1 = heap.getObject("emp1", Employee.class);
        var expectedEmp2 = heap.getObject("emp2", Employee.class);
        var expectedEmp3 = heap.getObject("emp3", Employee.class);
        var expectedEmp4 = heap.getObject("emp4", Employee.class);
        var expectedEmp5 = heap.getObject("emp5", Employee.class);
        var expectedEmp6 = heap.getObject("emp6", Employee.class);

        heap.close();

        // all should be null
        assertNull(expectedEmp1);
        assertNull(expectedEmp2);
        assertNull(expectedEmp3);
        assertNull(expectedEmp4);
        assertNull(expectedEmp5);
        assertNull(expectedEmp6);
    }


    @Test
    @Order(5)
    public void shouldAddEmployeeToCleanedHeap() {
        // after previous test heap should be empty, let's try to add new objects to cleaned heap
        var emp1 = new Employee(1, "John", "Doe", "john.doe@mail.com");
        var emp2 = new Employee(2, "John", "Doe", "john.doe@mail.com");
        var emp3 = new Employee(3, "John", "Doe", "john.doe@mail.com");

        final var heap = new FileHeap(Paths.get(pathToHeap));
        heap.putObject("emp1", emp1);
        heap.putObject("emp2", emp2);
        heap.putObject("emp3", emp3);

        var expectedEmp1 = heap.getObject("emp1", Employee.class);
        var expectedEmp2 = heap.getObject("emp2", Employee.class);
        var expectedEmp3 = heap.getObject("emp3", Employee.class);
        heap.close();

        assertEquals(emp1, expectedEmp1);
        assertEquals(emp2, expectedEmp2);
        assertEquals(emp3, expectedEmp3);
    }

    @Test
    @Order(6)
    public void shouldDeleteAllObjects2() {
        final var heap = new FileHeap(Paths.get(pathToHeap));

        // free rest of employees (1st and 4rd already deleted)
        heap.freeObject("emp1");
        heap.freeObject("emp2");
        heap.freeObject("emp3");

        // get employees
        var expectedEmp1 = heap.getObject("emp1", Employee.class);
        var expectedEmp2 = heap.getObject("emp2", Employee.class);
        var expectedEmp3 = heap.getObject("emp3", Employee.class);
        heap.close();

        // all should be null
        assertNull(expectedEmp1);
        assertNull(expectedEmp2);
        assertNull(expectedEmp3);
    }

    @Test
    @Order(7)
    public void shouldReturnNullForNonExistingObject() {
        final var heap = new FileHeap(Paths.get(pathToHeap));
        var object = heap.getObject("non-existing-object", Object.class);
        heap.close();

        assertNull(object);
    }


    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    private static class Employee {
        private long id;
        private String name;
        private String surname;
        private String email;
    }


}