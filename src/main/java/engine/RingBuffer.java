package engine;

public class RingBuffer {

    private final Event[] buffer;
    private final int capacity;

    private int head;
    private int tail;

    public RingBuffer(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be greater than 0");
        }

        this.capacity = capacity;
        this.buffer = new Event[capacity];
    }

    public void add(Event event) {
        if (isFull()) {
            throw new IllegalStateException("RingBuffer is full");
        }

        buffer[tail] = event;
        tail = (tail + 1) % capacity;
    }

    public Event poll() {
        if (isEmpty()) {
            return null;
        }

        Event event = buffer[head];
        buffer[head] = null;
        head = (head + 1) % capacity;

        return event;
    }

    public boolean isEmpty() {
        return head == tail;
    }

    public boolean isFull() {
        return (tail + 1) % capacity == head;
    }
}