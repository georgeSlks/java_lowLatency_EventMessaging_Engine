package benchmark;

import engine.Event;
import engine.EventType;
import engine.RingBuffer;

import org.openjdk.jmh.annotations.*;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
public class ProducerBenchmark {

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
    }

    @TearDown(Level.Invocation)
    public void cleanup() throws InterruptedException {
        ringBuffer.poll();
    }
}