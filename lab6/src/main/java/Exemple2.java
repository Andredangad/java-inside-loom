import java.util.List;

public class Exemple2 {
    private static boolean stop;

    public static void main(String[] args) {
        var scope = new ContinuationScope("scope");
        var continuation1 = new Continuation(scope, () -> {
            System.out.println("start 1");
            Continuation.yield(scope);
            System.out.println("middle 1");
            Continuation.yield(scope);
            System.out.println("end 1");
            Continuation.yield(scope);
            System.out.println("end 3");
        });
        var continuation2 = new Continuation(scope, () -> {
            System.out.println("start 2");
            Continuation.yield(scope);
            System.out.println("middle 2");
            Continuation.yield(scope);
            System.out.println("end 2");

        });
        var list = List.of(continuation1, continuation2);
        stop = false;
        while(!stop){


            list.forEach(e -> {
                if (!e.isDone()) {
                    e.run();
                }
                else {

                    stop = true;
                }
            });
        }

    }
}


