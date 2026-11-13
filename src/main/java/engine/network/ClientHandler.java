
package engine.network;

import engine.Event;
import engine.EventBus;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private static final int MAX_PAYLOAD_SIZE = 1024 * 1024;

    private final Socket client;
    private final EventBus eventBus;
    private final EventDecoder decoder;

    public ClientHandler(Socket client, EventBus eventBus) {
        this.client = client;
        this.eventBus = eventBus;
        this.decoder = new EventDecoder();
    }

    @Override
    public void run() {

        try (Socket socket = client;
             DataInputStream input =
                     new DataInputStream(socket.getInputStream())) {

            while (!socket.isClosed()) {

                int length;

                try {
                    length = input.readInt();
                } catch (EOFException e) {
                    break;
                }

                if (length < 1 || length > MAX_PAYLOAD_SIZE) {
                    throw new IOException("Invalid payload length: " + length);
                }

                byte[] payload = new byte[length];
                input.readFully(payload);

                Event event = decoder.decode(payload);

                eventBus.publish(event);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

        } catch (IOException | IllegalStateException e) {
            System.err.println(
                    "Client connection error: " + e.getMessage()
            );
        }
    }
}