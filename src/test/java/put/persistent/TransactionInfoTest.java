package put.persistent;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TransactionInfoTest {


    @Test
    public void shouldWriteToBytesAndRecreate(){
        // given tx Info
        var objId = new ObjectId();
        var hp = Integer.MAX_VALUE;
        var state = TransactionInfo.TransactionState.Committed;

        var txInfo = new TransactionInfo(objId, hp, state);

        // when converting to bytes
        byte[] bytes = txInfo.toBytes();

        // than object is recreated
        var expected = TransactionInfo.fromBytes(bytes);

        assertEquals(expected.getHeapPointer(), txInfo.getHeapPointer());
        assertEquals(expected.getState(), txInfo.getState());
        assertEquals(expected.getTxId(), txInfo.getTxId());

    }

    @Test
    public void shouldWriteToBytesAndRecreate2(){
        // given tx Info
        var objId = new ObjectId();
        var hp = Integer.MIN_VALUE;
        var state = TransactionInfo.TransactionState.None;

        var txInfo = new TransactionInfo(objId, hp, state);

        // when converting to bytes
        byte[] bytes = txInfo.toBytes();

        // than object is recreated
        var expected = TransactionInfo.fromBytes(bytes);

        assertEquals(expected.getHeapPointer(), txInfo.getHeapPointer());
        assertEquals(expected.getState(), txInfo.getState());
        assertEquals(expected.getTxId(), txInfo.getTxId());

    }

    @Test
    public void shouldWriteToBytesAndRecreate3(){
        // given tx Info
        var objId = new ObjectId();
        var hp = 0;
        var state = TransactionInfo.TransactionState.Aborted;

        var txInfo = new TransactionInfo(objId, hp, state);

        // when converting to bytes
        byte[] bytes = txInfo.toBytes();

        // than object is recreated
        var expected = TransactionInfo.fromBytes(bytes);

        assertEquals(expected.getHeapPointer(), txInfo.getHeapPointer());
        assertEquals(expected.getState(), txInfo.getState());
        assertEquals(expected.getTxId(), txInfo.getTxId());

    }
}