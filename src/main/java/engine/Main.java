package engine;

import java.time.Instant;

public class Main {

    public static void main(String[] args) {

        // Create the processing components
        EventProcessor processor = new EventProcessor();

        // Create the buffer and partition
        RingBuffer ringBuffer = new RingBuffer(10);
        Partition partition = new Partition(ringBuffer);

        // Create the consumer
        EventConsumer consumer = new EventConsumer(partition, processor);

        // Create the event bus
        EventBus eventBus = new EventBus(partition);

        // Produce an event
        Event event = new Event(
                "event-1",
                Instant.now(),
                EventType.DATA,
                "hello"
        );

        // Send the event to the engine
        eventBus.publish(event);

        // Consume and process the event
        consumer.consume();
    }
}