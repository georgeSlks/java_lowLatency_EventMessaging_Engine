package engine;

import java.time.Instant;

public class Event {

    private final String id;
    private final Instant timestamp;
    private final EventType type;
    private final Object payload;

    public Event(String id, Instant timestamp, EventType type, Object payload) {
        this.id = id;
        this.timestamp = timestamp;
        this.type = type;
        this.payload = payload;
    }

    public String getId() {
        return id;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public EventType getType() {
        return type;
    }

    public Object getPayload() {
        return payload;
    }
}
