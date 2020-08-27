package put.persistent;

import java.util.UUID;

public class XTransactionCore implements TransactionCore {
    private UUID transactionId;
    private final FileHeap fileHeap;

    public XTransactionCore(Heap heap) {
        this.fileHeap = (FileHeap) heap; // todo: fix - don't cast
        FileHeap.transactionLock.lock();

    }


    // operacje na UNDO LOG
    @Override
    public void start() {
        // utowrzenie wpisu w UNDO LOG
        System.out.println("NATIVE START TRANSACTION");
    }

    @Override
    public void commit() {
        // zatwierdzenie
        System.out.println("NATIVE COMMIT TRANSACTION");
    }

    @Override
    public void abort(TransactionException e) {
        System.out.println("NATIVE ABORT TRANSACTION");
    }
}
