
package engine.clickhouseConnectors.config;

public class ClickHouseConfig {

    private final String host;
    private final String database;
    private final String user;
    private final String password;

    private final int httpPort;

    public ClickHouseConfig() {

        this.host = System.getenv("CLICKHOUSE_HOST");
        this.database = System.getenv("CLICKHOUSE_DATABASE");
        this.user = System.getenv("CLICKHOUSE_USER");
        this.password = System.getenv("CLICKHOUSE_PASSWORD");

        this.httpPort = Integer.parseInt(
                System.getenv("CLICKHOUSE_HTTP_PORT")
        );

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
}