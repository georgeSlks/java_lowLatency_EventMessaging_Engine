package performance;

import engine.Event;
import engine.EventBus;
import engine.EventProcessor;
import engine.EventType;

import java.time.Instant;

public class EventLatencyTest {

    private static final int WARMUP = 100_000;
    private static final int ITERATIONS = 1_000_000;

    public static void main(String[] args) {

        EventProcessor processor = new EventProcessor() {
            @Override
            public void process(Event event) {
                // Intentionally empty.
                // We want to measure the EventBus path,
                // not console I/O.
            }
        };

        EventBus eventBus = new EventBus(processor);

        Event event = new Event(
                "latency-test",
                Instant.now(),
                EventType.DATA,
                "payload"
        );

        // Warmup
        for (int i = 0; i < WARMUP; i++) {
            eventBus.publish(event);
        }

        long total = 0;
        long min = Long.MAX_VALUE;
        long max = Long.MIN_VALUE;

        for (int i = 0; i < ITERATIONS; i++) {

            long start = System.nanoTime();

            eventBus.publish(event);

            long elapsed = System.nanoTime() - start;

            total += elapsed;
            min = Math.min(min, elapsed);
            max = Math.max(max, elapsed);
        }

        double average = (double) total / ITERATIONS;

        System.out.println("=================================");
        System.out.println("       EVENT LATENCY TEST");
        System.out.println("=================================");
        System.out.println("Iterations : " + ITERATIONS);
        System.out.println("Min        : " + min + " ns");
        System.out.println("Average    : " + String.format("%.2f", average) + " ns");
        System.out.println("Max        : " + max + " ns");
        System.out.println("=================================");
    }
}
