import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;

public class Scheduler {
    private final List<Continuation> listContinuation = new ArrayList<Continuation>();

    private interface PolicyImpl{
        boolean isEmpty();
        void add(Continuation continuation);
        Continuation remove();

    }
    private enum Policy {
        STACK{

            PolicyImpl createImpl(){
                return new PolicyImpl(){
                    private final ArrayDeque<Continuation> queue = new ArrayDeque<>();

                    @Override
                    public boolean isEmpty() {
                        return queue.isEmpty();
                    }

                    @Override
                    public void add(Continuation continuation) {
                        queue.offerLast(continuation);
                    }

                    @Override
                    public Continuation remove() {
                        return queue.removeLast();
                    }
                };

            }
        }, FIFO{
            PolicyImpl createImpl(){
                return new PolicyImpl(){
                    private final ArrayDeque<Continuation> queue = new ArrayDeque<>();

                    @Override
                    public boolean isEmpty() {
                        return queue.isEmpty();
                    }

                    @Override
                    public void add(Continuation continuation) {
                        queue.offerFirst(continuation);
                    }

                    @Override
                    public Continuation remove() {
                        return queue.removeFirst();
                    }
                };
            }
        }, RANDOM{
            PolicyImpl createImpl(){
                return new PolicyImpl(){
                    private final TreeMap<Integer, ArrayDeque<Continuation>> tree = new TreeMap<>();

                    @Override
                    public boolean isEmpty() {
                        return tree.isEmpty();
                    }

                    @Override
                    public void add(Continuation continuation) {
                        var random = ThreadLocalRandom.current().nextInt();
                        tree.computeIfAbsent(random, __->new ArrayDeque<>()).offer(continuation);
                    }

                    @Override
                    public Continuation remove() {
                        var random = ThreadLocalRandom.current().nextInt();
                        var key = tree.floorKey(random);
                        if(key == null){
                            key = tree.firstKey();
                        }
                        var queue = tree.get(key);
                        var continuation = queue.poll();
                        if(queue.isEmpty()){
                            tree.remove(key);
                        }
                        return continuation;
                    }
                };
            }
        };
        abstract PolicyImpl createImpl();
    }
    private PolicyImpl policyImpl;
    public Scheduler(Policy policy){
        this.policyImpl = policy.createImpl();


    }

    public static void main(String[] args) {
        var scope = new ContinuationScope("scope");
        var scheduler = new Scheduler(Policy.STACK);
        var continuation1 = new Continuation(scope, () -> {
            System.out.println("start 1");
            scheduler.enqueue(scope);
            System.out.println("middle 1");
            scheduler.enqueue(scope);
            System.out.println("end 1");
        });
        var continuation2 = new Continuation(scope, () -> {
            System.out.println("start 2");
            scheduler.enqueue(scope);
            System.out.println("middle 2");
            scheduler.enqueue(scope);
            System.out.println("end 2");
        });
        var list = List.of(continuation1, continuation2);
        list.forEach(Continuation::run);
        scheduler.runLoop();
    }

    private void runLoop() {
        while(!this.policyImpl.isEmpty()){
            policyImpl.remove().run();
        }

    }

    private void enqueue(ContinuationScope scope) {
        var current = Continuation.getCurrentContinuation(scope);
        if(current != null){
            policyImpl.add(current);
            Continuation.yield(scope);

        }
        else{
            throw new NullPointerException();
        }
    }
}
