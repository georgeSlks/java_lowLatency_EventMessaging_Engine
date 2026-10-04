package engine;

public class EventBus {

    private final EventProcessor processor;

    public EventBus(EventProcessor processor) {
        this.processor = processor;
    }

    public void publish(Event event) {
        processor.process(event);
    }
}
