package cs451.links;

import cs451.Observer;
import cs451.Host;
import cs451.Message;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
// import java.util.concurrent.atomic.AtomicInteger;

public class PerfectLink implements Observer {
    private final StubbornLink stubbornLink;
    private final Observer observer;

    private ArrayList<Message> deliveredMessages;
    // private static AtomicInteger counter = new AtomicInteger(0);
    // private static AtomicInteger counter2 = new AtomicInteger(0);

    public PerfectLink(int port, Observer observer) {
        this.stubbornLink = new StubbornLink(port, this);
        this.observer = observer;
        this.deliveredMessages = new ArrayList<>();
    }

    public void send(Message message, Host host) {
        stubbornLink.send(message, host);
    }

    public void start() {
        stubbornLink.start();
    }

    public void stop() {
        stubbornLink.stop();
    }

    @Override
    public void deliver(Message message) {
        if (!hasDelivered(message)) {
            // System.out.println("Delivery count: " + counter.incrementAndGet());
            deliveredMessages.add(message);
            observer.deliver(message);
        } else {
            // System.out.println("Duplicate count: " + counter2.incrementAndGet());
        }
    }

    private boolean hasDelivered(Message message) {
        return deliveredMessages.contains(message);
    }
}
