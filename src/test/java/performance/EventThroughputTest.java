package performance;

import engine.Event;
import engine.EventBus;
import engine.EventProcessor;
import engine.EventType;

import java.time.Instant;

public class EventThroughputTest {

    private static final int WARMUP = 1_000_000;
    private static final int ITERATIONS = 10_000_000;

    public static void main(String[] args) {

        EventProcessor processor = new EventProcessor() {
            @Override
            public void process(Event event) {
                // No I/O.
            }
        };

        EventBus eventBus = new EventBus(processor);

        Event event = new Event(
                "throughput-test",
                Instant.now(),
                EventType.DATA,
                "payload"
        );

        // Warmup JVM/JIT
        for (int i = 0; i < WARMUP; i++) {
            eventBus.publish(event);
        }

        long start = System.nanoTime();

        for (int i = 0; i < ITERATIONS; i++) {
            eventBus.publish(event);
        }

        long elapsed = System.nanoTime() - start;

        double seconds = elapsed / 1_000_000_000.0;
        double eventsPerSecond = ITERATIONS / seconds;

        System.out.println("=================================");
        System.out.println("      EVENT THROUGHPUT TEST");
        System.out.println("=================================");
        System.out.println("Events       : " + ITERATIONS);
        System.out.println("Time         : " + seconds + " sec");
        System.out.println("Throughput   : "
                + String.format("%.2f", eventsPerSecond)
                + " events/sec");
        System.out.println("=================================");
    }
}
