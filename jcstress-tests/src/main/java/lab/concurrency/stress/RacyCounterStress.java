package lab.concurrency.stress;

import org.openjdk.jcstress.annotations.Actor;
import org.openjdk.jcstress.annotations.Arbiter;
import org.openjdk.jcstress.annotations.Expect;
import org.openjdk.jcstress.annotations.JCStressTest;
import org.openjdk.jcstress.annotations.Outcome;
import org.openjdk.jcstress.annotations.State;
import org.openjdk.jcstress.infra.results.I_Result;

/** Deliberately unsafe teaching example, not a Counter implementation. */
@JCStressTest
@Outcome(id = "2", expect = Expect.ACCEPTABLE, desc = "The increments happened without a lost update.")
@Outcome(id = "1", expect = Expect.ACCEPTABLE_INTERESTING, desc = "Lost update: ++ is not atomic.")
@Outcome(expect = Expect.FORBIDDEN, desc = "No other final count is expected.")
@State
public class RacyCounterStress {
    private int count;

    @Actor
    public void first() {
        count++;
    }

    @Actor
    public void second() {
        count++;
    }

    @Arbiter
    public void observe(I_Result result) {
        result.r1 = count;
    }
}
