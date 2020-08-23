package put.persistent;

public class XTransactionCore implements TransactionCore {


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
