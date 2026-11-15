
import socket
import struct
import threading

HOST = "127.0.0.1"
PORT = 8080

NUMBER_OF_CLIENTS = 10
EVENTS_PER_CLIENT = 100


def send_events(client_id):

    try:
        with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as client:
            client.connect((HOST, PORT))

            for i in range(EVENTS_PER_CLIENT):

                event_id = f"client-{client_id}-event-{i}"
                payload = f"Hello from client {client_id}"

                message = f"{event_id}|{payload}".encode("utf-8")

                data = struct.pack(">I", len(message)) + message

                client.sendall(data)

        print(f"Client {client_id} finished successfully")

    except Exception as e:
        print(f"Client {client_id} failed: {e}")


threads = []

print("Starting multiple client test...")

for client_id in range(NUMBER_OF_CLIENTS):

    thread = threading.Thread(
        target=send_events,
        args=(client_id,)
    )

    threads.append(thread)
    thread.start()


for thread in threads:
    thread.join()


print("Multiple client test finished")
print(f"Expected events: {NUMBER_OF_CLIENTS * EVENTS_PER_CLIENT}")
