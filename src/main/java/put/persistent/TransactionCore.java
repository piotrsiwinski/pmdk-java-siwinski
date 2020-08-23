package put.persistent;

public interface TransactionCore {
    void start();
    void commit();
    void abort(TransactionException e);
}
