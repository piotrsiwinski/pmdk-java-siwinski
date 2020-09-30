package put.persistent;

import lombok.Getter;
import lombok.Setter;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;

public class MemoryBlock {
    private int size;
    @Getter
    @Setter
    private boolean used = false;
    @Getter
    private int address;
    private byte[] data;


    public MemoryBlock(int size, boolean used, int address) {
        this.size = size;
        this.used = used;
        this.address = address;
        this.data = new byte[size];
    }

    public void setData(byte[] data) {
        this.data = Arrays.copyOf(data, size);
    }

    public static MemoryBlock fromAddress(FileHeap heap, int address, int size) {
        byte[] data = new byte[size];
        heap.byteBuffer.get(address, data, 0, size);
        return new MemoryBlock(size, true, address);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MemoryBlock that = (MemoryBlock) o;
        return size == that.size &&
                used == that.used &&
                address == that.address;
    }

    @Override
    public int hashCode() {
        return Objects.hash(size, used, address);
    }
}
