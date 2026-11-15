
import socket
import struct

HOST = "127.0.0.1"
PORT = 8080

event_id = "event-1"
payload = "Hello from Python client"

# Build the message
message = f"{event_id}|{payload}".encode("utf-8")

# Add message length (4 bytes)
data = struct.pack(">I", len(message)) + message

# Connect to the Java server
with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as client:
    client.connect((HOST, PORT))

    print("Connected to Java server")

    client.sendall(data)

    print(f"Event sent: {event_id}")
    print(f"Payload: {payload}")

print("Client disconnected")
