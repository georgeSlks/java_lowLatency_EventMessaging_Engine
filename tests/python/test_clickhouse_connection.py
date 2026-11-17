
import os
import requests

from pathlib import Path
from dotenv import load_dotenv


# Load environment variables
ENV_FILE = Path(__file__).resolve().parent.parent.parent / ".env"
load_dotenv(ENV_FILE)


# ClickHouse configuration
HOST = os.getenv("CLICKHOUSE_HOST", "localhost")
PORT = os.getenv("CLICKHOUSE_HTTP_PORT", "8123")
DATABASE = os.getenv("CLICKHOUSE_DATABASE", "LowLatencySomething")
USER = os.getenv("CLICKHOUSE_USER", "default")
PASSWORD = os.getenv("CLICKHOUSE_PASSWORD")

URL = f"http://{HOST}:{PORT}/"


def execute_query(query):

    response = requests.post(
        URL,
        params={"query": query},
        auth=(USER, PASSWORD),
        timeout=5
    )

    response.raise_for_status()

    return response.text.strip()


def test_connection():

    result = execute_query("SELECT 1")

    assert result == "1", "ClickHouse connection failed"

    print("Connection test passed")


def initialize_database():

    query = f"CREATE DATABASE IF NOT EXISTS {DATABASE}"

    execute_query(query)

    print("Database initialized")


def initialize_table():

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

    print("Table initialized")


def test_database():

    query = f"EXISTS DATABASE {DATABASE}"

    result = execute_query(query)

    assert result == "1", f"Database {DATABASE} does not exist"

    print("Database test passed")


def test_table():

    query = f"EXISTS TABLE {DATABASE}.engine_events"

    result = execute_query(query)

    assert result == "1", "Table engine_events does not exist"

    print("Table test passed")


if __name__ == "__main__":

    test_connection()

    initialize_database()
    initialize_table()

    test_database()
    test_table()

    print("\nAll ClickHouse connection tests passed!")