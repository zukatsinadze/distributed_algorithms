package cs451.links;

import cs451.Observer;
import cs451.Host;
import cs451.Message;

import java.net.DatagramSocket;
import java.util.HashMap;
import java.util.HashSet;


public class PerfectLink implements Observer {
    private final StubbornLink stubbornLink;
    private final Observer observer;

    private HashSet<Message> deliveredMessages;

    public PerfectLink(int port, Observer observer, DatagramSocket socket, HashMap<Integer, Host> hostMap) {
        this.stubbornLink = new StubbornLink(this, socket, hostMap);
        this.observer = observer;
        this.deliveredMessages = new HashSet<>();
    }

    public void send(Message message) {
        stubbornLink.send(message);
    }

    public void start() {
        stubbornLink.start();
    }

    public static void stop() {
        StubbornLink.stop();
    }

    @Override
    public void deliver(Message message) {
        if (!hasDelivered(message)) {
            deliveredMessages.add(message);
            observer.deliver(message);
        }
    }

    private boolean hasDelivered(Message message) {
        return deliveredMessages.contains(message);
    }
}
