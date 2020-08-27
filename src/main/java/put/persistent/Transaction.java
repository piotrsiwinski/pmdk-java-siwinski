package put.persistent;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Transaction {
    private final static Lock lock = new ReentrantLock();
    private static final int MONITOR_ENTER_TIMEOUT = 30; // ms
    private static final int BASE_TRANSACTION_RETRY_DELAY = 200; //ms
    private static final int MAX_TRANSACTION_ATTEMPTS = 10;

    private final TransactionCore core;
    private State state;
    private int depth;

    private Transaction(TransactionCore core) {
        this.core = core;
        this.state = State.None;
    }

    public static void run(Heap heap, Runnable body) {
        var transaction = new Transaction(new XTransactionCore(heap));

        boolean success = false;
        int attempts = 1;
        int sleepTime = MONITOR_ENTER_TIMEOUT;
        int retryDelay = BASE_TRANSACTION_RETRY_DELAY;

        while (!success && attempts <= MAX_TRANSACTION_ATTEMPTS) {
            transaction.depth++;
            try {
                transaction.start();
                body.run();
                success = true;
            } catch (Throwable t) {
                transaction.abort(new TransactionException(t));
                success = false;
            } finally {
                transaction.commit();
            }
            if (!success) {
                attempts++;
            }
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
        if (!success) {
            throw new TransactionException(String.format("failed to execute transaction after %d attempts", attempts));
        }
    }


    private void start() {
        lock.lock();
        if (depth == 1) {
            if (state == State.None) {
                state = State.Active;
                core.start();
            }
        }

    }

    private void abort(TransactionException t) {
        if (state != State.Active) {
            state = State.Aborted;
            lock.unlock();
            return;
        }
        if (depth == 1) {
            core.abort(t);
            state = State.Aborted;
            lock.unlock();
        }
    }

    private void commit() {
        if (depth == 1) {
            if (state == State.None) {
                return;
            }
            if (state == State.Aborted) {
                return;
            }
            core.commit();
            lock.unlock();
        }
        depth--;
    }

    private enum State {None, Initializing, Active, Committed, Aborted}

}
