/*
  * Create the database and events table to store retail events such as purchases.
  * DateTime64(9) provides nanosecond precision, while UTC ensures consistent timestamps.
  */

CREATE DATABASE IF NOT EXISTS retailtech;

CREATE TABLE IF NOT EXISTS retailtech.events
(
    event_id      String,
    event_time    DateTime64(9, 'UTC'),
    store_id      UInt32,
    product_id    UInt64,
    event_type    LowCardinality(String),
    quantity      Int32,
    price         Decimal(12, 2)
)
ENGINE = MergeTree
PARTITION BY toYYYYMM(event_time)
ORDER BY (store_id, product_id, event_time, event_id);
