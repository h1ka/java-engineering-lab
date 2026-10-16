package lab.concurrency.stress;

import lab.concurrency.counter.AtomicCounter;
import org.openjdk.jcstress.annotations.Actor;
import org.openjdk.jcstress.annotations.Arbiter;
import org.openjdk.jcstress.annotations.Expect;
import org.openjdk.jcstress.annotations.JCStressTest;
import org.openjdk.jcstress.annotations.Outcome;
import org.openjdk.jcstress.annotations.State;
import org.openjdk.jcstress.infra.results.J_Result;

@JCStressTest
@Outcome(id = "2", expect = Expect.ACCEPTABLE, desc = "Both increments are preserved.")
@Outcome(expect = Expect.FORBIDDEN, desc = "A thread-safe counter must not lose updates.")
@State
public class AtomicCounterStress {
    private final AtomicCounter counter = new AtomicCounter();

    @Actor
    public void first() {
        counter.increment();
    }

    @Actor
    public void second() {
        counter.increment();
    }

    @Arbiter
    public void observe(J_Result result) {
        result.r1 = counter.value();
    }
}
