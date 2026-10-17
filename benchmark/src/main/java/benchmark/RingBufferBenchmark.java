package benchmark;

import engine.Event;
import engine.EventType;
import engine.RingBuffer;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
public class RingBufferBenchmark {

    private RingBuffer ringBuffer;
    private Event event;

    @Setup(Level.Iteration)
    public void setup() {
        ringBuffer = new RingBuffer(1024);

        event = new Event(
                "benchmark-event",
                Instant.now(),
                EventType.DATA,
                "benchmark-payload"
        );
    }

    @Benchmark
    public void addEvent() throws InterruptedException {
        ringBuffer.add(event);
        ringBuffer.poll();
    }
}