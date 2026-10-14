package engine;

public class EventConsumer implements Runnable {

    private final Partition partition;
    private final EventProcessor processor;

    public EventConsumer(
            Partition partition,
            EventProcessor processor) {

        this.partition = partition;
        this.processor = processor;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Event event = partition.consume();

                if (event == null) {
                    // Buffer closed and drained.
                    return;
                }

                processor.process(event);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}