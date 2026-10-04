package engine;

import java.time.Instant;

public class Main {

    public static void main(String[] args) {

        EventProcessor processor = new EventProcessor();
        EventBus eventBus = new EventBus(processor);

        Event event = new Event(
                "event-1",
                Instant.now(),
                EventType.DATA,
                "hello"
        );

        eventBus.publish(event);
    }
}
