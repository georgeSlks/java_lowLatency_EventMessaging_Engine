
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
    private volatile boolean closed;

    private ServerSocket serverSocket;

    public NetworkServer(int port, EventBus eventBus) {

        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("Invalid port");
        }

        this.port = port;
        this.eventBus = eventBus;
    }

    public void start() throws IOException {

        if (running || closed) {
            throw new IllegalStateException(
                    "Server cannot be started"
            );
        }

        serverSocket = new ServerSocket(port);
        running = true;

        System.out.println(
                "Network server listening on port " + port
        );

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
    }

    @Override
    public synchronized void close() {

        if (closed) {
            return;
        }

        closed = true;
        running = false;

        System.out.println("Stopping network server...");

        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException ignored) {
        }

        for (Socket client : activeClients) {
            try {
                client.close();
            } catch (IOException ignored) {
            }
        }

        clientExecutor.shutdownNow();

        System.out.println("Network server stopped.");
    }
}