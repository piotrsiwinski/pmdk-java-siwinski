package put.persistent;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TransactionTest1 {

    final static String pathToHeap = "transactionTest1.pool";

    @AfterEach
    public void cleanup() {
        File f = new File(pathToHeap);
        if (f.exists()) {
            // f.delete();
        }
    }

    // !! Zapisuje TransactionInfo bez serializacji obiektu - od razu zapis w bajtach
    @Test
    public void shouldSaveAndGetTransactionInfo() {
        var heap = new FileHeap(Paths.get(pathToHeap));
        var txInfo = new TransactionInfo(new ObjectId(), Integer.MAX_VALUE, TransactionInfo.TransactionState.Committed);

        heap.allocate(txInfo.getTxId().toString(), txInfo.toBytes());
        var object = heap.getObject(txInfo.getTxId().toString());
        var expected = TransactionInfo.fromBytes(object);


        assertEquals(expected.getHeapPointer(), txInfo.getHeapPointer());
        assertEquals(expected.getState(), txInfo.getState());
        assertEquals(expected.getTxId(), txInfo.getTxId());
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
