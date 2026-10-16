package lab.concurrency.benchmark;

import java.util.concurrent.TimeUnit;
import lab.concurrency.counter.AtomicCounter;
import lab.concurrency.counter.StripedCounter;
import lab.concurrency.counter.SynchronizedCounter;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
@Threads(4)
public class CounterBenchmark {
    // Every worker in a trial contends on the same counters, not thread-local copies.
    @State(Scope.Benchmark)
    public static class SharedCounters {
        AtomicCounter atomic;
        SynchronizedCounter monitor;
        StripedCounter striped;

        @Setup(Level.Iteration)
        public void reset() {
            atomic = new AtomicCounter();
            monitor = new SynchronizedCounter();
            striped = new StripedCounter();
        }
    }

    @Benchmark
    public void atomicLong(SharedCounters counters) {
        counters.atomic.increment();
    }

    @Benchmark
    public void synchronizedCounter(SharedCounters counters) {
        counters.monitor.increment();
    }

    @Benchmark
    public void longAdder(SharedCounters counters) {
        counters.striped.increment();
    }
}
