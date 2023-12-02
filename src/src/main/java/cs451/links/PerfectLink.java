package cs451.links;

import cs451.Observer;
import cs451.Host;
import cs451.Message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantLock;


public class PerfectLink implements Observer {
    private final StubbornLink stubbornLink;
    private final Observer observer;
    private final HashMap<Byte, Host> hostMap;
    private HashSet<Integer> deliveredMessages;
    private HashSet<Message> retry;
    private ReentrantLock retryLock = new ReentrantLock();

    public PerfectLink(int port, Observer observer, HashMap<Byte, Host> hostMap) {
        this.hostMap = hostMap;
        this.stubbornLink = new StubbornLink(this, port, hostMap);
        this.observer = observer;
        this.deliveredMessages = new HashSet<>();
        this.retry = new HashSet<>();

        (new Timer()).schedule(new TimerTask() {
            @Override
            public void run() {
                int windowSize = 300000 / ((hostMap.size() - 1));
                retryLock.lock();
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
                retryLock.unlock();
            }
        }, 1000, 200);
    }

    public void send(Message message) {
        int windowSize = 300000 / ((this.hostMap.size() - 1));
        if (stubbornLink.getMessagePoolSize(message.getReceiverId()) >= windowSize) {
            retryLock.lock();
            try {
                retry.add(message);
            } finally {
                retryLock.unlock();
            }
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
        if (deliveredMessages.add(message.uniqueId()))
            observer.deliver(message);
    }
}
