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

    public PerfectLink(byte myId, int port, Observer observer, HashMap<Byte, Host> hostMap) {
        this.stubbornLink = new StubbornLink(this, myId, port, hostMap);
        this.observer = observer;
        this.deliveredMessages = new HashSet<>();
    }

    public void send(Message message) {
      stubbornLink.send(message);
    }

    public void start() {
        stubbornLink.start();
    }

    public void stop() {
        stubbornLink.stop();
    }

    @Override
    public void deliver(Message message) {
        if (deliveredMessages.add(message.uniqueId()))
            observer.deliver(message);
    }
}
