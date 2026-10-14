package engine;

import java.time.Instant;

public class Main {

    public static void main(String[] args) throws InterruptedException {

        RingBuffer ringBuffer = new RingBuffer(10);
        Partition partition = new Partition(ringBuffer);

        EventProcessor processor = new EventProcessor();
        EventConsumer consumer = new EventConsumer(partition, processor);
        EventBus eventBus = new EventBus(partition);

        Thread consumerThread =
                new Thread(consumer, "event-consumer");

        consumerThread.start();

        try {
            for (int i = 1; i <= 20; i++) {
                Event event = new Event(
                        "event-" + i,
                        Instant.now(),
                        EventType.DATA,
                        "payload-" + i
                );

                eventBus.publish(event);

                System.out.println(
                        "Published " + event.getId()
                        + " by " + Thread.currentThread().getName()
                );
            }
        } finally {
            // Stop accepting events and let the consumer drain the queue.
            partition.close();
        }

        consumerThread.join();

        System.out.println("Messaging engine stopped.");
    }
}