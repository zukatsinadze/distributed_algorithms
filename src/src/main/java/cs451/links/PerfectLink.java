package cs451.links;

import cs451.Observer;
import cs451.Host;
import cs451.Message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;


public class PerfectLink implements Observer {
    private final StubbornLink stubbornLink;
    private final Observer observer;
    private final HashMap<Byte, Host> hostMap;
    private HashSet<Integer> deliveredMessages;
    private HashSet<Message> retry;

    public PerfectLink(int port, Observer observer, HashMap<Byte, Host> hostMap) {
        this.hostMap = hostMap;
        this.stubbornLink = new StubbornLink(this, port, hostMap);
        this.observer = observer;
        this.deliveredMessages = new HashSet<>();
        this.retry = new HashSet<>();
    }

    public void send(Message message) {
        int windowSize = 300000 / ((this.hostMap.size() - 1));
        // if (stubbornLink.getMessagePoolSize() > windowSize) {
        //     while (stubbornLink.getMessagePoolSize() > windowSize) {
        //         try {
        //             Thread.sleep(100);
        //         } catch (InterruptedException e) {
        //             e.printStackTrace();
        //         }
        //     }

        // }

        if (retry.size() > 0) {
            ArrayList<Message> toRemove = new ArrayList<>(retry);

            for (Message m : toRemove) {
                if (stubbornLink.getMessagePoolSize(m.getReceiverId()) >= windowSize) {
                    continue;
                }

                retry.remove(m);
                stubbornLink.send(m);
            }
            toRemove.clear();
        }

        if (stubbornLink.getMessagePoolSize(message.getReceiverId()) >= windowSize) {
            retry.add(message);
            return;
        }

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
        if (message.isAckAck()) {
            deliveredMessages.remove(message.uniqueId());
            return;
        }
        // if (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() > 63 * 1024 * 1024) {
        //     System.gc();
        // }
        if (deliveredMessages.add(message.uniqueId()))
            observer.deliver(message);
    }
}
