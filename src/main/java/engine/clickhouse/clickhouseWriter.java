package engine.clickhouse;

import engine.Event;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

public final class ClickHouseWriter {

    private final ClickHouseClient client;

    public ClickHouseWriter(ClickHouseClient client) {
        this.client = Objects.requireNonNull(client);
    }

    public void writeBatch(List<Event> events)
            throws IOException, InterruptedException {

        if (events.isEmpty()) {
            return;
        }

        StringBuilder body = new StringBuilder(
                events.size() * 128);

        for (Event event : events) {
            if (event == null) {
                throw new IllegalArgumentException(
                        "Batch contains a null event");
            }

            body.append("{\"event_id\":\"")
                .append(escape(event.getId()))
                .append("\",\"event_time\":\"")
                .append(DateTimeFormatter.ISO_INSTANT.format(
                        event.getTimestamp()))
                .append("\",\"event_type\":\"")
                .append(escape(event.getType().name()))
                .append("\",\"payload\":\"")
                .append(escape(event.getPayload()))
                .append("\"}\n");
        }

        client.insertJsonEachRow(
                "engine_events", body.toString());
    }

    private static String escape(String value) {
        Objects.requireNonNull(value, "JSON field cannot be null");

        StringBuilder out = new StringBuilder(value.length() + 16);

        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);

            switch (c) {
                case '"':  out.append("\\\""); break;
                case '\\': out.append("\\\\"); break;
                case '\b': out.append("\\b");  break;
                case '\f': out.append("\\f");  break;
                case '\n': out.append("\\n");  break;
                case '\r': out.append("\\r");  break;
                case '\t': out.append("\\t");  break;
                default:
                    if (c < 0x20) {
                        out.append(String.format(
                                "\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
            }
        }

        return out.toString();
    }
}