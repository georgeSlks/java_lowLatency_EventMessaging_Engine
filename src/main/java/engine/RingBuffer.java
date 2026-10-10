/* Single Consumer Single Producer Concurent Ring Buffer

EventBus[(Puplish Event)]-> Partition[publish(event)] -> RingBuffer[event storing and transferring] -> EventProcessor[process(event)]
*/

package engine;

import java.util.Objects;

public class RingBuffer {

    private final Event[] buffer;
    private final int capacity;

    // Written only by the consumer, read by the producer.
    private volatile long head;

    // Written only by the producer, read by the consumer.
    private volatile long tail;

    public RingBuffer(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException(
                "Capacity must be greater than 0"
            );
        }

        this.capacity = capacity;
        this.buffer = new Event[capacity];
    }

    // Single producer only.
    public void add(Event event) {
        Objects.requireNonNull(event, "Event must not be null");

        long currentTail = tail;
        long currentHead = head;

        if (currentTail - currentHead >= capacity) {
            throw new IllegalStateException("RingBuffer is full");
        }

        int index = (int) (currentTail % capacity);

        // Write the event.
        buffer[index] = event;

        // Publish the event to the consumer.
        tail = currentTail + 1;
    }

    // Single consumer only.
    public Event poll() {
        long currentHead = head;
        long currentTail = tail;

        if (currentHead >= currentTail) {
            return null;
        }

        int index = (int) (currentHead % capacity);

        // Read the published event
        Event event = buffer[index];

        // Release the slot for the next use.
        buffer[index] = null;

        // Publish the consumer change.
        head = currentHead + 1;

        return event;
    }

    public boolean isEmpty() {
        return head >= tail;
    }

    public boolean isFull() {
        return tail - head >= capacity;
    }

    public int capacity() {
        return capacity;
    }
}