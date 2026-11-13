
package engine.network;

import engine.Event;
import engine.EventType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

public class EventDecoder {

    public Event decode(byte[] payload) throws IOException {

        if (payload == null || payload.length == 0) {
            throw new IOException("Empty event payload");
        }

        String message = new String(
                payload,
                StandardCharsets.UTF_8
        );

        int separator = message.indexOf('|');

        if (separator <= 0 || separator == message.length() - 1) {
            throw new IOException("Invalid event message");
        }

        String eventId = message.substring(0, separator);
        String eventPayload = message.substring(separator + 1);

        return new Event(
                eventId,
                Instant.now(),
                EventType.DATA,
                eventPayload
        );
    }
}