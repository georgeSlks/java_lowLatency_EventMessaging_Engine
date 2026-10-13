package engine;

import java.util.Objects;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class RingBuffer {

    private final Event[] buffer;
    private final int capacity;

    private int head;
    private int tail;
    private int size;

    private boolean closed;

    private final ReentrantLock lock = new ReentrantLock();
    private final Condition notEmpty = lock.newCondition();
    private final Condition notFull = lock.newCondition();

    public RingBuffer(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException(
                "Capacity must be greater than 0"
            );
        }

        this.capacity = capacity;
        this.buffer = new Event[capacity];
    }

    // Blocks while the buffer is full.
    // Supports one producer and one consumer, as designed.
    public void add(Event event) throws InterruptedException {
        Objects.requireNonNull(event, "Event must not be null");

        lock.lockInterruptibly();
        try {
            while (size == capacity && !closed) {
                notFull.await();
            }

            if (closed) {
                throw new IllegalStateException(
                    "RingBuffer is closed"
                );
            }

            buffer[tail] = event;
            tail = (tail + 1) % capacity;
            size++;

            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }

    // Blocks while empty. 
    // Returns null after close and drain.
    public Event poll() throws InterruptedException {
        lock.lockInterruptibly();
        try {
            while (size == 0 && !closed) {
                notEmpty.await();
            }

            if (size == 0) {
                return null;
            }

            Event event = buffer[head];
            buffer[head] = null;

            head = (head + 1) % capacity;
            size--;

            notFull.signal();

            return event;
        } finally {
            lock.unlock();
        }
    }

    // Stops new publications and wakes all waiting threads.
    // Also , already published events remain available to the consumer(both are act on different threads).
    public void close() {
        lock.lock();
        try {
            closed = true;

            notEmpty.signalAll();
            notFull.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public int capacity() {
        return capacity;
    }
}