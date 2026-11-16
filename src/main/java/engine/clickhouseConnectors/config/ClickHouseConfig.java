
package engine.clickhouseConnectors.config;

public class ClickHouseConfig {

    private final String host;
    private final String database;
    private final String user;
    private final String password;

    private final int httpPort;
    private final int nativePort;

    private final String transport;

    public ClickHouseConfig() {

        this.host = System.getenv("CLICKHOUSE_HOST");
        this.database = System.getenv("CLICKHOUSE_DATABASE");
        this.user = System.getenv("CLICKHOUSE_USER");
        this.password = System.getenv("CLICKHOUSE_PASSWORD");

        this.httpPort = Integer.parseInt(
                System.getenv("CLICKHOUSE_HTTP_PORT")
        );

        this.nativePort = Integer.parseInt(
                System.getenv("CLICKHOUSE_NATIVE_PORT")
        );

        this.transport = System.getenv("CLICKHOUSE_TRANSPORT");

        validate();
    }

    private void validate() {

        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("Missing ClickHouse host");
        }

        if (database == null || database.isBlank()) {
            throw new IllegalArgumentException("Missing ClickHouse database");
        }

        if (user == null || user.isBlank()) {
            throw new IllegalArgumentException("Missing ClickHouse user");
        }

        if (httpPort < 1 || httpPort > 65535) {
            throw new IllegalArgumentException("Invalid HTTP port");
        }

        if (nativePort < 1 || nativePort > 65535) {
            throw new IllegalArgumentException("Invalid Native TCP port");
        }

        if (!transport.equals("http") && !transport.equals("native_tcp")) {
            throw new IllegalArgumentException("Invalid ClickHouse transport");
        }
    }

    public String getHost() {
        return host;
    }

    public String getDatabase() {
        return database;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public int getHttpPort() {
        return httpPort;
    }

    public int getNativePort() {
        return nativePort;
    }

    public String getTransport() {
        return transport;
    }
}