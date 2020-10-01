package put.persistent;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * |METADATA                     | xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
 * |Object directory             heapAddress - pokazuje na poczatek           | heapPointer - pokazuje na 1 wolne miejsce na stercie
 */
@Slf4j
public class FileHeap implements Heap {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final long TOTAL_BYTE_BUFFER_SIZE = 10L * 1024 * 1024; // Heap size: 10 MB
    private static final int metadataAddress = 0;
    private static final int metadataSize = 1 * 1024 * 1024; // 1 MB
    private static final int heapAddress = metadataSize;
    private static final String heapPointerName = "heapPointer";
    private final Path path;
    final static Lock transactionLock = new ReentrantLock();

    private int heapPointer = heapAddress;
    MappedByteBuffer byteBuffer;
    private Map<String, ObjectData> objectDirectory;
    private boolean isOpened = false;


    public FileHeap(Path path) {
        this.path = path;
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.objectDirectory = new HashMap<>();
        this.open();
    }

    static class TransactionLog {
        // http://www.mathcs.emory.edu/~cheung/Courses/554/Syllabus/6-logging/undo-redo2.html

        // redo log
        // We do not need to undo the updates made by the uncommitted transactions
        //And the recovery procedure for redo logging was simplified to:
        //
        // 1. Find all the committed transactions
        // 2. Redo all updates made by the committed transactions
    }

    @Override
    public int putObject(String name, Object object) {
//        Transaction.run(this, () -> {
            try {
                transactionLock.lock();
                // to mozna przekazac do Transaction.run() - nawet tylko heap pointer
                var tx = new TransactionInfo(new ObjectId(), heapPointer, TransactionInfo.TransactionState.None);
                // allocate(tx.getTxId().toString(), tx.toBytes());

                log.info("Putting object with name: {} and value: {} ", name, object);
                if (objectDirectory.containsKey(name)) {
                    log.info("Object already in object directory... Performing update");
                    freeObject(name);
                }
                byte[] bytes = mapper.writeValueAsBytes(object);
                return allocate(name, bytes);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Cannot put object into heap");
            } finally {
                transactionLock.unlock();
            }
//        });
    }

    // todo: zapis heap pointer - na razie serializacja całego objectDirectory
    // pewnie dałoby się zrobić stałe offsety i tylko bytebufferem zapisywać - do sprawdzenia

    public int allocate(String name, byte[] bytes) {
        int heapPointerTmp = heapPointer; // wskazuje na pierwsze wolne miejsce
        int blockSize = align(bytes.length);

        // sprawdzenie luki, jesli jest to wybierz juz zajete miejsce
        var reuse = false;
        var data = new TreeSet<>(objectDirectory.values());
        if (data.size() > 1) {
            Iterator<ObjectData> iterator = data.iterator();
            var data1 = iterator.next();
            while (iterator.hasNext()) {
                var data2 = iterator.next();
                if (data1.objectAddress + data1.objectSize + blockSize <= data2.objectAddress) {
                    heapPointer = data1.objectAddress + data1.objectSize;
                    reuse = true;
                }
                data1 = data2;
            }
        }

        objectDirectory.put(name, new ObjectData(heapPointer, blockSize, true));
        byteBuffer.position(heapPointer);
        byteBuffer.put(bytes);
        int allocatedAddress = heapPointer;
        if (reuse) {
            heapPointer = heapPointerTmp;
        } else {
            heapPointer += blockSize;
        }
        updateObjectDirectory(); // tylko tu serializacja
        return allocatedAddress;
    }

    // todo: poprawić na wersję bez serializacji, tylko stałe offsety
    private void updateObjectDirectory() {
        try {
            objectDirectory.put(heapPointerName, new ObjectData(heapPointer, Integer.SIZE, true));
            byte[] objectDir = mapper.writeValueAsBytes(objectDirectory);
            if (objectDir.length > metadataSize) {
                throw new RuntimeException("Metadata is too big!");
            }
            byteBuffer.position(metadataAddress);
            byteBuffer.put(objectDir);
            byteBuffer.force();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public <T> T getObject(String name, Class<T> aClass) {
        // todo: zapisywać jako stałe bloki, np char buffer[255], int size -> bezpośrednio w bytebuffer, bez serializacji
        return Optional.ofNullable(objectDirectory.get(name))
                .map(objectData -> {
                    byte[] bytes = new byte[objectData.objectSize];
                    byteBuffer.position(objectData.objectAddress);
                    byteBuffer.get(bytes);
                    try {
                        return mapper.readValue(bytes, aClass);
                    } catch (IOException e) {
                        e.printStackTrace();
                        throw new RuntimeException("Cannot read value: IOException");
                    }
                })
                .orElse(null);
    }

    public byte[] getObject(String name) {
        return Optional.ofNullable(objectDirectory.get(name))
                .map(objectData -> {
                    byte[] bytes = new byte[objectData.objectSize];
                    byteBuffer.position(objectData.objectAddress);
                    byteBuffer.get(bytes);
                    return bytes;
                })
                .orElse(null);
    }

    @Override
    public void freeObject(String name) {
        if (objectDirectory.containsKey(name)) {
            ObjectData objectData = objectDirectory.get(name);
//            Transaction.run(this, () -> {
            byte[] bytes = new byte[objectData.objectSize];
            byteBuffer.position(objectData.objectAddress);
            byteBuffer.put(bytes); // put zeroes to buffer (leaves empty space)
            byteBuffer.force();
            objectData.used = false;
            objectDirectory.remove(name);
            updateObjectDirectory();
//            });
        }
    }

    @Override
    public void open() {
        if (isOpened) return;
        boolean create = false;
        if (!path.toFile().exists()) {
            create = true;
        }

        try (RandomAccessFile fileInputStream = new RandomAccessFile(path.toFile(), "rw");
             FileChannel channel = fileInputStream.getChannel()) {
            byteBuffer = Optional
                    .ofNullable(channel.map(FileChannel.MapMode.READ_WRITE, 0, TOTAL_BYTE_BUFFER_SIZE))
                    .filter(ByteBuffer::isDirect)
                    .orElseThrow(() -> new RuntimeException("ByteBuffer is not direct"));
            if (!byteBuffer.isLoaded()) {
                byteBuffer.load();
            }
            if (create) {
                putObject("heapAddress", heapAddress);
                updateObjectDirectory();
            } else {
                byte[] arr = new byte[metadataSize];
                byteBuffer.get(arr);
                objectDirectory = mapper.readValue(arr, new TypeReference<HashMap<String, ObjectData>>() {
                });
                heapPointer = objectDirectory.get("heapPointer").objectAddress;
            }
            isOpened = true;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("AnyHeap::createHeap cannot create Heap");
        }
    }

    @Override
    public void close() {
//        Transaction.run(this, this::updateObjectDirectory);
        this.updateObjectDirectory();
    }

    public static class ObjectData implements Comparable<ObjectData> {
        int objectAddress;
        int objectSize;
        boolean used = false;

        private ObjectData() {
        }

        private ObjectData(int objectAddress, int objectSize, boolean used) {
            this.objectAddress = objectAddress;
            this.objectSize = objectSize;
            this.used = used;
        }

        @Override
        public int compareTo(ObjectData o) {
            return Integer.compare(objectAddress, o.objectAddress);
        }
    }

    int align(int n) {
        return (n + Long.BYTES - 1) & -Long.BYTES;
    }
}
