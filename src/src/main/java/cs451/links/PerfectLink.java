package cs451.links;

import cs451.Observer;
import cs451.Host;
import cs451.Message;

import java.util.HashMap;
import java.util.HashSet;


public class PerfectLink implements Observer {
    private final StubbornLink stubbornLink;
    private final Observer observer;
    private final HashMap<Byte, Host> hostMap;
    private HashSet<Integer> deliveredMessages;

    public PerfectLink(int port, Observer observer, HashMap<Byte, Host> hostMap) {
        this.hostMap = hostMap;
        this.stubbornLink = new StubbornLink(this, port, hostMap);
        this.observer = observer;
        this.deliveredMessages = new HashSet<>();
    }

    public void send(Message message) {

        if (stubbornLink.getMessagePoolSize() > (150000 / (this.hostMap.size() - 1))) {
            while (stubbornLink.getMessagePoolSize() > 100) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
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
        if (deliveredMessages.add(message.uniqueId()))
            observer.deliver(message);
    }

}
