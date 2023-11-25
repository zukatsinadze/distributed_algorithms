package cs451.fifo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import cs451.Host;
import cs451.Message;
import cs451.Observer;
import cs451.broadcast.UniformReliableBroadcast;

public class FIFO implements Observer {
    private final UniformReliableBroadcast urb;
    private final Observer observer;
    private final int[] lastDelivered;
    private final List<PriorityQueue<Message>> pending;
    private final Lock pendingLock = new ReentrantLock();


    public FIFO(byte myId, int port, Observer observer, HashMap<Byte, Host> hostMap) {
        this.urb = new UniformReliableBroadcast(myId, port, this, hostMap);
        this.observer = observer;

        List<PriorityQueue<Message>> tmp = new ArrayList<>();
        for (int i = 0; i < hostMap.size(); i++) {
            tmp.add(new PriorityQueue<>());
        }
        this.pending = Collections.unmodifiableList(tmp);
        this.lastDelivered = new int[hostMap.size()];
    }

    public void broadcast(int messageId, byte originalSenderId) {
        urb.broadcast(messageId, originalSenderId);
    }

    public void stop() {
        urb.stop();
    }

    public void start() {
        urb.start();
    }

    public void deliver(Message m) {
        try {
            pendingLock.lock();
            pending.get(m.getOriginalSenderId()).add(m);

            while (m.getMessageId() - 1 == lastDelivered[m.getOriginalSenderId()]) {
                observer.deliver(m);
                lastDelivered[m.getOriginalSenderId()] = m.getMessageId();
                pending.get(m.getOriginalSenderId()).poll();

                if (pending.get(m.getOriginalSenderId()).isEmpty()) {
                    break;
                }
                m = pending.get(m.getOriginalSenderId()).peek();
            }
        } finally {
            pendingLock.unlock();
        }
    }

}
