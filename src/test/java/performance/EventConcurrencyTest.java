package performance;

import engine.Event;
import engine.EventBus;
import engine.EventProcessor;
import engine.EventType;

import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

public class EventConcurrencyTest {

    private static final int THREADS = 8;
    private static final int EVENTS_PER_THREAD = 1_000_000;

    public static void main(String[] args) throws InterruptedException {

        AtomicLong processed = new AtomicLong();

        EventProcessor processor = new EventProcessor() {
            @Override
            public void process(Event event) {
                processed.incrementAndGet();
            }
        };

        EventBus eventBus = new EventBus(processor);

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(THREADS);

        Thread[] threads = new Thread[THREADS];

        for (int t = 0; t < THREADS; t++) {

            threads[t] = new Thread(() -> {

                try {
                    startLatch.await();

                    for (int i = 0; i < EVENTS_PER_THREAD; i++) {

                        Event event = new Event(
                                "event-" + i,
                                Instant.now(),
                                EventType.DATA,
                                "payload"
                        );

                        eventBus.publish(event);
                    }

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        for (Thread thread : threads) {
            thread.start();
        }

        long start = System.nanoTime();

        startLatch.countDown();

        doneLatch.await();

        long elapsed = System.nanoTime() - start;

        long totalEvents = (long) THREADS * EVENTS_PER_THREAD;

        double seconds = elapsed / 1_000_000_000.0;
        double throughput = totalEvents / seconds;

        System.out.println("=================================");
        System.out.println("      EVENT CONCURRENCY TEST");
        System.out.println("=================================");
        System.out.println("Threads      : " + THREADS);
        System.out.println("Events/thread: " + EVENTS_PER_THREAD);
        System.out.println("Total events : " + totalEvents);
        System.out.println("Processed    : " + processed.get());
        System.out.println("Time         : " + seconds + " sec");
        System.out.println("Throughput   : "
                + String.format("%.2f", throughput)
                + " events/sec");
        System.out.println("=================================");
    }
}
