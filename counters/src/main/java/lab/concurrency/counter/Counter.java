package lab.concurrency.counter;

/** An increment-only counter. Reads are exact once all writers have finished. */
public interface Counter {
    void increment();

    long value();
}
