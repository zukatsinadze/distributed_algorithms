package cs451.links;

import cs451.Observer;
import cs451.Host;
import cs451.Message;

import java.net.DatagramSocket;
import java.util.HashSet;

// import java.util.concurrent.atomic.AtomicInteger;

public class PerfectLink implements Observer {
    private final StubbornLink stubbornLink;
    private final Observer observer;

    private HashSet<Message> deliveredMessages;
    // private static AtomicInteger counter = new AtomicInteger(0);
    // private static AtomicInteger counter2 = new AtomicInteger(0);

    public PerfectLink(int port, Observer observer, DatagramSocket socket) {
        this.stubbornLink = new StubbornLink(this, socket);
        this.observer = observer;
        this.deliveredMessages = new HashSet<>();
    }

    public void send(Message message, Host host) {
        stubbornLink.send(message, host);
    }

    public void start() {
        stubbornLink.start();
    }

    public static void stop() {
        StubbornLink.stop();
    }

    @Override
    public synchronized void deliver(Message message) {
        // System.out.println("Delivered count: " + this + " " + deliveredMessages.size());
        if (!hasDelivered(message)) {
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
