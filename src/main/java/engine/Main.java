
package engine;

import engine.network.NetworkServer;

public final class Main {

    private static final int PORT = 8080;
    private static final int RING_BUFFER_CAPACITY = 1024;

    private Main() {
    }

    public static void main(String[] args) throws Exception {

        RingBuffer ringBuffer =
                new RingBuffer(RING_BUFFER_CAPACITY);

        Partition partition =
                new Partition(ringBuffer);

        EventBus eventBus =
                new EventBus(partition);

        EventProcessor processor =
                new EventProcessor();

        EventConsumer consumer =
                new EventConsumer(partition, processor);

        Thread consumerThread =
                new Thread(consumer, "event-consumer");

        NetworkServer networkServer =
                new NetworkServer(PORT, eventBus);

        Runtime.getRuntime().addShutdownHook(
                new Thread(() -> {
                    System.out.println(
                            "Shutdown signal received..."
                    );

                    try {
                        networkServer.close();
                    } catch (Exception e) {
                        System.err.println(
                                "Error closing network server: "
                                        + e.getMessage()
                        );
                    }
                }, "shutdown-hook")
        );

        //Start application
        System.out.println("Starting Event Messaging Engine...");
        System.out.println(
                "RingBuffer capacity: " + RING_BUFFER_CAPACITY
        );

        consumerThread.start();

        try {
            networkServer.start();
        } finally {

            // Stop accepting new events
            networkServer.close();

            // Drain remaining events
            partition.close();

            // Wait for consumer termination
            consumerThread.join();

            System.out.println(
                    "Event Messaging Engine stopped."
            );
        }
    }
}
