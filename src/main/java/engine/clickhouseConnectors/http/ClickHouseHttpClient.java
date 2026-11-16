
package engine.clickhouseConnectors.http;

import engine.clickhouseConnectors.config.ClickHouseConfig;

import java.net.URI;
import java.net.http.HttpClient;

public class ClickHouseHttpClient {

    private final HttpClient httpClient;
    private final URI baseUri;

    public ClickHouseHttpClient(ClickHouseConfig config) {

        this.httpClient = HttpClient.newHttpClient();

        this.baseUri = URI.create(
                "http://" + config.getHost()
                        + ":" + config.getHttpPort()
        );
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

    public URI getBaseUri() {
        return baseUri;
    }
}