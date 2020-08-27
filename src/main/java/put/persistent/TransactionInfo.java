package put.persistent;

import lombok.Getter;
import org.bson.types.ObjectId;

import java.nio.ByteBuffer;

@Getter
public class TransactionInfo implements PersistentObject<TransactionInfo> {

    // OBJECT_ID - 12 B
    // HEAP_POINTER - 8
    // STATE - 8
    private static final int SIZE = Integer.SIZE * 2 + new ObjectId().toByteArray().length;
    private final ObjectId txId;
    private final int heapPointer; // adres, który transakcja chce modfikować
    private TransactionState state = TransactionState.None;

    public TransactionInfo(ObjectId txId, int heapPointer, TransactionState state) {
        this.txId = txId;
        this.heapPointer = heapPointer;
        this.state = state;
    }

    public byte[] toBytes() {
        var tmp = ByteBuffer.allocate(SIZE);
        txId.putToByteBuffer(tmp);
        tmp.putInt(heapPointer);
        tmp.putInt(state.ordinal());
        return tmp.array();
    }

    public static TransactionInfo fromBytes(byte[] arr) {
        var objectIdBytes = new byte[12];
        var buffer = ByteBuffer.wrap(arr);
        buffer.get(objectIdBytes);
        var hp = buffer.getInt();
        var state = TransactionState.values()[buffer.getInt()];
        return new TransactionInfo(new ObjectId(objectIdBytes), hp, state);
    }

    enum TransactionState {None, Initializing, Active, Committed, Aborted}

}