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
public class ConsumerBenchmark {

    private RingBuffer ringBuffer;
    private Event event;

    @Setup(Level.Trial)
    public void setup() {
        ringBuffer = new RingBuffer(1024);

        event = new Event(
                "benchmark-event",
                Instant.now(),
                EventType.DATA,
                "benchmark-payload"
        );
    }

    @Setup(Level.Invocation)
    public void prepare() throws InterruptedException {
        ringBuffer.add(event);
    }

    @Benchmark
    public Event pollEvent() throws InterruptedException {
        return ringBuffer.poll();
    }
}