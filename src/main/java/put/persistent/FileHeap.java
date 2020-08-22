package put.persistent;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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

    private int heapPointer = heapAddress;
    private MappedByteBuffer byteBuffer;
    private Map<String, ObjectData> objectDirectory;
    private boolean isOpened = false;


    public FileHeap(Path path) {
        this.path = path;
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.objectDirectory = new HashMap<>();
        this.open();
    }

    @Override
    public int putObject(String name, Object object) {
        Transaction.run(this, () -> {
            try {
                log.info("Putting object with name: {} and value: {} ", name, object);
                if (objectDirectory.containsKey(name)) {
                    log.info("Object already in object directory... Performing update");
                    freeObject(name);
                }
                byte[] bytes = mapper.writeValueAsBytes(object);
                heapPointer = allocate(name, bytes);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Cannot put object into heap");
            }
        });
        return heapPointer;
    }

    // zapis heap pointer - na razie serializacja całego objectDirectory
    // pewnie dałoby się zrobić stałe offsety i tylko bytebufferem zapisywać - do sprawdzenia
    public int allocate(byte[] bytes) {
        byteBuffer.position(heapPointer);
        byteBuffer.put(bytes);
        heapPointer = byteBuffer.position();
        updateObjectDirectory(); // tylko tu serializacja
        return heapPointer;
    }

    public int allocate(String name, byte[] bytes) {
        putToObjectDirectory(name, bytes.length);
        return allocate(bytes);
    }

    private void putToObjectDirectory(String name, int size) {
        objectDirectory.put(name, new ObjectData(heapPointer, size));
    }

    // poprawić na wersję bez serializacji, tylko stałe offsety
    private void updateObjectDirectory() {
        try {
            putToObjectDirectory(heapPointerName, Integer.SIZE);
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
        return Optional.ofNullable(objectDirectory.get(name))
                .map(objectData -> {
                    byte[] bytes = new byte[objectData.objectSize];
                    byteBuffer.position(objectData.objectAddress);
                    byteBuffer.get(bytes);
                    try {
                        return mapper.readValue(bytes, aClass);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .orElse(null);
    }

    @Override
    public void freeObject(String name) {
        if (objectDirectory.containsKey(name)) {
            ObjectData objectData = objectDirectory.get(name);
            Transaction.run(this, () -> {
                byte[] bytes = new byte[objectData.objectSize];
                byteBuffer.position(objectData.objectAddress);
                byteBuffer.put(bytes); // put zeroes to buffer (leaves empty space)
                byteBuffer.force();
                objectDirectory.remove(name);
                updateObjectDirectory();
            });
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
        Transaction.run(this::updateObjectDirectory);
    }

    static class ObjectData {
        private int objectAddress;
        private int objectSize;

        private ObjectData() { // required for Jackson
        }

        private ObjectData(int objectAddress, int objectSize) {
            this.objectAddress = objectAddress;
            this.objectSize = objectSize;
        }

    }
}
