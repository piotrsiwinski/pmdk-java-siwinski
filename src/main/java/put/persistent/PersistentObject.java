package put.persistent;

import org.bson.types.ObjectId;

import java.nio.ByteBuffer;

public interface PersistentObject<T> {

    byte[] toBytes();

    // T fromBytes(byte[] arr);

}
