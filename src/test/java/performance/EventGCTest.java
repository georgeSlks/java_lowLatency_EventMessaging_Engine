package performance;

import engine.Event;
import engine.EventBus;
import engine.EventProcessor;
import engine.EventType;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.time.Instant;
import java.util.List;

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

        /*
         * Take GC measurements BEFORE the workload.
         */
        long gcCollectionsBefore = getGcCollectionCount();
        long gcTimeBefore = getGcCollectionTime();

        /*
         * Start measuring total execution time.
         */
        long start = System.nanoTime();

        for (int i = 0; i < EVENTS; i++) {

            Event event = new Event(
                    "event-" + i,
                    Instant.now(),
                    EventType.DATA,
                    "payload"
            );

            eventBus.publish(event);
        }

        long elapsed = System.nanoTime() - start;

        /*
         * Take GC measurements AFTER the workload.
         */
        long gcCollectionsAfter = getGcCollectionCount();
        long gcTimeAfter = getGcCollectionTime();

        /*
         * Calculate differences.
         */
        long gcCollections =
                gcCollectionsAfter - gcCollectionsBefore;

        long gcTime =
                gcTimeAfter - gcTimeBefore;

        double seconds =
                elapsed / 1_000_000_000.0;

        double throughput =
                EVENTS / seconds;

        double gcOverhead =
                (gcTime / 1000.0) / seconds * 100.0;

        System.out.println();
        System.out.println("=================================");
        System.out.println("           GC TEST");
        System.out.println("=================================");

        System.out.println("Events          : " + EVENTS);

        System.out.println(
                "Execution time  : "
                        + String.format("%.3f", seconds)
                        + " sec"
        );

        System.out.println(
                "Throughput      : "
                        + String.format("%.2f", throughput)
                        + " events/sec"
        );

        System.out.println(
                "GC collections  : "
                        + gcCollections
        );

        System.out.println(
                "GC time         : "
                        + gcTime
                        + " ms"
        );

        System.out.println(
                "GC overhead     : "
                        + String.format("%.2f", gcOverhead)
                        + "%"
        );

        System.out.println("=================================");
    }

    private static long getGcCollectionCount() {

        long total = 0;

        List<GarbageCollectorMXBean> beans =
                ManagementFactory.getGarbageCollectorMXBeans();

        for (GarbageCollectorMXBean bean : beans) {

            long count = bean.getCollectionCount();

            if (count != -1) {
                total += count;
            }
        }

        return total;
    }

    private static long getGcCollectionTime() {

        long total = 0;

        List<GarbageCollectorMXBean> beans =
                ManagementFactory.getGarbageCollectorMXBeans();

        for (GarbageCollectorMXBean bean : beans) {

            long time = bean.getCollectionTime();

            if (time != -1) {
                total += time;
            }
        }

        return total;
    }
}
