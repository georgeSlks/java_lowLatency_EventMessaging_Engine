
package engine.clickhouseConnectors.http;

import engine.Event;
import engine.clickhouseConnectors.config.ClickHouseConfig;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

public class ClickHouseHttpWriter {

    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss.SSSSSSSSS")
                    .withZone(ZoneOffset.UTC);

    private final ClickHouseHttpClient client;
    private final ClickHouseConfig config;

    public ClickHouseHttpWriter(
            ClickHouseHttpClient client,
            ClickHouseConfig config) {

        this.client = client;
        this.config = config;
    }

    public void write(Event event)
            throws IOException, InterruptedException {

        String query = "INSERT INTO "
                + config.getDatabase()
                + ".engine_events "
                + "(event_id, event_time, event_type, payload) "
                + "FORMAT TabSeparated";

        String row = escapeTsv(event.getId()) + "\t"
                + TIMESTAMP_FORMAT.format(event.getTimestamp()) + "\t"
                + escapeTsv(event.getType().name()) + "\t"
                + escapeTsv(String.valueOf(event.getPayload()));

        URI uri = URI.create(
                client.getBaseUri()
                        + "/?query="
                        + URLEncoder.encode(query, StandardCharsets.UTF_8)
        );

        String credentials = config.getUser() + ":"
                + config.getPassword();

        String authorization = "Basic "
                + Base64.getEncoder().encodeToString(
                        credentials.getBytes(StandardCharsets.UTF_8)
                );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Authorization", authorization)
                .header("Content-Type", "text/plain; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(
                        row + "\n",
                        StandardCharsets.UTF_8
                ))
                .build();

        HttpResponse<String> response =
                client.getHttpClient().send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

        if (response.statusCode() != 200) {
            throw new IOException(
                    "ClickHouse insert failed: "
                            + response.statusCode()
                            + " - "
                            + response.body()
            );
        }
    }

    private String escapeTsv(String value) {

        StringBuilder result = new StringBuilder();

        for (char c : value.toCharArray()) {

            switch (c) {
                case '\\' -> result.append("\\\\");
                case '\t' -> result.append("\\t");
                case '\n' -> result.append("\\n");
                case '\r' -> result.append("\\r");
                case '\0' -> result.append("\\0");
                case '\b' -> result.append("\\b");
                case '\f' -> result.append("\\f");
                default -> result.append(c);
            }
        }

        return result.toString();
    }
}