package engine.clickhouse;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Objects;

public final class ClickHouseClient {

    private final ClickHouseConfig config;
    private final HttpClient httpClient;

    public ClickHouseClient(ClickHouseConfig config) {
        this.config = Objects.requireNonNull(config);

        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
    }

    public void insertJsonEachRow(
            String table,
            String rows) throws IOException, InterruptedException {

        Objects.requireNonNull(table);
        Objects.requireNonNull(rows);

        if (!table.matches("[A-Za-z_][A-Za-z0-9_]*")) {
            throw new IllegalArgumentException("Invalid table name");
        }

        String query = "INSERT INTO "
                + quoteIdentifier(config.database())
                + "."
                + quoteIdentifier(table)
                + " FORMAT JSONEachRow";

        String url = config.endpoint().toString()
                + (config.endpoint().toString().contains("?") ? "&" : "?")
                + "query="
                + URLEncoder.encode(query, StandardCharsets.UTF_8);

        String credentials = config.username() + ":" + config.password();

        String authorization = "Basic " + Base64.getEncoder()
                .encodeToString(
                        credentials.getBytes(StandardCharsets.UTF_8));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(config.requestTimeout())
                .header("Authorization", authorization)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                        rows, StandardCharsets.UTF_8))
                .build();

        HttpResponse<Void> response = httpClient.send(
                request, HttpResponse.BodyHandlers.discarding());

        if (response.statusCode() < 200
                || response.statusCode() >= 300) {
            throw new IOException(
                    "ClickHouse HTTP error: "
                            + response.statusCode());
        }
    }

    private static String quoteIdentifier(String identifier) {
        return "`" + identifier.replace("`", "``") + "`";
    }
}