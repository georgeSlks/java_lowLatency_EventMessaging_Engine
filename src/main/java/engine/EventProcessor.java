package engine;

public class EventProcessor {

    public void process(Event event) {
        System.out.println(
                "Processing event: " + event.getId()
                        + " type=" + event.getType()
                        + " payload=" + event.getPayload()
        );
    }
}
