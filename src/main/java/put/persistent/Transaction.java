package put.persistent;

public class Transaction {
    public enum State {None, Initializing, Active, Committed, Aborted}

    public static void run(Runnable body) {
        body.run();
    }
    public static void run(Heap heap, Runnable body) {
        body.run();
    }
}
