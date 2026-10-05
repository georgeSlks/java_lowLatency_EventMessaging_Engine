package performance;

import engine.Event;
import engine.EventBus;
import engine.EventProcessor;
import engine.EventType;

import java.time.Instant;

public class EventGCTest {

    private static final int EVENTS = 20_000_000;

    public static void main(String[] args) {

        EventProcessor processor = new EventProcessor() {
            @Override
            public void process(Event event) {
                // No I/O.
            }
        };

        EventBus eventBus = new EventBus(processor);

        Runtime runtime = Runtime.getRuntime();

        long start = System.nanoTime();

        for (int i = 0; i < EVENTS; i++) {

            Event event = new Event(
                    "event-" + i,
                    Instant.now(),
                    EventType.DATA,
                    "payload"
            );

            eventBus.publish(event);

            if (i > 0 && i % 5_000_000 == 0) {

                long used =
                        runtime.totalMemory()
                                - runtime.freeMemory();

                System.out.println(
                        "Events: " + i
                                + " | Used memory: "
                                + formatBytes(used)
                );
            }
        }

        long elapsed = System.nanoTime() - start;

        double seconds = elapsed / 1_000_000_000.0;

        System.out.println();
        System.out.println("=================================");
        System.out.println("           GC TEST");
        System.out.println("=================================");
        System.out.println("Events       : " + EVENTS);
        System.out.println("Time         : " + seconds + " sec");
        System.out.println("=================================");
        System.out.println();
        System.out.println(
                "Run with GC logging to see actual GC activity."
        );
    }

    private static String formatBytes(long bytes) {

        if (bytes < 1024) {
            return bytes + " B";
        }

        if (bytes < 1024 * 1024) {
            return String.format("%.2f KB",
                    bytes / 1024.0);
        }

        if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB",
                    bytes / (1024.0 * 1024));
        }

        return String.format("%.2f GB",
                bytes / (1024.0 * 1024 * 1024));
    }
}
