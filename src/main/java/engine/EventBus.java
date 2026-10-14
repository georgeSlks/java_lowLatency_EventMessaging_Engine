package engine;

public class EventBus {

    private final Partition partition;

    public EventBus(Partition partition) {
        this.partition = partition;
    }

    public void publish(Event event) throws InterruptedException {
        partition.publish(event);
    }
}