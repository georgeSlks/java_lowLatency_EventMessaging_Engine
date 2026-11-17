
import os
import json
import uuid
import subprocess
import tempfile
import requests

from pathlib import Path
from datetime import datetime, timezone
from dotenv import load_dotenv


# Project paths
PROJECT_ROOT = Path(__file__).resolve().parent.parent.parent

ENV_FILE = PROJECT_ROOT / ".env"
load_dotenv(ENV_FILE)


# ClickHouse configuration
HOST = os.getenv("CLICKHOUSE_HOST", "localhost")
PORT = os.getenv("CLICKHOUSE_HTTP_PORT", "8123")
DATABASE = os.getenv("CLICKHOUSE_DATABASE", "LowLatencySomething")
USER = os.getenv("CLICKHOUSE_USER", "default")
PASSWORD = os.getenv("CLICKHOUSE_PASSWORD")

URL = f"http://{HOST}:{PORT}/"


# Java source files
JAVA_FILES = [
    PROJECT_ROOT / "src/main/java/engine/Event.java",
    PROJECT_ROOT / "src/main/java/engine/EventType.java",
    PROJECT_ROOT / "src/main/java/engine/clickhouseConnectors/config/ClickHouseConfig.java",
    PROJECT_ROOT / "src/main/java/engine/clickhouseConnectors/http/ClickHouseHttpClient.java",
    PROJECT_ROOT / "src/main/java/engine/clickhouseConnectors/http/ClickHouseHttpWriter.java"
]


def execute_query(query):

    response = requests.post(
        URL,
        params={"query": query},
        auth=(USER, PASSWORD),
        timeout=5
    )

    response.raise_for_status()

    return response.text.strip()


def initialize_database():

    query = f"CREATE DATABASE IF NOT EXISTS {DATABASE}"

    execute_query(query)

    query = f"""
    CREATE TABLE IF NOT EXISTS {DATABASE}.engine_events
    (
        event_id String,
        event_time DateTime64(9, 'UTC'),
        event_type LowCardinality(String),
        payload String
    )
    ENGINE = MergeTree
    PARTITION BY toYYYYMM(event_time)
    ORDER BY (event_type, event_time, event_id)
    """

    execute_query(query)

    print("Database and table initialized")


def create_java_runner(directory):

    java_code = """
import engine.Event;
import engine.EventType;

import engine.clickhouseConnectors.config.ClickHouseConfig;
import engine.clickhouseConnectors.http.ClickHouseHttpClient;
import engine.clickhouseConnectors.http.ClickHouseHttpWriter;

import java.time.Instant;

public class ClickHouseWriterTest {

    public static void main(String[] args) throws Exception {

        String eventId = args[0];
        Instant timestamp = Instant.parse(args[1]);
        String payload = args[2];

        ClickHouseConfig config = new ClickHouseConfig();

        ClickHouseHttpClient client =
                new ClickHouseHttpClient(config);

        ClickHouseHttpWriter writer =
                new ClickHouseHttpWriter(client, config);

        Event event = new Event(
                eventId,
                timestamp,
                EventType.DATA,
                payload
        );

        writer.write(event);

        System.out.println("Event written successfully");
    }
}
"""

    runner_file = directory / "ClickHouseWriterTest.java"

    runner_file.write_text(java_code)

    return runner_file


def compile_java(runner_file, output_directory):

    command = [
        "javac",
        "-d",
        str(output_directory)
    ]

    for java_file in JAVA_FILES:
        command.append(str(java_file))

    command.append(str(runner_file))

    subprocess.run(
        command,
        cwd=PROJECT_ROOT,
        check=True
    )

    print("Java compilation passed")


def write_event(event_id, timestamp, payload, class_directory):

    command = [
        "java",
        "-cp",
        str(class_directory),
        "ClickHouseWriterTest",
        event_id,
        timestamp,
        payload
    ]

    subprocess.run(
        command,
        cwd=PROJECT_ROOT,
        check=True,
        env=os.environ.copy()
    )

    print("Java HTTP writer test passed")


def verify_event(event_id, expected_payload):

    query = f"""
    SELECT event_id, event_type, payload
    FROM {DATABASE}.engine_events
    WHERE event_id = '{event_id}'
    FORMAT JSONEachRow
    """

    result = execute_query(query)

    assert result, "Event was not found in ClickHouse"

    rows = result.splitlines()

    assert len(rows) == 1, "Unexpected number of matching events"

    event = json.loads(rows[0])

    assert event["event_id"] == event_id, "Incorrect event ID"

    assert event["event_type"] == "DATA", "Incorrect event type"

    assert event["payload"] == expected_payload, "Incorrect payload"

    print("Event verification passed")


if __name__ == "__main__":

    initialize_database()

    event_id = "python-test-" + str(uuid.uuid4())

    timestamp = datetime.now(timezone.utc).isoformat()

    payload = "Hello from Python ClickHouse writer test"

    with tempfile.TemporaryDirectory() as temp_directory:

        temp_directory = Path(temp_directory)

        runner_file = create_java_runner(temp_directory)

        compile_java(runner_file, temp_directory)

        write_event(
            event_id,
            timestamp,
            payload,
            temp_directory
        )

    verify_event(event_id, payload)

    print("\nAll ClickHouse writer tests passed!")