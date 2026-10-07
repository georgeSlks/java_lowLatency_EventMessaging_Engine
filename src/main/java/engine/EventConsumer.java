package engine;

public class EventConsumer {

    private final Partition partition;
    private final EventProcessor processor;

    public EventConsumer(Partition partition, EventProcessor processor) {
        this.partition = partition;
        this.processor = processor;
    }

    public void consume() {
        Event event = partition.consume();

        if (event != null) {
            processor.process(event);
        }
    }
}