package lab.concurrency.counter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Timeout(20)
class CounterTest {
    static Stream<Arguments> counters() {
        return Stream.of(
                Arguments.of("AtomicLong", (Supplier<Counter>) AtomicCounter::new),
                Arguments.of("synchronized", (Supplier<Counter>) SynchronizedCounter::new),
                Arguments.of("LongAdder", (Supplier<Counter>) StripedCounter::new));
    }

    @ParameterizedTest(name = "{0}: starts at zero")
    @MethodSource("counters")
    void startsAtZero(String name, Supplier<Counter> factory) {
        assertEquals(0, factory.get().value());
    }

    @ParameterizedTest(name = "{0}: sequential increments")
    @MethodSource("counters")
    void incrementsSequentially(String name, Supplier<Counter> factory) {
        Counter counter = factory.get();
        counter.increment();
        counter.increment();
        assertEquals(2, counter.value());
    }

    @ParameterizedTest(name = "{0}: concurrent increments")
    @MethodSource("counters")
    void preservesAllConcurrentIncrements(String name, Supplier<Counter> factory) throws Exception {
        int workers = 8;
        int incrementsPerWorker = 10_000;
        Counter counter = factory.get();
        CountDownLatch ready = new CountDownLatch(workers);
        CountDownLatch start = new CountDownLatch(1);
        var executor = Executors.newFixedThreadPool(workers);
        try {
            List<Future<?>> tasks = new ArrayList<>();
            for (int worker = 0; worker < workers; worker++) {
                tasks.add(executor.submit(() -> {
                    ready.countDown();
                    start.await();
                    for (int i = 0; i < incrementsPerWorker; i++) {
                        counter.increment();
                    }
                    return null;
                }));
            }
            assertTrue(ready.await(5, TimeUnit.SECONDS), "Workers did not reach the start gate");
            start.countDown();
            for (Future<?> task : tasks) {
                // Propagate worker failures and establish completion before reading LongAdder.
                task.get(10, TimeUnit.SECONDS);
            }
            assertEquals((long) workers * incrementsPerWorker, counter.value());
        } finally {
            start.countDown();
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS), "Workers did not stop");
        }
    }
}
