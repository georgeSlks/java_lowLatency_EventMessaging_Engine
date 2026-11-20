
import os
import socket
import struct
import subprocess
import time
import uuid
import requests

from pathlib import Path
from dotenv import load_dotenv


PROJECT_ROOT = Path(__file__).resolve().parent.parent.parent

load_dotenv(PROJECT_ROOT / ".env")

HOST = "127.0.0.1"
PORT = 8080

CH_HOST = os.getenv("CLICKHOUSE_HOST", "localhost")
CH_PORT = os.getenv("CLICKHOUSE_HTTP_PORT", "8123")
DATABASE = os.getenv("CLICKHOUSE_DATABASE", "LowLatencySomething")
USER = os.getenv("CLICKHOUSE_USER", "default")
PASSWORD = os.getenv("CLICKHOUSE_PASSWORD")

CH_URL = f"http://{CH_HOST}:{CH_PORT}/"

NUMBER_OF_EVENTS = 100


def execute_query(query):

    response = requests.post(
        CH_URL,
        params={"query": query},
        auth=(USER, PASSWORD),
        timeout=10
    )

    response.raise_for_status()

    return response.text.strip()


def start_engine():

    process = subprocess.Popen(
        ["./jll_engine_run.sh"],
        cwd=PROJECT_ROOT,
        stdout=subprocess.DEVNULL,
        stderr=subprocess.PIPE
    )

    for _ in range(30):

        if process.poll() is not None:
            raise RuntimeError("Engine failed to start")

        try:
            with socket.create_connection(
                (HOST, PORT),
                timeout=1
            ):
                print("Engine started successfully")
                return process

        except OSError:
            time.sleep(1)

    process.terminate()

    raise TimeoutError("Engine did not start")


def send_events():

    event_ids = []

    with socket.create_connection((HOST, PORT)) as client:

        for i in range(NUMBER_OF_EVENTS):

            event_id = f"e2e-{uuid.uuid4()}"

            payload = f"End-to-end test event {i}"

            message = f"{event_id}|{payload}".encode("utf-8")

            packet = struct.pack(">I", len(message)) + message

            client.sendall(packet)

            event_ids.append(event_id)

    print(f"Sent {len(event_ids)} events")

    return event_ids


def verify_events(event_ids):

    ids = ",".join(
        f"'{event_id}'" for event_id in event_ids
    )

    query = f"""
    SELECT count()
    FROM {DATABASE}.engine_events
    WHERE event_id IN ({ids})
    """

    for _ in range(30):

        result = int(execute_query(query))

        if result == len(event_ids):
            print("All events found in ClickHouse")
            return

        time.sleep(1)

    raise AssertionError(
        f"Expected {len(event_ids)} events, found {result}"
    )


def test_end_to_end():

    print("\nStarting End-to-End Test")

    process = start_engine()

    try:

        event_ids = send_events()

        verify_events(event_ids)

        print("\nEnd-to-End Test Passed!")

    finally:

        process.terminate()

        try:
            process.wait(timeout=10)

        except subprocess.TimeoutExpired:
            process.kill()
            process.wait()


if __name__ == "__main__":

    test_end_to_end()