package put.persistent;

public interface TransactionCore {
    void start();
    public void commit();
    public void abort(TransactionException e);
}
