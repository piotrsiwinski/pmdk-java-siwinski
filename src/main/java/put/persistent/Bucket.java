package put.persistent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Bucket {
    private int size;
    private List<MemoryBlock> memoryBlocks = new ArrayList<>();

    public Bucket(int size) {
        this.size = size;
    }

    public void addMemoryBlock(MemoryBlock memoryBlock) {
        if (!memoryBlocks.contains(memoryBlock)) {
            memoryBlocks.add(memoryBlock);
        }

    }

    public Optional<MemoryBlock> getFirstFree() {
        return memoryBlocks.stream().filter(b -> !b.isUsed()).findFirst();
    }

    public void removeByAddress(int objectAddress) {
        MemoryBlock memoryBlock = memoryBlocks.stream()
                .filter(b -> b.getAddress() == objectAddress)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cannot find block by address"));
        memoryBlock.setUsed(false);
    }
}
