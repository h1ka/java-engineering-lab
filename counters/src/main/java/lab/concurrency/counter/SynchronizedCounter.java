package lab.concurrency.counter;

public final class SynchronizedCounter implements Counter {
    private long count;

    @Override
    public synchronized void increment() {
        count++;
    }

    @Override
    public synchronized long value() {
        return count;
    }
}
