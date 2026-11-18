
package engine;

import engine.clickhouseConnectors.http.ClickHouseHttpWriter;

import java.io.IOException;

public class EventProcessor {

    private final ClickHouseHttpWriter writer;

    public EventProcessor(ClickHouseHttpWriter writer) {
        this.writer = writer;
    }

    public void process(Event event)
            throws IOException, InterruptedException {

        writer.write(event);
    }
}