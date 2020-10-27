import java.util.concurrent.locks.ReentrantLock;

public class Exemple1 {

    private final static ReentrantLock reentrantLock = new ReentrantLock();

    public static void main(String[] args) {
        var lock = new Object();

        var scope = new ContinuationScope("scope");
        var continuation = new Continuation(scope, () -> {
//            synchronized (reentrantLock){
//                Continuation.yield(scope);
//            }
            reentrantLock.lock();  // block until condition holds
            try {
                Continuation.yield(scope);
            } finally {
                reentrantLock.unlock();
            }


            System.out.println(Continuation.getCurrentContinuation(scope));
            System.out.println("Hello continuation");


        });

        continuation.run();
        continuation.run();

    }
}
