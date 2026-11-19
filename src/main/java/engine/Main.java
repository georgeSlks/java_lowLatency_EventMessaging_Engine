
package engine;

import engine.clickhouseConnectors.config.ClickHouseConfig;
import engine.clickhouseConnectors.http.ClickHouseHttpClient;
import engine.clickhouseConnectors.http.ClickHouseHttpWriter;
import engine.network.NetworkServer;

public final class Main {

    private static final int PORT = 8080;
    private static final int RING_BUFFER_CAPACITY = 1024;

    private Main() {
    }

    public static void main(String[] args) throws Exception {

        // Initialize ClickHouse configuration
        ClickHouseConfig config = new ClickHouseConfig();

        // Initialize ClickHouse HTTP client
        ClickHouseHttpClient client =
                new ClickHouseHttpClient(config);

        // Initialize ClickHouse writer
        ClickHouseHttpWriter writer =
                new ClickHouseHttpWriter(client, config);

        // Initialize event processing pipeline
        RingBuffer ringBuffer =
                new RingBuffer(RING_BUFFER_CAPACITY);

        Partition partition =
                new Partition(ringBuffer);

        EventBus eventBus =
                new EventBus(partition);

        EventProcessor processor =
                new EventProcessor(writer);

        EventConsumer consumer =
                new EventConsumer(partition, processor);

        Thread consumerThread =
                new Thread(consumer, "event-consumer");

        // Initialize network server
        NetworkServer networkServer =
                new NetworkServer(PORT, eventBus);

        // Shutdown hook
        Runtime.getRuntime().addShutdownHook(
                new Thread(() -> {
                    System.out.println(
                            "Shutdown signal received..."
                    );

                    networkServer.close();
                }, "shutdown-hook")
        );

        System.out.println(
                "Starting Event Messaging Engine..."
        );

        System.out.println(
                "RingBuffer capacity: " + RING_BUFFER_CAPACITY
        );

        System.out.println(
                "ClickHouse host: " + config.getHost()
        );

        // Start consumer
        consumerThread.start();

        try {
            // Start accepting TCP connections
            networkServer.start();

        } finally {

            // Stop accepting new events
            networkServer.close();

            // Close buffer and drain remaining events
            partition.close();

            // Wait for consumer termination
            consumerThread.join();

            System.out.println(
                    "Event Messaging Engine stopped."
            );
        }
    }
}
