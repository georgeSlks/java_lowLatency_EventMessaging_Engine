package engine;

public class Partition {

    private final RingBuffer ringBuffer;

    public Partition(RingBuffer ringBuffer) {
        this.ringBuffer = ringBuffer;
    }

    public void publish(Event event) {
        ringBuffer.add(event);
    }

    public Event consume() {
        return ringBuffer.poll();
    }
}