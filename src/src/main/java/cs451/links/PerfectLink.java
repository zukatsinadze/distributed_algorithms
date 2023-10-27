package cs451.links;

import cs451.Observer;
import cs451.Host;
import cs451.Message;

import java.util.HashMap;
import java.util.HashSet;


public class PerfectLink implements Observer {
    private final StubbornLink stubbornLink;
    private final Observer observer;

    private HashSet<Integer> deliveredMessages;

    public PerfectLink(int port, Observer observer, HashMap<Byte, Host> hostMap) {
        this.stubbornLink = new StubbornLink(this, port, hostMap);
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
        int key = message.uniqueId();
        if (!deliveredMessages.contains(key)) {
            deliveredMessages.add(key);
            observer.deliver(message);
        }
    }
}
