package lab.concurrency.stress;

import org.openjdk.jcstress.annotations.Actor;
import org.openjdk.jcstress.annotations.Expect;
import org.openjdk.jcstress.annotations.JCStressTest;
import org.openjdk.jcstress.annotations.Outcome;
import org.openjdk.jcstress.annotations.State;
import org.openjdk.jcstress.infra.results.I_Result;

@JCStressTest
@Outcome(id = "-1", expect = Expect.ACCEPTABLE, desc = "Reader ran before publication.")
@Outcome(id = "42", expect = Expect.ACCEPTABLE, desc = "Volatile publication made initialization visible.")
@Outcome(expect = Expect.FORBIDDEN, desc = "A published object must not expose its default field value.")
@State
public class VolatilePublicationStress {
    private volatile Payload published;

    static class Payload {
        // Intentionally non-final: the volatile reference supplies safe publication.
        int value;

        Payload(int value) {
            this.value = value;
        }
    }

    @Actor
    public void writer() {
        published = new Payload(42);
    }

    @Actor
    public void reader(I_Result result) {
        Payload observed = published;
        result.r1 = observed == null ? -1 : observed.value;
    }
}
