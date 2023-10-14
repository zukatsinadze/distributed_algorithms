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

    private HashMap<Integer, Message> deliveredMessages;

    public PerfectLink(int port, Observer observer, DatagramSocket socket, HashMap<Integer, Host> hostMap) {
        this.stubbornLink = new StubbornLink(this, socket, hostMap);
        this.observer = observer;
        this.deliveredMessages = new HashMap<>();
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
        if (!deliveredMessages.containsKey(key)) {
            deliveredMessages.put(key, message);
            observer.deliver(message);
        }
    }
}
