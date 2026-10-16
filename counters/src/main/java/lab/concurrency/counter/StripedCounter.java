package lab.concurrency.counter;

import java.util.concurrent.atomic.LongAdder;

public final class StripedCounter implements Counter {
    private final LongAdder count = new LongAdder();

    @Override
    public void increment() {
        count.increment();
    }

    /** Unlike AtomicLong.get(), this is not an atomic snapshot during writes. */
    @Override
    public long value() {
        return count.sum();
    }
}
