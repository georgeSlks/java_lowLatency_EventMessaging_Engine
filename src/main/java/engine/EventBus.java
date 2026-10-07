package engine;

public class EventBus {

    private final Partition partition;

    public EventBus(Partition partition) {
        this.partition = partition;
    }

    public void publish(Event event) {
        partition.publish(event);
    }
}
