CREATE DATABASE IF NOT EXISTS LowLatencySomething;

CREATE TABLE IF NOT EXISTS LowLatencySomething.engine_events
(
    event_id   String,
    event_time DateTime64(9, 'UTC'),
    event_type LowCardinality(String),
    payload    String
)
ENGINE = MergeTree
PARTITION BY toYYYYMM(event_time)
ORDER BY (event_type, event_time, event_id);