
package engine.network;

import engine.EventBus;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NetworkServer implements AutoCloseable {

    private static final int MAX_CLIENT_THREADS = 4;

    private final int port;
    private final EventBus eventBus;

    private final ExecutorService clientExecutor =
            Executors.newFixedThreadPool(MAX_CLIENT_THREADS);

    private final Set<Socket> activeClients =
            ConcurrentHashMap.newKeySet();

    private volatile boolean running;
    private ServerSocket serverSocket;

    public NetworkServer(int port, EventBus eventBus) {

        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("Invalid port");
        }

        this.port = port;
        this.eventBus = eventBus;
    }

    public void start() throws IOException {

        if (running) {
            throw new IllegalStateException("Server is already running");
        }

        serverSocket = new ServerSocket(port);
        running = true;

        System.out.println("Network server listening on port " + port);

        try {
            while (running) {

                try {
                    Socket client = serverSocket.accept();

                    activeClients.add(client);

                    clientExecutor.execute(() -> {
                        try {
                            new ClientHandler(client, eventBus).run();
                        } finally {
                            activeClients.remove(client);
                        }
                    });

                } catch (SocketException e) {
                    if (running) {
                        throw e;
                    }
                }
            }

        } finally {
            close();
        }
    }

    @Override
    public void close() {

        running = false;

        // Close server socket
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException ignored) {
        }

        // Close active client connections
        for (Socket client : activeClients) {
            try {
                client.close();
            } catch (IOException ignored) {
            }
        }

        // Stop client threads
        clientExecutor.shutdownNow();

        System.out.println("Network server stopped.");
    }
}