package engine;

public class Partition {

    private final RingBuffer ringBuffer;

    public Partition(RingBuffer ringBuffer) {
        this.ringBuffer = ringBuffer;
    }

    public void publish(Event event) throws InterruptedException {
        ringBuffer.add(event);
    }

    public Event consume() throws InterruptedException {
        return ringBuffer.poll();
    }

    public void close() {
        ringBuffer.close();
    }
}