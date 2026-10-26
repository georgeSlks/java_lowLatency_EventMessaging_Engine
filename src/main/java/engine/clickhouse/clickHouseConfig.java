package engine.clickhouse;

import java.net.URI;
import java.time.Duration;
import java.util.Objects;

public final class ClickHouseConfig {

    private final URI endpoint;
    private final String database;
    private final String username;
    private final String password;
    private final Duration requestTimeout;

    public ClickHouseConfig(
            URI endpoint,
            String database,
            String username,
            String password,
            Duration requestTimeout) {

        this.endpoint = Objects.requireNonNull(endpoint);
        this.database = Objects.requireNonNull(database);
        this.username = Objects.requireNonNull(username);
        this.password = Objects.requireNonNull(password);
        this.requestTimeout =
                Objects.requireNonNull(requestTimeout);

        if (!"http".equalsIgnoreCase(endpoint.getScheme())
                && !"https".equalsIgnoreCase(endpoint.getScheme())) {
            throw new IllegalArgumentException(
                    "Endpoint must use HTTP or HTTPS");
        }

        if (requestTimeout.isZero() || requestTimeout.isNegative()) {
            throw new IllegalArgumentException(
                    "Timeout must be positive");
        }
    }

    public URI endpoint() {
        return endpoint;
    }

    public String database() {
        return database;
    }

    public String username() {
        return username;
    }

    public String password() {
        return password;
    }

    public Duration requestTimeout() {
        return requestTimeout;
    }
}