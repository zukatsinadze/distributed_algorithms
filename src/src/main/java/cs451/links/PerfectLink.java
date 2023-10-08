package cs451.links;

import cs451.Observer;
import cs451.Host;
import cs451.Message;

import java.util.HashSet;
import java.util.Set;

public class PerfectLink implements Observer {
    private final StubbornLink stubbornLink;
    private final Observer observer;

    private Set<Message> deliveredMessages;

    public PerfectLink(int port, Observer observer) {
        this.stubbornLink = new StubbornLink(port, this);
        this.observer = observer;
        this.deliveredMessages = new HashSet<>();
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
            deliveredMessages.add(message);
            observer.deliver(message);
        } else {
            System.out.println("Duplicate message: " + message.getMessageId());
        }
    }

    private boolean hasDelivered(Message message) {
        return deliveredMessages.contains(message);
    }
}
