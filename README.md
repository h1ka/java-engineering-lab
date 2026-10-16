# Java Engineering Lab

A Java 21 concurrency playground with JUnit 5, OpenJDK JCStress, and JMH.

## Build and unit tests

Requirements: JDK 21 and Maven 3.9+. In the prepared cloud environment, activate
the toolchain first with `source /workspace/.cloud-java/env.sh`.

From the repository root:

```bash
mvn --strict-checksums --batch-mode --no-transfer-progress clean verify
```

This runs nine JUnit cases and builds both executable harnesses. **Maven does
not automatically execute JCStress or JMH.** Run them separately as shown below.

| Module | Contents |
| --- | --- |
| `counters` | `AtomicLong`, `synchronized`, and `LongAdder` counter implementations; parameterized JUnit tests |
| `jcstress-tests` | Actor-based concurrency tests and an executable `target/jcstress.jar` |
| `benchmarks` | Contended increment throughput benchmarks and an executable `target/benchmarks.jar` |

JUnit checks initial state, sequential updates, and eight workers performing
10,000 increments each. A start gate creates contention, futures propagate worker
failures, and timeouts bound the tests. The final count is read after every worker
has completed. `LongAdder.sum()` is exact at that point, but does not provide an
atomic snapshot while writers are active.

## JCStress: correctness under concurrency

| Test | Expected outcomes |
| --- | --- |
| `AtomicCounterStress` | Two actors increment the counter; the arbiter must observe `2`. |
| `RacyCounterStress` | Deliberately unsafe `count++`: `2` is acceptable, and `1` is an interesting lost update. |
| `VolatilePublicationStress` | The reader sees either no object (`-1`) or its initialized value (`42`); a default field value is forbidden. |

Quick smoke run from the repository root:

```bash
java -jar jcstress-tests/target/jcstress.jar \
  -t 'lab.concurrency.stress.*' \
  -m quick -f 1 -fsm 1 -iters 1 -time 100 \
  -c 2 -af NONE -hs 128 -jvmArgs '-Xms64m -Xmx128m' \
  -r jcstress-tests/target/report
```

Open `jcstress-tests/target/report/index.html` for the outcome tables. JCStress
also writes a timestamped `jcstress-results-*.bin.gz` file, ignored by Git.
`ACCEPTABLE_INTERESTING` is intentional for the racy example; it demonstrates a
bug rather than failing the harness. A short run might not observe that outcome.

For a longer investigation with automatically selected JVM configurations:

```bash
java -jar jcstress-tests/target/jcstress.jar \
  -t 'lab.concurrency.stress.*' -m default -c 2 -af NONE -hs 256 \
  -r jcstress-tests/target/report
```

`-af NONE` avoids CPU affinity restrictions in containers. The quick run limits
time, memory, and JVM configurations; neither run proves correctness across every
hardware platform or permitted Java Memory Model execution. Add a new annotated
class under `jcstress-tests/src/main/java/lab/concurrency/stress`, declare its
allowed and forbidden outcomes, then rebuild to regenerate the test registry.

## JMH: performance measurements

`CounterBenchmark` compares increment throughput for the same three counter
implementations. `Scope.Benchmark` shares state across workers so they contend
on the same counter. Counters reset between iterations, outside measured code.
The benchmark measures writes only; it does not compare read costs or claim that
`LongAdder` has the same snapshot semantics as `AtomicLong`.

Short harness check:

```bash
java -jar benchmarks/target/benchmarks.jar '.*CounterBenchmark.*' \
  -wi 1 -i 1 -w 200ms -r 200ms -f 1 -t 2 -foe true \
  -rf json -rff benchmarks/target/smoke.json
```

For measurements, the defaults use three one-second warmup iterations, five
one-second measurement iterations, two forks, and four threads:

```bash
java -jar benchmarks/target/benchmarks.jar '.*CounterBenchmark.*' \
  -foe true -rf json -rff benchmarks/target/results.json
```

Compare contention levels with `-t 1`, `-t 2`, or `-t 4`. Keep hardware, JDK,
thread count, and other system load consistent, and inspect error estimates
before drawing conclusions. Smoke-run timings only verify the harness runs.
Add benchmarks under `benchmarks/src/main/java`; the annotation processor
generates their registry when Maven builds the module.
