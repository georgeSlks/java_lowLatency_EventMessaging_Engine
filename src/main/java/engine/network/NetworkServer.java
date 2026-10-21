package engine.network;

import engine.Event;
import engine.EventBus;
import engine.EventType;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Instant;

public class NetworkServer implements AutoCloseable {

    private static final int DEFAULT_PORT = 8080;
    private static final int MAX_PAYLOAD_SIZE = 1024 * 1024;

    private final int port;
    private final EventBus eventBus;

    private volatile boolean running;
    private ServerSocket serverSocket;

    public NetworkServer(int port, EventBus eventBus) {
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("Invalid port");
        }

        this.port = port;
        this.eventBus = eventBus;
    }

    public void start() throws IOException, InterruptedException {
        if (running) {
            throw new IllegalStateException("Server is already running");
        }

        serverSocket = new ServerSocket(port);
        running = true;

        System.out.println("Network server listening on port " + port);

        try {
            while (running) {
                try (Socket client = serverSocket.accept()) {
                    handleClient(client);
                } catch (IOException e) {
                    if (running) {
                        throw e;
                    }
                }
            }
        } finally {
            running = false;
            if (!serverSocket.isClosed()) {
                serverSocket.close();
            }
        }
    }

    private void handleClient(Socket client)
            throws IOException, InterruptedException {

        System.out.println("Client connected: " + client.getRemoteSocketAddress());

        DataInputStream input =
                new DataInputStream(client.getInputStream());

        while (running) {
            final int length;

            try {
                length = input.readInt();
            } catch (java.io.EOFException e) {
                return;
            }

            if (length < 1 || length > MAX_PAYLOAD_SIZE) {
                throw new IOException("Invalid payload length: " + length);
            }

            byte[] payload = new byte[length];
            input.readFully(payload);

            Event event = decodeEvent(payload);
            eventBus.publish(event);
        }
    }

    private Event decodeEvent(byte[] payload) throws IOException {
        
        String message = new String(
                payload, java.nio.charset.StandardCharsets.UTF_8);

        int separator = message.indexOf('|');

        if (separator <= 0 || separator == message.length() - 1) {
            throw new IOException("Invalid event message");
        }

        String eventId = message.substring(0, separator);
        String eventPayload = message.substring(separator + 1);

        return new Event(
                eventId,
                Instant.now(),
                EventType.DATA,
                eventPayload
        );
    }

    @Override
    public void close() throws IOException {
        running = false;

        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }
    }
}